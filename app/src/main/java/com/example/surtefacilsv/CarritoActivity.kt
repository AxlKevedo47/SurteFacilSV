package com.example.surtefacilsv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent

class CarritoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var carritoAdapter: CarritoAdapter
    private lateinit var textViewTotal: TextView
    private lateinit var btnRealizarPedido: Button
    private lateinit var btnVerPedidos: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        // Habilitar botón de regreso en ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Carrito de Compras"

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerViewCarrito)
        textViewTotal = findViewById(R.id.textViewTotal)
        btnRealizarPedido = findViewById(R.id.btnRealizarPedido)
        btnVerPedidos = findViewById(R.id.btnVerPedidos)

        setupRecyclerView()
        updateTotal()

        btnRealizarPedido.setOnClickListener {
            realizarPedido()
        }

        // Botón para ver pedidos anteriores
        btnVerPedidos.setOnClickListener {
            val intent = Intent(this, PedidosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        carritoAdapter = CarritoAdapter(Carrito.productos) { productoCarrito ->
            Carrito.removerProducto(productoCarrito)
            carritoAdapter.notifyDataSetChanged()
            updateTotal()

            if (Carrito.productos.isEmpty()) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = carritoAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observar cambios en el adapter para actualizar el total
        carritoAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                updateTotal()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                updateTotal()
            }
        })
    }

    private fun updateTotal() {
        val total = Carrito.getTotal()
        textViewTotal.text = String.format("$%.2f", total)

        // Deshabilitar botón si el carrito está vacío
        btnRealizarPedido.isEnabled = Carrito.productos.isNotEmpty()
    }

    private fun realizarPedido() {
        if (Carrito.productos.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            return
        }

        println("🛒 DEBUG: Productos en carrito: ${Carrito.productos.size}")
        println("🛒 DEBUG: Total del carrito: ${Carrito.getTotal()}")

        // Navegar a la pantalla de confirmación de pedido
        val intent = Intent(this, ConfirmacionPedidoActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Actualizar la lista cuando se regresa a esta actividad
        carritoAdapter.notifyDataSetChanged()
        updateTotal()
    }

    // Agregar soporte para el botón de retroceso en la ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}