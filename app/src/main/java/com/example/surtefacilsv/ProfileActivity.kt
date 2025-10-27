package com.example.surtefacilsv

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvSyncStatus: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Configurar toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mi Perfil"

        initViews()
        loadUserData()
        setupListeners()
    }

    private fun initViews() {
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvSyncStatus = findViewById(R.id.tvSyncStatus)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        databaseHelper = DatabaseHelper(this)
    }

    private fun loadUserData() {
        val userEmail = sharedPreferences.getString("user_email", "")

        if (!userEmail.isNullOrEmpty()) {
            val userName = databaseHelper.getUserByEmail(userEmail) ?: "Usuario no encontrado"

            tvFullName.text = userName
            tvEmail.text = userEmail
            tvSyncStatus.text = "Datos sincronizados"
        } else {
            tvFullName.text = "Error al cargar datos"
            tvEmail.text = "Error al cargar datos"
            tvSyncStatus.text = "No se pudieron cargar los datos"
        }
    }

    private fun setupListeners() {
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cuando vuelva de editar
        loadUserData()
    }
}