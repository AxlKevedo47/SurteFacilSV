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

        fun bind(pedido: Pedido) {
            textViewPedidoId.text = "Pedido #${pedido.id.take(6)}..."
            textViewPedidoTotal.text = String.format("Total: $%.2f", pedido.total)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            textViewPedidoFecha.text = sdf.format(pedido.fecha)
        }
    }
}
