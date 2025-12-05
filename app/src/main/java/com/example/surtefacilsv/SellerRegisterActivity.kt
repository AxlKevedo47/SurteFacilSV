package com.example.surtefacilsv

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class SellerRegisterActivity : AppCompatActivity() {

    private lateinit var etBusinessName: EditText
    private lateinit var etOwnerName: EditText
    private lateinit var etDui: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etRubro: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnRegisterSeller: Button
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_register)

        initViews()
        userRepository = UserRepository(this)

        btnRegisterSeller.setOnClickListener {
            attemptSellerRegistration()
        }
    }

    private fun initViews() {
        etBusinessName = findViewById(R.id.etBusinessName)
        etOwnerName = findViewById(R.id.etOwnerName)
        etDui = findViewById(R.id.etDui)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etRubro = findViewById(R.id.etRubro)
        etAddress = findViewById(R.id.etAddress)
        btnRegisterSeller = findViewById(R.id.btnRegisterSeller)
    }

    private fun attemptSellerRegistration() {
        val businessName = etBusinessName.text.toString().trim()
        val ownerName = etOwnerName.text.toString().trim()
        val dui = etDui.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val rubro = etRubro.text.toString().trim()
        val address = etAddress.text.toString().trim()

        if (validateInputs(businessName, ownerName, dui, email, password, confirmPassword, rubro, address)) {
            btnRegisterSeller.isEnabled = false
            userRepository.registerUser(
                ownerName, email, password, "Soy un Vendedor", businessName, dui, rubro, address
            ) { success, error ->
                if (success) {
                    Toast.makeText(this, "Registro de vendedor exitoso!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error en el registro: ${error ?: "Error desconocido"}", Toast.LENGTH_LONG).show()
                    btnRegisterSeller.isEnabled = true
                }
            }
        }
    }

    private fun validateInputs(
        businessName: String,
        ownerName: String,
        dui: String,
        email: String,
        password: String,
        confirmPassword: String,
        rubro: String,
        address: String
    ): Boolean {
        if (businessName.isEmpty()) {
            etBusinessName.error = "El nombre del negocio es requerido"
            return false
        }
        if (ownerName.isEmpty()) {
            etOwnerName.error = "El nombre del propietario es requerido"
            return false
        }
        if (dui.isEmpty()) {
            etDui.error = "El DUI es requerido"
            return false
        }
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
        if (rubro.isEmpty()) {
            etRubro.error = "El rubro es requerido"
            return false
        }
        if (address.isEmpty()) {
            etAddress.error = "La dirección es requerida"
            return false
        }
        return true
    }
}
