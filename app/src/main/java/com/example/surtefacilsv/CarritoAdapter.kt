package com.example.surtefacilsv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.bumptech.glide.Glide

class CarritoAdapter(
    private val productos: MutableList<ProductoCarrito>,
    private val onRemoveClick: (ProductoCarrito) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val productoCarrito = productos[position]
        holder.bind(productoCarrito)

        // Botón eliminar - Mostrar diálogo de confirmación
        holder.buttonRemove.setOnClickListener {
            mostrarDialogoConfirmacion(holder.itemView, productoCarrito)
        }

        // Botón incrementar cantidad
        holder.btnMas.setOnClickListener {
            productoCarrito.cantidad++
            notifyItemChanged(position)
        }

        // Botón decrementar cantidad
        holder.btnMenos.setOnClickListener {
            if (productoCarrito.cantidad > 1) {
                productoCarrito.cantidad--
                notifyItemChanged(position)
            } else {
                // Si la cantidad es 1 y se presiona menos, mostrar confirmación
                mostrarDialogoConfirmacion(holder.itemView, productoCarrito)
            }
        }
    }

    private fun mostrarDialogoConfirmacion(view: View, productoCarrito: ProductoCarrito) {
        val builder = AlertDialog.Builder(view.context)

        // Inflar el layout personalizado del diálogo
        val dialogView = LayoutInflater.from(view.context)
            .inflate(R.layout.dialog_confirmar_eliminacion, null)

        builder.setView(dialogView)
        val dialog = builder.create()

        // Hacer el fondo transparente para mostrar el diseño personalizado
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Configurar los botones
        val btnSi = dialogView.findViewById<android.widget.Button>(R.id.btnSi)
        val btnNo = dialogView.findViewById<android.widget.Button>(R.id.btnNo)

        btnSi.setOnClickListener {
            onRemoveClick(productoCarrito)
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun getItemCount(): Int = productos.size

    class CarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        private val textViewProductName: TextView = itemView.findViewById(R.id.textViewProductName)
        private val textViewProductPrice: TextView = itemView.findViewById(R.id.textViewProductPrice)
        private val textViewPrecioUnitario: TextView = itemView.findViewById(R.id.textViewPrecioUnitario)
        private val textViewQuantity: TextView = itemView.findViewById(R.id.textViewQuantity)
        val buttonRemove: ImageView = itemView.findViewById(R.id.buttonRemoveFromCart)
        val btnMas: FloatingActionButton = itemView.findViewById(R.id.btnMas)
        val btnMenos: FloatingActionButton = itemView.findViewById(R.id.btnMenos)

        fun bind(productoCarrito: ProductoCarrito) {
            textViewProductName.text = productoCarrito.producto.name
            textViewPrecioUnitario.text = String.format("$%.2f", productoCarrito.producto.price)
            textViewQuantity.text = productoCarrito.cantidad.toString()

            // Precio total del item (precio unitario * cantidad)
            val precioTotal = productoCarrito.producto.price * productoCarrito.cantidad
            textViewProductPrice.text = String.format("Total: $%.2f", precioTotal)

            // Cargar imagen del producto con Glide
            Glide.with(itemView.context)
                .load(productoCarrito.producto.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(imgProduct)
        }
    }
}