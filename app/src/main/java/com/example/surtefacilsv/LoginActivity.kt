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
import androidx.appcompat.app.AppCompatDelegate

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbRemember: CheckBox
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnThemeToggle: ImageButton

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    // Constantes para los temas
    companion object {
        private const val PREF_THEME = "app_theme"
        private const val THEME_LIGHT = "light"
        private const val THEME_DARK = "dark"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar el tema guardado ANTES de crear la vista
        applySavedTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        databaseHelper = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

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
        btnLogin.setOnClickListener {
            attemptLogin()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }

        btnThemeToggle.setOnClickListener {
            toggleTheme()
        }
    }

    private fun toggleTheme() {
        val currentTheme = sharedPreferences.getString(PREF_THEME, THEME_LIGHT)
        val newTheme = if (currentTheme == THEME_LIGHT) THEME_DARK else THEME_LIGHT

        sharedPreferences.edit().putString(PREF_THEME, newTheme).apply()

        applyTheme(newTheme)
        updateThemeIcon()

        Toast.makeText(this, "Tema: ${if (newTheme == THEME_DARK) "Oscuro" else "Claro"}", Toast.LENGTH_SHORT).show()
    }

     private fun applySavedTheme() {
        val savedTheme = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString(PREF_THEME, THEME_LIGHT) ?: THEME_LIGHT
        applyTheme(savedTheme)
    }


    private fun applyTheme(theme: String) {
        when (theme) {
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    // Actualizar icono del botón
    private fun updateThemeIcon() {
        val currentTheme = sharedPreferences.getString(PREF_THEME, THEME_LIGHT)
        val iconRes = if (currentTheme == THEME_DARK) {
            R.drawable.ic_day  // Mostrar sol si está en modo oscuro
        } else {
            R.drawable.ic_night // Mostrar luna si está en modo claro
        }
        btnThemeToggle.setImageResource(iconRes)
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (validateInputs(email, password)) {
            if (databaseHelper.checkUser(email, password)) {
                try {
                    if (cbRemember.isChecked) {
                        saveUserCredentials(email, password)
                    } else {
                        clearUserCredentials()
                    }

                    // Intentar obtener el nombre del usuario
                    val userName = databaseHelper.getUserByEmail(email) ?: "Usuario"

                    // Guardar datos para el Home
                    val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    prefs.edit().apply {
                        putString("user_email", email)
                        putString("user_name", userName)
                        apply()
                    }

                    // Navegar al Dashboard del Cliente
                    val intent = Intent(this, DashboardClienteActivity::class.java)
                    startActivity(intent)
                    finish()

                    Toast.makeText(this, "Login exitoso!", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("LOGIN_ERROR", "Error al navegar al Dashboard", e)
                }
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserCredentials(email: String, password: String) {
        sharedPreferences.edit().apply {
            putString("saved_email", email)
            putString("saved_password", password)
            putBoolean("remember_user", true)
            apply()
        }
    }

    private fun clearUserCredentials() {
        sharedPreferences.edit().remove("saved_email").remove("saved_password").remove("remember_user").apply()
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