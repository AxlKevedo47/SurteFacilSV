package com.example.surtefacilsv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surtefacilsv.adapters.CartAdapter
import com.example.surtefacilsv.managers.CartManager
import com.example.surtefacilsv.models.OrderItem
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var rvCartItems: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var btnConfirmOrder: Button
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val toolbar: Toolbar = findViewById(R.id.toolbarCart)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun initViews() {
        rvCartItems = findViewById(R.id.rvCartItems)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder)
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            mutableListOf(), // Start with an empty list
            onIncrease = { item ->
                val success = CartManager.increaseQuantity(item)
                if (!success) {
                    Toast.makeText(this, "No hay más stock para este producto.", Toast.LENGTH_SHORT).show()
                }
            },
            onDecrease = { item -> CartManager.decreaseQuantity(item) },
            onRemove = { item -> showRemoveConfirmationDialog(item) }
        )
        rvCartItems.layoutManager = LinearLayoutManager(this)
        rvCartItems.adapter = cartAdapter
    }

    private fun setupObservers() {
        CartManager.cartItems.observe(this) { cartItems ->
            // The observer will update the adapter with the correct list
            cartAdapter.updateList(cartItems)
        }

        CartManager.subtotal.observe(this) { subtotal ->
            tvSubtotal.text = String.format(Locale.US, "Subtotal: $%.2f", subtotal)
            btnConfirmOrder.isEnabled = subtotal > 0
        }
    }

    private fun setupListeners() {
        btnConfirmOrder.setOnClickListener {
            startActivity(Intent(this, ConfirmOrderActivity::class.java))
        }
    }

    private fun showRemoveConfirmationDialog(item: OrderItem) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de que quieres eliminar este producto del carrito?")
            .setPositiveButton("Sí") { _, _ -> CartManager.removeProduct(item) }
            .setNegativeButton("No", null)
            .show()
    }
}
