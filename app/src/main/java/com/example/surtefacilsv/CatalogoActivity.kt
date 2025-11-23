package com.example.surtefacilsv

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class CatalogoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var catalogoAdapter: CatalogoAdapter
    private val productoList = mutableListOf<Product>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo)

        recyclerView = findViewById(R.id.recyclerViewCatalogo)
        setupRecyclerView()
        loadProductsFromFirebase()
    }

    private fun setupRecyclerView() {
        catalogoAdapter = CatalogoAdapter(productoList) { product ->
            // Agregar al carrito
            Carrito.agregarProducto(product, 1)
            Toast.makeText(this, "${product.name} agregado al carrito", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = catalogoAdapter
    }

    private fun loadProductsFromFirebase() {
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                productoList.clear()
                for (document in result) {
                    val product = Product(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        price = document.getDouble("price") ?: 0.0,
                        imageUrl = document.getString("imageUrl") ?: ""
                    )
                    productoList.add(product)
                }
                catalogoAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading products: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}