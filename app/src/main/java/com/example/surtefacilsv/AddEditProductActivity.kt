package com.example.surtefacilsv

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.surtefacilsv.models.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var tvAddEditTitle: TextView
    private lateinit var ivProductImagePreview: ImageView
    private lateinit var btnUploadImage: Button
    private lateinit var etProductName: EditText
    private lateinit var etProductDescription: EditText
    private lateinit var etProductPrice: EditText
    private lateinit var etProductStock: EditText
    private lateinit var btnSaveProduct: Button
    private lateinit var btnUpdateProduct: Button

    private var selectedImageUri: Uri? = null
    private var selectedImageUrl: String? = null
    private var isEditMode = false
    private var existingProduct: Product? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        const val EXTRA_PRODUCT_ID = "product_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_product)

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        initViews()
        checkEditMode()
        setupListeners()
    }

    private fun initViews() {
        tvAddEditTitle = findViewById(R.id.tvAddEditTitle)
        ivProductImagePreview = findViewById(R.id.ivProductImagePreview)
        btnUploadImage = findViewById(R.id.btnUploadImage)
        etProductName = findViewById(R.id.etProductName)
        etProductDescription = findViewById(R.id.etProductDescription)
        etProductPrice = findViewById(R.id.etProductPrice)
        etProductStock = findViewById(R.id.etProductStock)
        btnSaveProduct = findViewById(R.id.btnSaveProduct)
        btnUpdateProduct = findViewById(R.id.btnUpdateProduct)
    }

    private fun checkEditMode() {
        val productId = intent.getStringExtra(EXTRA_PRODUCT_ID)

        if (productId != null) {
            // Edit mode
            isEditMode = true
            tvAddEditTitle.text = "Editar Producto"
            btnSaveProduct.visibility = View.GONE
            btnUpdateProduct.visibility = View.VISIBLE
            loadProductData(productId)
        } else {
            // Add mode
            isEditMode = false
            tvAddEditTitle.text = "Agregar Producto"
            btnSaveProduct.visibility = View.VISIBLE
            btnUpdateProduct.visibility = View.GONE
        }
    }

    private fun loadProductData(productId: String) {
        btnUpdateProduct.isEnabled = false

        firestore.collection("products").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    existingProduct = document.toObject(Product::class.java)
                    existingProduct?.let { product ->
                        etProductName.setText(product.name)
                        etProductDescription.setText(product.description)
                        etProductPrice.setText(product.price.toString())
                        etProductStock.setText(product.stock.toString())

                        if (product.imageUrl.isNotEmpty()) {
                            selectedImageUrl = product.imageUrl
                            Glide.with(this)
                                .load(product.imageUrl)
                                .into(ivProductImagePreview)
                        }
                    }
                    btnUpdateProduct.isEnabled = true
                } else {
                    Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar producto: ${e.message}", Toast.LENGTH_LONG).show()
                btnUpdateProduct.isEnabled = true
            }
    }

    private fun setupListeners() {
        btnUploadImage.setOnClickListener { showImageSourceDialog() }
        btnSaveProduct.setOnClickListener { saveProduct() }
        btnUpdateProduct.setOnClickListener { updateProduct() }
    }

    private fun saveProduct() {
        if (!validateInputs()) {
            return
        }

        btnSaveProduct.isEnabled = false
        Toast.makeText(this, "Guardando producto...", Toast.LENGTH_SHORT).show()

        when {
            selectedImageUri != null -> uploadImageAndSaveProduct(selectedImageUri!!)
            selectedImageUrl != null -> createProduct(selectedImageUrl!!)
            else -> createProduct("")
        }
    }

    private fun updateProduct() {
        if (!validateInputs()) {
            return
        }

        val product = existingProduct
        if (product == null) {
            Toast.makeText(this, "Error: Producto no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        btnUpdateProduct.isEnabled = false
        Toast.makeText(this, "Actualizando producto...", Toast.LENGTH_SHORT).show()

        when {
            selectedImageUri != null -> uploadImageAndUpdateProduct(selectedImageUri!!, product)
            selectedImageUrl != product.imageUrl -> updateProductData(product.id, selectedImageUrl ?: "")
            else -> updateProductData(product.id, product.imageUrl)
        }
    }

    private fun uploadImageAndSaveProduct(imageUri: Uri) {
        val fileName = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("product_images/$fileName")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    createProduct(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir la imagen: ${e.message}", Toast.LENGTH_LONG).show()
                btnSaveProduct.isEnabled = true
            }
    }

    private fun uploadImageAndUpdateProduct(imageUri: Uri, product: Product) {
        val fileName = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("product_images/$fileName")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Delete old image if it exists
                    if (product.imageUrl.isNotEmpty()) {
                        deleteOldImage(product.imageUrl)
                    }
                    updateProductData(product.id, uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir la imagen: ${e.message}", Toast.LENGTH_LONG).show()
                btnUpdateProduct.isEnabled = true
            }
    }

    private fun deleteOldImage(imageUrl: String) {
        try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete()
        } catch (e: Exception) {
            // Silently fail - old image might not exist or URL might be external
        }
    }

    private fun createProduct(imageUrl: String) {
        val sellerId = sharedPreferences.getString("user_uid", null)
        if (sellerId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado. Por favor, inicie sesión de nuevo.", Toast.LENGTH_LONG).show()
            btnSaveProduct.isEnabled = true
            return
        }

        val product = Product(
            id = UUID.randomUUID().toString(),
            name = etProductName.text.toString().trim(),
            description = etProductDescription.text.toString().trim(),
            price = etProductPrice.text.toString().toDoubleOrNull() ?: 0.0,
            stock = etProductStock.text.toString().toIntOrNull() ?: 0,
            imageUrl = imageUrl,
            sellerId = sellerId
        )

        firestore.collection("products").document(product.id)
            .set(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Producto guardado con éxito", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el producto: ${e.message}", Toast.LENGTH_LONG).show()
                btnSaveProduct.isEnabled = true
            }
    }

    private fun updateProductData(productId: String, imageUrl: String) {
        val updates = hashMapOf<String, Any>(
            "name" to etProductName.text.toString().trim(),
            "description" to etProductDescription.text.toString().trim(),
            "price" to (etProductPrice.text.toString().toDoubleOrNull() ?: 0.0),
            "stock" to (etProductStock.text.toString().toIntOrNull() ?: 0),
            "imageUrl" to imageUrl
        )

        firestore.collection("products").document(productId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar el producto: ${e.message}", Toast.LENGTH_LONG).show()
                btnUpdateProduct.isEnabled = true
            }
    }

    private fun validateInputs(): Boolean {
        if (etProductName.text.isBlank()) {
            etProductName.error = "El nombre es requerido"
            etProductName.requestFocus()
            return false
        }
        if (etProductPrice.text.isBlank()) {
            etProductPrice.error = "El precio es requerido"
            etProductPrice.requestFocus()
            return false
        }
        val price = etProductPrice.text.toString().toDoubleOrNull()
        if (price == null || price < 0) {
            etProductPrice.error = "Ingrese un precio válido"
            etProductPrice.requestFocus()
            return false
        }
        if (etProductStock.text.isBlank()) {
            etProductStock.error = "El stock es requerido"
            etProductStock.requestFocus()
            return false
        }
        val stock = etProductStock.text.toString().toIntOrNull()
        if (stock == null || stock < 0) {
            etProductStock.error = "Ingrese un stock válido"
            etProductStock.requestFocus()
            return false
        }
        return true
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Subir desde el dispositivo", "Usar URL de la imagen")
        AlertDialog.Builder(this)
            .setTitle("Elegir fuente de la imagen")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openFileChooser()
                    1 -> showImageUrlDialog()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showImageUrlDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pegar URL de la imagen")

        val input = EditText(this)
        input.hint = "https://ejemplo.com/imagen.jpg"
        builder.setView(input)

        builder.setPositiveButton("Aceptar") { dialog, _ ->
            val url = input.text.toString().trim()
            if (url.isNotEmpty()) {
                selectedImageUrl = url
                selectedImageUri = null
                Glide.with(this).load(selectedImageUrl).into(ivProductImagePreview)
                Toast.makeText(this, "Imagen cargada desde URL", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "La URL no puede estar vacía", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            selectedImageUrl = null
            Glide.with(this).load(selectedImageUri).into(ivProductImagePreview)
        }
    }
}