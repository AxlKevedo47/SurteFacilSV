package com.example.surtefacilsv

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
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

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_product)
        
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        initViews()
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

    private fun setupListeners() {
        btnUploadImage.setOnClickListener { showImageSourceDialog() }
        btnSaveProduct.setOnClickListener { saveProduct() }
        btnUpdateProduct.setOnClickListener { /* TODO: Implement update logic */ }
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

    private fun createProduct(imageUrl: String) {
        val sellerId = sharedPreferences.getString("user_uid", null)
        if (sellerId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado. Por favor, inicie sesión de nuevo.", Toast.LENGTH_LONG).show()
            btnSaveProduct.isEnabled = true
            return
        }

        val product = Product(
            id = UUID.randomUUID().toString(),
            name = etProductName.text.toString(),
            description = etProductDescription.text.toString(),
            price = etProductPrice.text.toString().toDoubleOrNull() ?: 0.0,
            stock = etProductStock.text.toString().toIntOrNull() ?: 0,
            imageUrl = imageUrl,
            sellerId = sellerId
        )

        firestore.collection("products").document(product.id)
            .set(product)
            .addOnSuccessListener { 
                Toast.makeText(this, "Producto guardado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el producto: ${e.message}", Toast.LENGTH_LONG).show()
                btnSaveProduct.isEnabled = true
            }
    }

    private fun validateInputs(): Boolean {
        if (etProductName.text.isBlank()) {
            etProductName.error = "El nombre es requerido"
            return false
        }
        if (etProductPrice.text.isBlank()) {
            etProductPrice.error = "El precio es requerido"
            return false
        }
        if (etProductStock.text.isBlank()) {
            etProductStock.error = "El stock es requerido"
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