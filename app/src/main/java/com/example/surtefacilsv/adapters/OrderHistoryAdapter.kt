package com.example.surtefacilsv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.surtefacilsv.R
import com.example.surtefacilsv.models.Order
import java.text.SimpleDateFormat
import java.util.Locale

class OrderHistoryAdapter(
    private var orderList: List<Order>,
    private val onDetailsClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]
        holder.bind(order, onDetailsClick)
    }

    override fun getItemCount(): Int = orderList.size

    fun updateList(newList: List<Order>) {
        orderList = newList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDateHistory)
        private val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotalHistory)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatusHistory)
        private val btnViewDetails: Button = itemView.findViewById(R.id.btnViewOrderDetailsHistory)
        private val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))

        fun bind(order: Order, onDetailsClick: (Order) -> Unit) {
            tvOrderDate.text = dateFormat.format(order.orderDate)
            tvOrderTotal.text = String.format(Locale.US, "Total: $%.2f", order.totalPrice)
            tvOrderStatus.text = order.status

            val statusColor = if (order.status == "Pendiente") {
                itemView.context.getColor(R.color.colorAccent)
            } else {
                itemView.context.getColor(R.color.colorPrimary)
            }
            tvOrderStatus.setTextColor(statusColor)

            btnViewDetails.setOnClickListener { onDetailsClick(order) }
        }
    }
}
