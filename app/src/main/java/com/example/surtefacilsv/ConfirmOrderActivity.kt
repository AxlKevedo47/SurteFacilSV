package com.example.surtefacilsv

import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surtefacilsv.adapters.OrderDetailProductAdapter
import com.example.surtefacilsv.managers.CartManager
import com.example.surtefacilsv.models.Order
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale
import java.util.UUID

class ConfirmOrderActivity : AppCompatActivity() {

    private lateinit var rvOrderSummary: RecyclerView
    private lateinit var tvTotalOrderSummary: TextView
    private lateinit var etDeliveryAddress: EditText
    private lateinit var cbCashOnDelivery: CheckBox

    private lateinit var btnSendOrder: Button

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private var progressDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_order)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        initViews()
        setupRecyclerView()
        displayOrderSummary()
        loadBusinessData()
        setupListeners()
    }

    private fun initViews() {
        rvOrderSummary = findViewById(R.id.rvOrderSummary)
        tvTotalOrderSummary = findViewById(R.id.tvTotalOrderSummary)
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress)
        cbCashOnDelivery = findViewById(R.id.cbCashOnDelivery)

        btnSendOrder = findViewById(R.id.btnSendOrder)
    }

    private fun setupRecyclerView() {
        CartManager.cartItems.observe(this) { items ->
            val adapter = OrderDetailProductAdapter(items)
            rvOrderSummary.layoutManager = LinearLayoutManager(this)
            rvOrderSummary.adapter = adapter
        }
    }

    private fun displayOrderSummary() {
        CartManager.subtotal.observe(this) { subtotal ->
            tvTotalOrderSummary.text = String.format(Locale.US, "Total: $%.2f", subtotal)
        }
    }

    private fun loadBusinessData() {
        val sellerId = CartManager.cartItems.value?.firstOrNull()?.sellerId
        if (sellerId != null) {
            firestore.collection("users").document(sellerId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val businessName = document.getString("businessName") ?: (document.getString("name") ?: "Nombre no disponible")
                        val businessAddress = document.getString("address") ?: "Dirección no disponible"

                    } else {

                    }
                }
                .addOnFailureListener { 

                }
        }
    }

    private fun setupListeners() {
        btnSendOrder.setOnClickListener { sendOrder() }
    }

    private fun sendOrder() {
        if (!isNetworkAvailable()) {
            showErrorDialog("No hay conexión a internet.")
            return
        }
        val address = etDeliveryAddress.text.toString().trim()
        if (address.isEmpty()) {
            etDeliveryAddress.error = "La dirección es requerida"
            return
        }
        if (!cbCashOnDelivery.isChecked) {
            Toast.makeText(this, "Por favor, confirma el método de pago.", Toast.LENGTH_SHORT).show()
            return
        }
        val customerId = sharedPreferences.getString("user_uid", null)
        val customerName = sharedPreferences.getString("user_name", "Cliente")
        val cartItems = CartManager.cartItems.value
        if (customerId == null || cartItems.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Tu carrito está vacío o no has iniciado sesión.", Toast.LENGTH_LONG).show()
            return
        }
        showProgressDialog()
        val ordersBySeller = cartItems.groupBy { it.sellerId }
        val batch = firestore.batch()
        for ((sellerId, items) in ordersBySeller) {
            val orderId = UUID.randomUUID().toString()
            val orderRef = firestore.collection("orders").document(orderId)
            val totalForThisOrder = items.sumOf { it.price * it.quantity }
            val order = Order(id = orderId, customerId = customerId, customerName = customerName ?: "Cliente", customerAddress = address, sellerId = sellerId, items = items, totalPrice = totalForThisOrder, orderDate = Date(), status = "Pendiente")
            batch.set(orderRef, order)
        }
        batch.commit()
            .addOnSuccessListener { dismissProgressDialog(); showSuccessDialog() }
            .addOnFailureListener { e -> dismissProgressDialog(); showErrorDialog(e.message) }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
    
    private fun showProgressDialog() {
        if (progressDialog == null) {
            val builder = AlertDialog.Builder(this)
            builder.setView(R.layout.dialog_progress)
            builder.setCancelable(false)
            progressDialog = builder.create()
        }
        progressDialog?.show()
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("¡Pedido Enviado!")
            .setMessage("Tu pedido ha sido enviado con éxito.")
            .setPositiveButton("Volver") { _, _ ->
                CartManager.clearCart()
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(message: String?) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("No se pudo enviar el pedido. Verifica tu conexión a internet.\nError: ${message ?: "Desconocido"}")
            .setPositiveButton("Volver", null)
            .show()
    }
}
