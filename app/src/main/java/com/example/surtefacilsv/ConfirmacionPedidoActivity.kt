package com.example.surtefacilsv

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ConfirmacionPedidoActivity : AppCompatActivity() {

    private lateinit var recyclerViewResumen: RecyclerView
    private lateinit var resumenAdapter: ResumenPedidoAdapter
    private lateinit var txtSubtotal: TextView
    private lateinit var txtEnvio: TextView
    private lateinit var txtTotal: TextView
    private lateinit var editTextDireccion: TextInputEditText
    private lateinit var btnEnviarPedido: Button
    private lateinit var firestore: FirebaseFirestore

    private val costoEnvio = 5.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacion_pedido)

        // Habilitar botón de regreso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Confirmación de Pedido"

        inicializarVistas()
        configurarRecyclerView()
        calcularTotales()

        btnEnviarPedido.setOnClickListener {
            enviarPedido()
        }
    }

    private fun inicializarVistas() {
        recyclerViewResumen = findViewById(R.id.recyclerViewResumen)
        txtSubtotal = findViewById(R.id.txtSubtotal)
        txtEnvio = findViewById(R.id.txtEnvio)
        txtTotal = findViewById(R.id.txtTotal)
        editTextDireccion = findViewById(R.id.editTextDireccion)
        btnEnviarPedido = findViewById(R.id.btnEnviarPedido)
        firestore = FirebaseFirestore.getInstance()
    }

    private fun configurarRecyclerView() {
        resumenAdapter = ResumenPedidoAdapter(Carrito.productos)
        recyclerViewResumen.apply {
            layoutManager = LinearLayoutManager(this@ConfirmacionPedidoActivity)
            adapter = resumenAdapter
        }
    }

    private fun calcularTotales() {
        val subtotal = Carrito.getTotal()
        val total = subtotal + costoEnvio

        txtSubtotal.text = String.format("$%.2f", subtotal)
        txtEnvio.text = String.format("$%.2f", costoEnvio)
        txtTotal.text = String.format("$%.2f", total)
    }

    private fun enviarPedido() {
        println("🚀 DEBUG: Iniciando enviarPedido()")

        val direccion = editTextDireccion.text.toString().trim()
        println("📍 DEBUG: Dirección ingresada: '$direccion'")

        // Validar que se haya ingresado una dirección
        if (direccion.isEmpty()) {
            println("⚠️ DEBUG: Dirección vacía")
            Toast.makeText(this, "Por favor ingresa una dirección de entrega", Toast.LENGTH_SHORT).show()
            editTextDireccion.error = "La dirección es requerida"
            return
        }

        // Validar que el carrito no esté vacío
        println("🛒 DEBUG: Productos en Carrito: ${Carrito.productos.size}")
        if (Carrito.productos.isEmpty()) {
            println("⚠️ DEBUG: Carrito vacío")
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Deshabilitar botón mientras se procesa
        println("🔒 DEBUG: Deshabilitando botón")
        btnEnviarPedido.isEnabled = false
        btnEnviarPedido.text = "Procesando..."

        // Calcular totales correctamente
        val subtotal = Carrito.productos.sumOf { it.producto.price * it.cantidad }
        val totalConEnvio = subtotal + costoEnvio

        println("🛒 DEBUG: Calculando totales...")
        println("🛒 DEBUG: Subtotal: $subtotal")
        println("🛒 DEBUG: Envío: $costoEnvio")
        println("🛒 DEBUG: Total: $totalConEnvio")
        println("🛒 DEBUG: Productos en carrito: ${Carrito.productos.size}")

        // Validar que el subtotal no sea 0
        if (subtotal <= 0) {
            Toast.makeText(this, "Error: El carrito está vacío o el total es inválido", Toast.LENGTH_SHORT).show()
            btnEnviarPedido.isEnabled = true
            btnEnviarPedido.text = "Enviar Pedido"
            return
        }

        // Generar ID usando Firebase
        val pedidoRef = firestore.collection("pedidos").document()
        val pedidoId = pedidoRef.id

        // Crear el pedido con TODOS los campos
        val pedidoData = hashMapOf(
            "id" to pedidoId,
            "productos" to Carrito.productos.map { productoCarrito ->
                hashMapOf(
                    "producto" to hashMapOf(
                        "id" to productoCarrito.producto.id,
                        "name" to productoCarrito.producto.name,
                        "price" to productoCarrito.producto.price,
                        "imageUrl" to (productoCarrito.producto.imageUrl ?: "")
                    ),
                    "cantidad" to productoCarrito.cantidad
                )
            },
            "subtotal" to subtotal,
            "envio" to costoEnvio,
            "total" to totalConEnvio,
            "direccion" to direccion,
            "metodoPago" to "Contra Entrega",
            "fecha" to Date(),
            "estado" to "Pendiente",
            "usuarioEmail" to ""
        )

        println("🛒 DEBUG: Enviando pedido - ID: $pedidoId")
        println("🛒 DEBUG: Datos del pedido: $pedidoData")

        // CAMBIO IMPORTANTE: Usar .set() en lugar de .add()
        println("💾 DEBUG: Intentando guardar en Firebase...")
        pedidoRef.set(pedidoData)
            .addOnSuccessListener {
                println("✅ DEBUG: Pedido guardado exitosamente con ID: $pedidoId")

                // Mostrar diálogo de éxito
                DialogHelper.showSuccessDialog(
                    context = this,
                    title = "Pedido realizado con éxito",
                    buttonText = "Volver"
                ) {
                    // Limpiar el carrito
                    Carrito.limpiarCarrito()

                    // Cerrar esta actividad y volver
                    finish()
                }
            }
            .addOnFailureListener { e ->
                println("❌ DEBUG: Error al guardar pedido")
                println("❌ DEBUG: Tipo de error: ${e.javaClass.simpleName}")
                println("❌ DEBUG: Mensaje: ${e.message}")

                // Determinar el mensaje de error
                val errorMessage = if (e.message?.contains("Unable to resolve host") == true ||
                    e.message?.contains("UNAVAILABLE") == true) {
                    "No hay conexión a Internet"
                } else {
                    "Error al realizar el pedido"
                }

                // Mostrar diálogo de error
                DialogHelper.showErrorDialog(
                    context = this,
                    title = errorMessage,
                    buttonText = "Volver"
                ) {
                    // Rehabilitar botón
                    btnEnviarPedido.isEnabled = true
                    btnEnviarPedido.text = "Enviar Pedido"
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}