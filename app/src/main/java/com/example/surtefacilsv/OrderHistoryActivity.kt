package com.example.surtefacilsv

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surtefacilsv.adapters.OrderHistoryAdapter
import com.example.surtefacilsv.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var rvOrderHistory: RecyclerView
    private lateinit var rgOrderStatusFilter: RadioGroup
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter
    private var allOrders = mutableListOf<Order>()
    private var displayedOrders = mutableListOf<Order>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        val toolbar: Toolbar = findViewById(R.id.toolbarOrderHistory)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        setupRecyclerView()
        setupListeners()
        loadOrderHistory()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun initViews() {
        rvOrderHistory = findViewById(R.id.rvOrderHistory)
        rgOrderStatusFilter = findViewById(R.id.rgOrderStatusFilterHistory)
    }

    private fun setupRecyclerView() {
        orderHistoryAdapter = OrderHistoryAdapter(displayedOrders) { order ->
            val intent = Intent(this, OrderDetailsActivity::class.java)
            intent.putExtra(OrderDetailsActivity.EXTRA_ORDER_ID, order.id)
            startActivity(intent)
        }
        rvOrderHistory.layoutManager = LinearLayoutManager(this)
        rvOrderHistory.adapter = orderHistoryAdapter
    }

    private fun setupListeners() {
        rgOrderStatusFilter.setOnCheckedChangeListener { _, _ ->
            filterOrders()
        }
    }

    private fun loadOrderHistory() {
        val customerId = auth.currentUser?.uid
        if (customerId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_LONG).show()
            return
        }

        firestore.collection("orders")
            .whereEqualTo("customerId", customerId)
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("OrderHistoryActivity", "Listen failed.", e)
                    Toast.makeText(this, "Error al cargar el historial de pedidos.", Toast.LENGTH_SHORT).show()
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
        val selectedStatus = if (rgOrderStatusFilter.checkedRadioButtonId == R.id.rbPendingHistory) {
            "Pendiente"
        } else {
            "Entregado"
        }

        displayedOrders.clear()
        displayedOrders.addAll(allOrders.filter { it.status == selectedStatus })
        orderHistoryAdapter.updateList(displayedOrders)
    }
}
