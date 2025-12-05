package com.example.surtefacilsv

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbRemember: CheckBox
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnThemeToggle: ImageButton

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firebaseAuth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val PREF_THEME = "app_theme"
        private const val THEME_LIGHT = "light"
        private const val THEME_DARK = "dark"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        firebaseAuth = FirebaseAuth.getInstance()

        initViews()
        loadSavedUser()
        updateThemeIcon()
        setupListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        cbRemember = findViewById(R.id.cbRemember)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        btnThemeToggle = findViewById(R.id.btnThemeToggle)
    }

    private fun loadSavedUser() {
        val savedEmail = sharedPreferences.getString("saved_email", "")
        val savedPassword = sharedPreferences.getString("saved_password", "")
        val isRemembered = sharedPreferences.getBoolean("remember_user", false)

        if (isRemembered && !savedEmail.isNullOrEmpty()) {
            etEmail.setText(savedEmail)
            etPassword.setText(savedPassword ?: "")
            cbRemember.isChecked = true
        }
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener { attemptLogin() }
        tvRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        tvForgotPassword.setOnClickListener { showForgotPasswordDialog() }
        btnThemeToggle.setOnClickListener { toggleTheme() }
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (validateInputs(email, password)) {
            btnLogin.isEnabled = false
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            fetchUserAndNavigate(user)
                        } else {
                            Toast.makeText(this, "Error: No se pudo obtener el usuario.", Toast.LENGTH_SHORT).show()
                            btnLogin.isEnabled = true
                        }
                    } else {
                        Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Fallo de autenticación. Verifique sus credenciales.", Toast.LENGTH_SHORT).show()
                        btnLogin.isEnabled = true
                    }
                }
        }
    }

    private fun fetchUserAndNavigate(firebaseUser: FirebaseUser) {
        firestore.collection("users").document(firebaseUser.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userType = document.getString("user_type")
                    val userName = document.getString("full_name") ?: "Usuario"
                    val businessName = document.getString("business_name") ?: "Negocio"
                    val email = firebaseUser.email ?: ""

                    if (cbRemember.isChecked) {
                        saveUserCredentials(email, etPassword.text.toString().trim())
                    }

                    val editor = sharedPreferences.edit()
                    editor.putString("user_uid", firebaseUser.uid) 
                    editor.putString("user_email", email)

                    if (userType == "Soy un Vendedor") {
                        editor.putString("user_name", businessName).apply()
                        val intent = Intent(this, SellerDashboardActivity::class.java)
                        intent.putExtra("business_name", businessName)
                        startActivity(intent)
                    } else {
                        editor.putString("user_name", userName).apply()
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                    finish()
                } else {
                    Toast.makeText(this, "Error: No se encontraron datos para este usuario.", Toast.LENGTH_SHORT).show()
                    btnLogin.isEnabled = true
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LoginActivity", "Error getting user details", exception)
                Toast.makeText(this, "Error al obtener datos del usuario.", Toast.LENGTH_SHORT).show()
                btnLogin.isEnabled = true
            }
    }
    
    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Restablecer Contraseña")
        val view = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        val etEmailDialog = view.findViewById<EditText>(R.id.etEmailDialog)
        builder.setView(view)
        builder.setPositiveButton("Enviar") { _, _ ->
            val email = etEmailDialog.text.toString().trim()
            sendPasswordResetEmail(email)
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun sendPasswordResetEmail(email: String) {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, ingrese un correo válido", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Correo de restablecimiento enviado. Revisa tu bandeja de spam.", Toast.LENGTH_LONG).show()
                } else {
                    Log.e("FORGOT_PASSWORD", "Error al enviar correo de restablecimiento", task.exception)
                    Toast.makeText(this, "No se pudo enviar el correo. Verifica que el correo esté registrado.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun toggleTheme() {
        val currentTheme = sharedPreferences.getString(PREF_THEME, THEME_LIGHT)
        val newTheme = if (currentTheme == THEME_LIGHT) THEME_DARK else THEME_LIGHT

        sharedPreferences.edit().putString(PREF_THEME, newTheme).apply()
        applyTheme(newTheme)
        recreate()
    }

    private fun applyTheme(theme: String) {
        when (theme) {
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun applySavedTheme() {
        val savedTheme = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString(PREF_THEME, THEME_LIGHT) ?: THEME_LIGHT
        applyTheme(savedTheme)
    }

    private fun updateThemeIcon() {
        val currentTheme = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString(PREF_THEME, THEME_LIGHT)
        val iconRes = if (currentTheme == THEME_DARK) R.drawable.ic_day else R.drawable.ic_night
        btnThemeToggle.setImageResource(iconRes)
    }

    private fun saveUserCredentials(email: String, password: String) {
        val editor = sharedPreferences.edit()
        editor.putString("saved_email", email)
        editor.putString("saved_password", password)
        editor.putBoolean("remember_user", true)
        editor.apply()
    }

    private fun clearUserCredentials() {
        val editor = sharedPreferences.edit()
        editor.remove("saved_email")
        editor.remove("saved_password")
        editor.remove("remember_user")
        editor.apply()
    }

    private fun validateInputs(email: String, password: String): Boolean {
        etEmail.error = null
        etPassword.error = null

        if (email.isEmpty()) {
            etEmail.error = "El correo electrónico es requerido"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Formato de correo electrónico inválido"
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "La contraseña es requerida"
            return false
        }

        if (password.length < 8) {
            etPassword.error = "La contraseña debe tener al menos 8 caracteres"
            return false
        }

        return true
    }
}
