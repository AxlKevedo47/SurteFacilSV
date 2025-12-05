package com.example.surtefacilsv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class PedidosAdapter(private val pedidos: List<Pedido>) : RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        holder.bind(pedido)
    }

    override fun getItemCount(): Int = pedidos.size

    class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewPedidoId: TextView = itemView.findViewById(R.id.textViewPedidoId)
        private val textViewPedidoTotal: TextView = itemView.findViewById(R.id.textViewPedidoTotal)
        private val textViewPedidoFecha: TextView = itemView.findViewById(R.id.textViewPedidoFecha)
        private val textViewEstado: TextView = itemView.findViewById(R.id.textViewEstado)
        private val textViewCantidadProductos: TextView = itemView.findViewById(R.id.textViewCantidadProductos)

        fun bind(pedido: Pedido) {
            // ID del pedido
            textViewPedidoId.text = "Pedido #${pedido.id.take(8)}"

            // Total
            textViewPedidoTotal.text = String.format("Total: $%.2f", pedido.total)

            // Fecha
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
            textViewPedidoFecha.text = sdf.format(pedido.fecha)

            // Cantidad de productos
            val cantidadTotal = pedido.productos.sumOf { it.cantidad }
            textViewCantidadProductos.text = if (cantidadTotal == 1) {
                "1 Producto"
            } else {
                "$cantidadTotal Productos"
            }

            // Estado con background según el tipo
            textViewEstado.text = pedido.estado
            when (pedido.estado.lowercase()) {
                "pendiente" -> {
                    textViewEstado.setBackgroundResource(R.drawable.pendiente)
                }
                "completado", "entregado" -> {
                    textViewEstado.setBackgroundResource(R.drawable.completado)
                }
                "cancelado" -> {
                    textViewEstado.setBackgroundResource(R.drawable.cancelado)
                }
                else -> {
                    textViewEstado.setBackgroundResource(R.drawable.pendiente)
                }
            }
        }
    }
}