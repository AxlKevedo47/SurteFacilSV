package com.example.surtefacilsv

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surtefacilsv.adapters.ProductAdapter
import com.example.surtefacilsv.models.Product
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SellerProductListActivity : AppCompatActivity() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var fabAddProduct: FloatingActionButton
    private lateinit var searchView: SearchView
    private lateinit var productAdapter: ProductAdapter
    private var productList = mutableListOf<Product>()
    private var displayedList = mutableListOf<Product>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_product_list)

        initViews()
        setupRecyclerView()
        setupListeners()
        loadProductsFromFirestore()
    }

    private fun initViews() {
        rvProducts = findViewById(R.id.rvProducts)
        fabAddProduct = findViewById(R.id.fabAddProduct)
        searchView = findViewById(R.id.searchView)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(displayedList)
        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = productAdapter
    }

    private fun setupListeners() {
        fabAddProduct.setOnClickListener {
            startActivity(Intent(this, AddEditProductActivity::class.java))
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterProducts(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText)
                return true
            }
        })
    }

    private fun loadProductsFromFirestore() {
        val sellerId = auth.currentUser?.uid
        if (sellerId == null) {
            Toast.makeText(this, "Error: No se pudo identificar al vendedor.", Toast.LENGTH_LONG).show()
            return
        }

        firestore.collection("products")
            .whereEqualTo("sellerId", sellerId)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("SellerProductList", "Listen failed.", e)
                    Toast.makeText(this, "Error al cargar los productos.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                productList.clear()
                for (doc in snapshots!!) {
                    val product = doc.toObject(Product::class.java)
                    productList.add(product)
                }
                // Initially, show all products
                filterProducts(searchView.query.toString())
            }
    }

    private fun filterProducts(query: String?) {
        displayedList.clear()
        if (query.isNullOrBlank()) {
            displayedList.addAll(productList)
        } else {
            val filtered = productList.filter {
                it.name.contains(query, ignoreCase = true)
            }
            displayedList.addAll(filtered)
        }
        productAdapter.notifyDataSetChanged()
    }
}
