package com.example.surtefacilsv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
        holder.buttonRemove.setOnClickListener {
            onRemoveClick(productoCarrito)
        }
    }

    override fun getItemCount(): Int = productos.size

    class CarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewProductName: TextView = itemView.findViewById(R.id.textViewProductName)
        private val textViewProductPrice: TextView = itemView.findViewById(R.id.textViewProductPrice)
        private val textViewQuantity: TextView = itemView.findViewById(R.id.textViewQuantity)
        val buttonRemove: ImageButton = itemView.findViewById(R.id.buttonRemoveFromCart)

        fun bind(productoCarrito: ProductoCarrito) {
            textViewProductName.text = productoCarrito.producto.name
            textViewProductPrice.text = String.format("$%.2f", productoCarrito.producto.price)
            textViewQuantity.text = "Cantidad: ${productoCarrito.cantidad}"
        }
    }
}