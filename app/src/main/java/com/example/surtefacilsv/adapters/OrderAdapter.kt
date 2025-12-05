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

class OrderAdapter(
    private var orderList: List<Order>,
    private val onDetailsClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_seller, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]
        holder.bind(order, onDetailsClick)
    }

    override fun getItemCount(): Int = orderList.size

    fun updateList(newList: List<Order>) {
        orderList = newList
        notifyDataSetChanged()
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        private val btnViewOrderDetails: Button = itemView.findViewById(R.id.btnViewOrderDetails)
        private val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))

        fun bind(order: Order, onDetailsClick: (Order) -> Unit) {
            tvCustomerName.text = order.customerName
            tvOrderDate.text = dateFormat.format(order.orderDate)
            tvOrderStatus.text = order.status

            val statusColor = if (order.status == "Pendiente") {
                itemView.context.getColor(R.color.colorAccent)
            } else {
                itemView.context.getColor(R.color.colorPrimary) // Or another color for "Entregado"
            }
            tvOrderStatus.setTextColor(statusColor)

            btnViewOrderDetails.setOnClickListener { onDetailsClick(order) }
        }
    }
}
