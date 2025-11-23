package com.example.surtefacilsv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PedidosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var pedidosAdapter: PedidosAdapter
    private var pedidosList = mutableListOf<Pedido>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedidos)

        recyclerView = findViewById(R.id.recyclerViewPedidos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // TODO: Cargar los pedidos del usuario desde Firebase

        pedidosAdapter = PedidosAdapter(pedidosList)
        recyclerView.adapter = pedidosAdapter
    }
}