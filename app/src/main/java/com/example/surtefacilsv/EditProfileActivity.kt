package com.example.surtefacilsv

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Handler

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnToggleNewPassword: ImageButton
    private lateinit var btnToggleConfirmPassword: ImageButton
    private lateinit var btnSaveChanges: Button
    private lateinit var tvStatus: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var firestore: FirebaseFirestore

    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar Perfil"

        initViews()
        loadCurrentUserData()
        setupListeners()
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnToggleNewPassword = findViewById(R.id.btnToggleNewPassword)
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        tvStatus = findViewById(R.id.tvStatus)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        databaseHelper = DatabaseHelper(this)
        firestore = FirebaseFirestore.getInstance()
    }

    private fun loadCurrentUserData() {
        val userEmail = sharedPreferences.getString("user_email", "")

        if (!userEmail.isNullOrEmpty()) {
            val userName = databaseHelper.getUserByEmail(userEmail) ?: ""

            etFullName.setText(userName)
            etEmail.setText(userEmail)
        } else {
            tvStatus.text = "Error al cargar datos del usuario"
            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }

    private fun setupListeners() {

        btnToggleNewPassword.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            togglePasswordVisibility(etNewPassword, btnToggleNewPassword, isNewPasswordVisible)
        }

        btnToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(etConfirmPassword, btnToggleConfirmPassword, isConfirmPasswordVisible)
        }

        btnSaveChanges.setOnClickListener {
            attemptSaveChanges()
        }
    }

    private fun togglePasswordVisibility(editText: EditText, button: ImageButton, isVisible: Boolean) {
        if (isVisible) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            button.setImageResource(R.drawable.ic_visibility_off)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            button.setImageResource(R.drawable.ic_visibility)
        }

        editText.setSelection(editText.text.length)
    }

    private fun attemptSaveChanges() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (validateInputs(fullName, email, newPassword, confirmPassword)) {
            saveChanges(fullName, email, newPassword)
        }
    }

    private fun validateInputs(fullName: String, email: String, newPassword: String, confirmPassword: String): Boolean {

        etFullName.error = null
        etEmail.error = null
        etNewPassword.error = null
        etConfirmPassword.error = null

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

        if (newPassword.isNotEmpty()) {
            if (newPassword.length < 8) {
                etNewPassword.error = "La contraseña debe tener al menos 8 caracteres"
                return false
            } else if (!newPassword.matches(Regex("^(?=.*[a-zA-Z])(?=.*\\d).+\$"))) {
                etNewPassword.error = "La contraseña debe ser alfanumérica"
                return false
            }

            if (newPassword != confirmPassword) {
                etConfirmPassword.error = "Las contraseñas no coinciden"
                return false
            }
        }

        return true
    }

    private fun saveChanges(fullName: String, email: String, newPassword: String) {
        val oldEmail = sharedPreferences.getString("user_email", "")

        if (oldEmail.isNullOrEmpty()) {
            showStatus("Error: No se pudo identificar al usuario", false)
            return
        }

        try {

            val success = if (newPassword.isNotEmpty()) {
                databaseHelper.updateUser(oldEmail, fullName, email, newPassword)
            } else {
                databaseHelper.updateUserWithoutPassword(oldEmail, fullName, email)
            }

            if (success) {

                updateUserInFirebase(oldEmail, fullName, email, newPassword)

                updateSharedPreferences(email, fullName)

                showStatus("Cambios guardados exitosamente", true)

                Handler().postDelayed({
                    finish()
                }, 2000)

            } else {
                showStatus("Error al guardar cambios en la base de datos local", false)
            }

        } catch (e: Exception) {
            showStatus("Error: ${e.message}", false)
        }
    }

    private fun updateUserInFirebase(oldEmail: String, fullName: String, newEmail: String, newPassword: String) {
        val userData = hashMapOf(
            "full_name" to fullName,
            "email" to newEmail
        )
        if (newPassword.isNotEmpty()) {
            userData["password"] = newPassword
        }

        firestore.collection("users")
            .whereEqualTo("email", oldEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    firestore.collection("users").document(document.id)
                        .update(userData as Map<String, Any>)
                        .addOnSuccessListener {
                            println("Usuario actualizado en Firestore")
                        }
                        .addOnFailureListener { e ->
                            println("Error al actualizar en Firestore: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                println("Error al buscar usuario en Firestore: $e")
            }
    }

    private fun updateSharedPreferences(email: String, fullName: String) {
        sharedPreferences.edit().apply {
            putString("user_email", email)
            putString("user_name", fullName)
            apply()
        }
    }

    private fun showStatus(message: String, isSuccess: Boolean) {
        tvStatus.text = message
        if (isSuccess) {
            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}