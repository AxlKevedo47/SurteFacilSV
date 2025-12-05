package com.example.surtefacilsv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class PedidosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var btnBack: ImageView
    private lateinit var pedidosAdapter: PedidosAdapter
    private var pedidosList = mutableListOf<Pedido>()
    private lateinit var firestore: FirebaseFirestore
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedidos)

        // Configurar toolbar - sin action bar porque usamos header personalizado
        supportActionBar?.hide()

        recyclerView = findViewById(R.id.recyclerViewPedidos)
        emptyView = findViewById(R.id.emptyView)
        btnBack = findViewById(R.id.btnBack)
        recyclerView.layoutManager = LinearLayoutManager(this)

        firestore = FirebaseFirestore.getInstance()

        pedidosAdapter = PedidosAdapter(pedidosList)
        recyclerView.adapter = pedidosAdapter

        // Configurar botón de regresar
        btnBack.setOnClickListener {
            finish()
        }

        // CARGAR PEDIDOS DESDE FIREBASE
        loadPedidos()
    }

    private fun loadPedidos() {
        listener = firestore.collection("pedidos")
            .orderBy("fecha", Query.Direction.DESCENDING) // Ordenar por fecha descendente
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
                        println("DEBUG: Pedido cargado - ID: ${pedido.id}, Total: ${pedido.total}, Estado: ${pedido.estado}")
                    } catch (e: Exception) {
                        println("DEBUG: Error al convertir documento a Pedido: ${e.message}")
                    }
                }

                // Mostrar/ocultar mensaje de lista vacía
                if (pedidosList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                }

                pedidosAdapter.notifyDataSetChanged()
                println("DEBUG: Total de pedidos cargados: ${pedidosList.size}")
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}