package com.example.surtefacilsv

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ProductListActivity : AppCompatActivity() {

    private lateinit var adapter: ProductAdapter
    private val productList = mutableListOf<Product>()
    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        val recycler = findViewById<RecyclerView>(R.id.rvProducts)
        val btnAdd = findViewById<FloatingActionButton>(R.id.btnAddProduct) // FLOATING ACTION BUTTON

        adapter = ProductAdapter(
            productList,
            onEditClick = { product -> openDialog(product) },
            onDeleteClick = { product -> deleteProduct(product) }
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // AGREGAR TRY-CATCH AL LISTENER
        try {
            btnAdd.setOnClickListener {
                println("DEBUG: FAB clickeado - Abriendo diálogo")
                openDialog(null)
            }
        } catch (e: Exception) {
            println("ERROR: No se pudo configurar el FAB: ${e.message}")
            Toast.makeText(this, "Error al configurar botón agregar", Toast.LENGTH_SHORT).show()
        }

        loadProducts()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Productos"
        toolbar.setTitleTextColor(getColor(R.color.white))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadProducts() {
        listener = db.collection("products")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error al cargar productos: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                productList.clear()
                snapshots?.forEach { doc ->
                    val product = Product(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        imageUrl = doc.getString("imageUrl") ?: "",
                    )
                    productList.add(product)
                }
                adapter.updateList(productList)
            }
    }

    // ✅ AGREGAR TRY-CATCH AL OPEN DIALOG
    private fun openDialog(productToEdit: Product?) {
        try {
            println("DEBUG: Iniciando openDialog")
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_product, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            val title = dialogView.findViewById<TextView>(R.id.txtDialogTitle)
            val etName = dialogView.findViewById<EditText>(R.id.etProductName)
            val etPrice = dialogView.findViewById<EditText>(R.id.etProductPrice)
            val etImageUrl = dialogView.findViewById<EditText>(R.id.etProductImageUrl)
            val btnSave = dialogView.findViewById<Button>(R.id.btnSaveProduct)

            if (productToEdit != null) {
                title.text = "Editar Producto"
                etName.setText(productToEdit.name)
                etPrice.setText(productToEdit.price.toString())
                etImageUrl.setText(productToEdit.imageUrl)
            } else {
                title.text = "Agregar Producto"
            }

            btnSave.setOnClickListener {
                try {
                    val name = etName.text.toString().trim()
                    val price = etPrice.text.toString().toDoubleOrNull()
                    val url = etImageUrl.text.toString().trim()

                    if (name.isBlank() || price == null || url.isBlank()) {
                        Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    println("DEBUG: Guardando producto - $name, $price, $url")

                    if (productToEdit == null) {
                        val newProduct = hashMapOf(
                            "name" to name,
                            "price" to price,
                            "imageUrl" to url
                        )
                        db.collection("products")
                            .add(newProduct)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al agregar: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        val updates = hashMapOf<String, Any>(
                            "name" to name,
                            "price" to price,
                            "imageUrl" to url
                        )
                        db.collection("products")
                            .document(productToEdit.id)
                            .update(updates)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } catch (e: Exception) {
                    println("ERROR en btnSave: ${e.message}")
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
            println("DEBUG: Diálogo mostrado exitosamente")
        } catch (e: Exception) {
            println("ERROR en openDialog: ${e.message}")
            Toast.makeText(this, "Error al abrir diálogo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteProduct(product: Product) {
        db.collection("products")
            .document(product.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}