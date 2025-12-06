package com.example.surtefacilsv

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surtefacilsv.adapters.BuyerProductAdapter
import com.example.surtefacilsv.managers.CartManager
import com.example.surtefacilsv.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeActivity : AppCompatActivity() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var productAdapter: BuyerProductAdapter
    private var allProducts = mutableListOf<Product>()
    private var displayedProducts = mutableListOf<Product>()

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        applySavedTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Catálogo"

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)

        initViews()
        setupRecyclerView()
        setupListeners()
        loadProducts()
    }

    private fun initViews() {
        rvProducts = findViewById(R.id.rvProductsHome)
        searchView = findViewById(R.id.searchViewHome)
    }

    private fun setupRecyclerView() {
        productAdapter = BuyerProductAdapter(displayedProducts) { product ->
            val success = CartManager.addProduct(product)
            if (success) {
                Toast.makeText(this, "${product.name} agregado al carrito", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No hay más stock para este producto.", Toast.LENGTH_SHORT).show()
            }
        }
        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = productAdapter
    }

    private fun setupListeners() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterProducts(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText)
                return true
            }
        })
    }

    private fun loadProducts() {
        firestore.collection("products")
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("HomeActivity", "Listen failed.", e)
                    Toast.makeText(this, "Error al cargar productos.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                allProducts.clear()
                for (doc in snapshots!!) {
                    val product = doc.toObject(Product::class.java)
                    allProducts.add(product)
                }
                filterProducts(searchView.query.toString())
            }
    }

    private fun filterProducts(query: String?) {
        displayedProducts.clear()
        if (query.isNullOrBlank()) {
            displayedProducts.addAll(allProducts)
        } else {
            displayedProducts.addAll(
                allProducts.filter { it.name.contains(query, ignoreCase = true) }
            )
        }
        productAdapter.updateList(displayedProducts)
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_cart -> {
                startActivity(Intent(this, CartActivity::class.java))
                true
            }
            R.id.action_theme -> {
                toggleTheme()
                true
            }
            R.id.action_logout -> {
                showLogoutConfirmation()
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
        recreate()
    }

    private fun applySavedTheme() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedTheme = prefs.getString("app_theme", "light") ?: "light"
        applyTheme(savedTheme)
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
        FirebaseAuth.getInstance().signOut()

        sharedPreferences.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
