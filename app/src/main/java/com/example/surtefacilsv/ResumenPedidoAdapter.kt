
package com.example.surtefacilsv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResumenPedidoAdapter(
    private val productos: MutableList<ProductoCarrito>
) : RecyclerView.Adapter<ResumenPedidoAdapter.ResumenViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResumenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resumen_pedido, parent, false)
        return ResumenViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResumenViewHolder, position: Int) {
        val productoCarrito = productos[position]
        holder.bind(productoCarrito)
    }

    override fun getItemCount(): Int = productos.size

    class ResumenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtProductoResumen: TextView = itemView.findViewById(R.id.txtProductoResumen)
        private val txtPrecioResumen: TextView = itemView.findViewById(R.id.txtPrecioResumen)

        fun bind(productoCarrito: ProductoCarrito) {
            // Formato: "Nombre del producto  x#"
            txtProductoResumen.text = "${productoCarrito.producto.name}  x${productoCarrito.cantidad}"

            // Precio total del item
            val precioTotal = productoCarrito.producto.price * productoCarrito.cantidad
            txtPrecioResumen.text = String.format("$%.2f", precioTotal)
        }
    }
}