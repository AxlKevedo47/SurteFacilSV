package com.example.surtefacilsv

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar

class DashboardClienteActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvStats: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "SurteFacilSV"

        initViews()
        loadUserData()
        setupClickListeners()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvStats = findViewById(R.id.tvStats)
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
    }

    private fun loadUserData() {
        val userEmail = sharedPreferences.getString("user_email", "")
        val userName = sharedPreferences.getString("user_name", "Usuario")

        tvWelcome.text = getString(R.string.welcome_message, userName)
        tvUserEmail.text = userEmail

        loadInitialStats()
    }

    private fun loadInitialStats() {
        tvStats.text = "* Proveedores registrados: 0\n* Pedidos activos: 0\n* Sincronización: Al día"
    }

    private fun setupClickListeners() {
        val cardProviders: LinearLayout = findViewById(R.id.cardProviders)
        val cardOrders: LinearLayout = findViewById(R.id.cardOrders)
        val cardProducts: LinearLayout = findViewById(R.id.cardProducts)
        val cardPurchases: LinearLayout = findViewById(R.id.cardPurchases)

        cardProviders.setOnClickListener {
            Toast.makeText(this, "Navegando a Gestión de Proveedores", Toast.LENGTH_SHORT).show()
        }

        cardOrders.setOnClickListener {
            startActivity(Intent(this, PedidosActivity::class.java))
        }

        cardProducts.setOnClickListener {
            startActivity(Intent(this, CatalogoActivity::class.java))
        }

        cardPurchases.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }
    }

    // CONFIGURAR MENÚ EN TOOLBAR
    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_theme -> {
                toggleTheme()
                true
            }
            R.id.action_sync -> {
                syncData()
                true
            }
            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }
            R.id.action_map -> {
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleTheme() {
        val currentTheme = sharedPreferences.getString("app_theme", "light")
        val newTheme = if (currentTheme == "light") "dark" else "light"
        sharedPreferences.edit().putString("app_theme", newTheme).apply()
        applyTheme(newTheme)
        Toast.makeText(this, "Tema: ${if (newTheme == "dark") "Oscuro" else "Claro"}", Toast.LENGTH_SHORT).show()
        recreate()
    }

    private fun applyTheme(theme: String) {
        when (theme) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun syncData() {
        Toast.makeText(this, "Sincronizando datos...", Toast.LENGTH_SHORT).show()
        // Aquí puedes agregar la lógica de sincronización
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí") { dialog, which ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        val editor = sharedPreferences.edit()
        editor.remove("user_email")
        editor.remove("user_name")
        editor.remove("remember_me")
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}