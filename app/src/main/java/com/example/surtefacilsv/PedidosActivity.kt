package com.example.surtefacilsv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PedidosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var pedidosAdapter: PedidosAdapter
    private var pedidosList = mutableListOf<Pedido>()
    private lateinit var firestore: FirebaseFirestore
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedidos)

        // Configurar toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mis Pedidos"

        recyclerView = findViewById(R.id.recyclerViewPedidos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        firestore = FirebaseFirestore.getInstance()

        pedidosAdapter = PedidosAdapter(pedidosList)
        recyclerView.adapter = pedidosAdapter

        // CARGAR PEDIDOS DESDE FIREBASE
        loadPedidos()
    }

    private fun loadPedidos() {
        listener = firestore.collection("pedidos")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error al cargar pedidos: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                pedidosList.clear()
                snapshots?.forEach { doc ->
                    try {
                        val pedido = doc.toObject(Pedido::class.java)
                        pedidosList.add(pedido)
                        println("DEBUG: Pedido cargado - ID: ${pedido.id}, Total: ${pedido.total}")
                    } catch (e: Exception) {
                        println("DEBUG: Error al convertir documento a Pedido: ${e.message}")
                    }
                }
                pedidosAdapter.notifyDataSetChanged()
                println("DEBUG: Total de pedidos cargados: ${pedidosList.size}")
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}