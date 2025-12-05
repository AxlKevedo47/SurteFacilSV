package com.example.surtefacilsv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surtefacilsv.adapters.OrderDetailProductAdapter
import com.example.surtefacilsv.models.Order
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var tvCustomerName: TextView
    private lateinit var tvCustomerAddress: TextView
    private lateinit var rvOrderProducts: RecyclerView
    private lateinit var tvTotalOrderPrice: TextView
    private lateinit var btnMarkAsDelivered: Button

    private lateinit var productAdapter: OrderDetailProductAdapter
    private var order: Order? = null

    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)

        initViews()
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID)

        if (orderId == null) {
            Toast.makeText(this, "Error: ID de pedido no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerView()
        loadOrderDetails(orderId)
        setupListeners()
    }

    private fun initViews() {
        tvCustomerName = findViewById(R.id.tvCustomerNameDetail)
        tvCustomerAddress = findViewById(R.id.tvCustomerAddressDetail)
        rvOrderProducts = findViewById(R.id.rvOrderProducts)
        tvTotalOrderPrice = findViewById(R.id.tvTotalOrderPrice)
        btnMarkAsDelivered = findViewById(R.id.btnMarkAsDelivered)
    }

    private fun setupRecyclerView() {
        productAdapter = OrderDetailProductAdapter(emptyList())
        rvOrderProducts.layoutManager = LinearLayoutManager(this)
        rvOrderProducts.adapter = productAdapter
    }

    private fun loadOrderDetails(orderId: String) {
        firestore.collection("orders").document(orderId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    order = document.toObject(Order::class.java)
                    order?.let { displayOrderDetails(it) }
                } else {
                    Toast.makeText(this, "Pedido no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar el pedido: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayOrderDetails(order: Order) {
        tvCustomerName.text = "Nombre: ${order.customerName}"
        tvCustomerAddress.text = "Dirección: ${order.customerAddress}"
        tvTotalOrderPrice.text = String.format(Locale.US, "Total del Pedido: $%.2f", order.totalPrice)

        productAdapter = OrderDetailProductAdapter(order.items)
        rvOrderProducts.adapter = productAdapter

        if (order.status == "Entregado") {
            btnMarkAsDelivered.text = "Pedido ya Entregado"
            btnMarkAsDelivered.isEnabled = false
        } else {
            btnMarkAsDelivered.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        btnMarkAsDelivered.setOnClickListener {
            order?.let { markOrderAsDelivered(it.id) }
        }
    }

    private fun markOrderAsDelivered(orderId: String) {
        firestore.collection("orders").document(orderId)
            .update("status", "Entregado")
            .addOnSuccessListener {
                Toast.makeText(this, "Pedido marcado como entregado", Toast.LENGTH_SHORT).show()
                btnMarkAsDelivered.text = "Pedido ya Entregado"
                btnMarkAsDelivered.isEnabled = false
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar el pedido: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
