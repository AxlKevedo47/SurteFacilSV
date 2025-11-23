package com.example.surtefacilsv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Date
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent

class CarritoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var carritoAdapter: CarritoAdapter
    private lateinit var textViewTotal: TextView
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        recyclerView = findViewById(R.id.recyclerViewCarrito)
        textViewTotal = findViewById(R.id.textViewTotal)
        val btnRealizarPedido = findViewById<Button>(R.id.btnRealizarPedido)
        val btnVerPedidos = findViewById<Button>(R.id.btnVerPedidos) // NUEVO

        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        updateTotal()

        btnRealizarPedido.setOnClickListener {
            realizarPedido()
        }

        // NUEVO: Botón para ver pedidos anteriores
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
        }
        recyclerView.adapter = carritoAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun updateTotal() {
        val total = Carrito.getTotal()
        textViewTotal.text = String.format("Total: $%.2f", total)
    }

    private fun realizarPedido() {
        if (Carrito.productos.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            return
        }

        println("🛒 DEBUG: Productos en carrito: ${Carrito.productos.size}")
        println("🛒 DEBUG: Total del carrito: ${Carrito.getTotal()}")

        val nuevoPedido = Pedido(
            id = UUID.randomUUID().toString(),
            productos = ArrayList(Carrito.productos),
            total = Carrito.getTotal(),
            fecha = Date()
        )

        println("🛒 DEBUG: Pedido creado: ${nuevoPedido.id}")

        // GUARDA EN FIREBASE
        firestore.collection("pedidos")
            .add(nuevoPedido)
            .addOnSuccessListener { documentReference ->
                println("✅ DEBUG: Pedido guardado en Firebase con ID: ${documentReference.id}")
                Toast.makeText(this, "Pedido realizado con éxito", Toast.LENGTH_LONG).show()
                Carrito.limpiarCarrito()
                finish()
            }
            .addOnFailureListener { e ->
                println("❌ DEBUG: Error al guardar pedido: ${e.message}")
                Toast.makeText(this, "Error al realizar pedido: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Agregar soporte para el botón de retroceso en la ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}