package com.example.surtefacilsv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.surtefacilsv.R
import com.example.surtefacilsv.models.OrderItem

class OrderDetailProductAdapter(private val itemList: List<OrderItem>) : RecyclerView.Adapter<OrderDetailProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_product_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = itemList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvProductQuantity)
        private val tvName: TextView = itemView.findViewById(R.id.tvProductNameDetail)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvProductPriceDetail)

        fun bind(item: OrderItem) {
            tvQuantity.text = "${item.quantity}x"
            tvName.text = item.name
            tvPrice.text = String.format("$%.2f", item.price)
        }
    }
}
