package com.example.surtefacilsv

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate

class RegisterActivity : AppCompatActivity() {

    private lateinit var btnSeller: Button
    private lateinit var btnBuyer: Button
    private lateinit var btnThemeToggle: ImageButton

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        initViews()
        updateThemeIcon()
        setupListeners()
    }

    private fun initViews() {
        btnSeller = findViewById(R.id.btnSeller)
        btnBuyer = findViewById(R.id.btnBuyer)
        btnThemeToggle = findViewById(R.id.btnThemeToggle)
    }

    private fun setupListeners() {
        btnSeller.setOnClickListener {
            startActivity(Intent(this, SellerRegisterActivity::class.java))
        }

        btnBuyer.setOnClickListener {
            startActivity(Intent(this, BuyerRegisterActivity::class.java))
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
}
