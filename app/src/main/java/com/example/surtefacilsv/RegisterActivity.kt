package com.example.surtefacilsv

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var btnThemeToggle: ImageButton

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme() // Aplicar tema antes de crear la vista

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        databaseHelper = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        firestore = FirebaseFirestore.getInstance() // Inicializa el Firestore

        initViews()
        updateThemeIcon()
        setupListeners()
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
        btnThemeToggle = findViewById(R.id.btnThemeToggle)
    }

    private fun setupListeners() {
        btnRegister.setOnClickListener { attemptRegister() }
        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        btnThemeToggle.setOnClickListener { toggleTheme() }
    }

    private fun toggleTheme() {
        val currentTheme = sharedPreferences.getString("app_theme", "light")
        val newTheme = if (currentTheme == "light") "dark" else "light"
        sharedPreferences.edit().putString("app_theme", newTheme).apply()
        applyTheme(newTheme)
        updateThemeIcon()
        Toast.makeText(this, "Tema: ${if (newTheme == "dark") "Oscuro" else "Claro"}", Toast.LENGTH_SHORT).show()
    }

    private fun applySavedTheme() {
        val savedTheme = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("app_theme", "light") ?: "light"
        applyTheme(savedTheme)
    }

    private fun applyTheme(theme: String) {
        when (theme) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun updateThemeIcon() {
        val currentTheme = sharedPreferences.getString("app_theme", "light")
        val iconRes = if (currentTheme == "dark") R.drawable.ic_day else R.drawable.ic_night
        btnThemeToggle.setImageResource(iconRes)
    }

    private fun attemptRegister() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (validateInputs(fullName, email, password)) {
            val success = databaseHelper.addUser(fullName, email, password)

            if (success) {
                //  Guardar también en Firestore
                val userData = hashMapOf(
                    "full_name" to fullName,
                    "email" to email,
                    "password" to password,
                    "created_at" to System.currentTimeMillis()
                )

                firestore.collection("users")
                    .add(userData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usuario guardado en Firestore", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
                    }

                Toast.makeText(this, "Registro exitoso!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Error: El usuario ya existe", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(fullName: String, email: String, password: String): Boolean {
        etFullName.error = null
        etEmail.error = null
        etPassword.error = null

        if (fullName.isEmpty()) {
            etFullName.error = "El nombre completo es requerido"
            return false
        } else if (fullName.length < 7) {
            etFullName.error = "El nombre debe tener al menos 7 caracteres"
            return false
        }

        if (email.isEmpty()) {
            etEmail.error = "El correo electrónico es requerido"
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Formato de correo electrónico inválido"
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "La contraseña es requerida"
            return false
        } else if (password.length < 8) {
            etPassword.error = "La contraseña debe tener al menos 8 caracteres"
            return false
        } else if (!password.matches(Regex("^(?=.*[a-zA-Z])(?=.*\\d).+$"))) {
            etPassword.error = "La contraseña debe ser alfanumérica"
            return false
        }

        return true
    }
}
