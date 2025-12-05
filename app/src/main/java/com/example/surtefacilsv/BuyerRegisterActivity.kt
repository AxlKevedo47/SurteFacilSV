package com.example.surtefacilsv

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class BuyerRegisterActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegisterBuyer: Button
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_register)

        initViews()
        userRepository = UserRepository(this)

        btnRegisterBuyer.setOnClickListener {
            attemptBuyerRegistration()
        }
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegisterBuyer = findViewById(R.id.btnRegisterBuyer)
    }

    private fun attemptBuyerRegistration() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (validateInputs(email, password, confirmPassword)) {
            btnRegisterBuyer.isEnabled = false
            userRepository.registerUser(
                email, email, password, "Soy un comprador"
            ) { success, error ->
                if (success) {
                    Toast.makeText(this, "Registro de comprador exitoso!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error en el registro: ${error ?: "Error desconocido"}", Toast.LENGTH_LONG).show()
                    btnRegisterBuyer.isEnabled = true
                }
            }
        }
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
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
        if (password != confirmPassword) {
            etConfirmPassword.error = "Las contraseñas no coinciden"
            return false
        }
        return true
    }
}
