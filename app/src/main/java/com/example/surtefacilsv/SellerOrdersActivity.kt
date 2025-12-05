package com.example.surtefacilsv

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surtefacilsv.adapters.OrderAdapter
import com.example.surtefacilsv.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SellerOrdersActivity : AppCompatActivity() {

    private lateinit var rvOrders: RecyclerView
    private lateinit var rgOrderStatusFilter: RadioGroup
    private lateinit var orderAdapter: OrderAdapter
    private var allOrders = mutableListOf<Order>()
    private var displayedOrders = mutableListOf<Order>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_orders)

        initViews()
        setupRecyclerView()
        setupListeners()
        loadOrdersFromFirestore()
    }

    private fun initViews() {
        rvOrders = findViewById(R.id.rvOrders)
        rgOrderStatusFilter = findViewById(R.id.rgOrderStatusFilter)
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(displayedOrders) { order ->
            val intent = Intent(this, OrderDetailsActivity::class.java)
            intent.putExtra(OrderDetailsActivity.EXTRA_ORDER_ID, order.id)
            startActivity(intent)
        }
        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = orderAdapter
    }

    private fun setupListeners() {
        rgOrderStatusFilter.setOnCheckedChangeListener { _, checkedId ->
            filterOrders()
        }
    }

    private fun loadOrdersFromFirestore() {
        val sellerId = auth.currentUser?.uid
        if (sellerId == null) {
            Toast.makeText(this, "Error: Vendedor no autenticado.", Toast.LENGTH_LONG).show()
            return
        }

        firestore.collection("orders")
            .whereEqualTo("sellerId", sellerId)
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("SellerOrdersActivity", "Listen failed.", e)
                    Toast.makeText(this, "Error al cargar los pedidos.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                allOrders.clear()
                for (doc in snapshots!!) {
                    val order = doc.toObject(Order::class.java)
                    allOrders.add(order)
                }
                filterOrders()
            }
    }

    private fun filterOrders() {
        val selectedStatus = if (rgOrderStatusFilter.checkedRadioButtonId == R.id.rbPending) {
            "Pendiente"
        } else {
            "Entregado"
        }

        displayedOrders.clear()
        displayedOrders.addAll(allOrders.filter { it.status == selectedStatus })
        orderAdapter.updateList(displayedOrders)
    }
}
