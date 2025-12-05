package com.example.surtefacilsv

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvUserType: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnOrderHistory: Button
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var btnLogout: Button

    private lateinit var sharedPreferences: SharedPreferences
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Perfil y Configuración"

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)

        initViews()
        loadUserData()
        setupListeners()
    }

    private fun initViews() {
        tvFullName = findViewById(R.id.tvFullNameProfile)
        tvEmail = findViewById(R.id.tvEmailProfile)
        tvUserType = findViewById(R.id.tvUserTypeProfile)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnOrderHistory = findViewById(R.id.btnOrderHistory)
        switchNotifications = findViewById(R.id.switchNotifications)
        switchDarkMode = findViewById(R.id.switchDarkMode)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            tvEmail.text = currentUser.email
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userType = document.getString("user_type")
                        val name = document.getString("full_name")

                        tvFullName.text = name
                        tvUserType.text = "Tipo: $userType"

                        if (userType == "Soy un Vendedor") {
                            btnOrderHistory.visibility = View.GONE
                        }
                    }
                }
        }
    }

    private fun setupListeners() {
        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        btnOrderHistory.setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }

        switchDarkMode.isChecked = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val newTheme = if (isChecked) "dark" else "light"
            sharedPreferences.edit().putString("app_theme", newTheme).apply()
            applyTheme(newTheme)
        }
        
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Notificaciones ${if(isChecked) "activadas" else "desactivadas"}", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }
    
    private fun applyTheme(theme: String) {
        when (theme) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí") { _, _ -> logout() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        auth.signOut()
        sharedPreferences.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
