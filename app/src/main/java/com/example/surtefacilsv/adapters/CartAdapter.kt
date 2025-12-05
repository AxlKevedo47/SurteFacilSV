package com.example.surtefacilsv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.surtefacilsv.R
import com.example.surtefacilsv.models.OrderItem

class CartAdapter(
    private var cartItems: List<OrderItem>,
    private val onIncrease: (OrderItem) -> Unit,
    private val onDecrease: (OrderItem) -> Unit,
    private val onRemove: (OrderItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = cartItems[position]
        holder.bind(item, onIncrease, onDecrease, onRemove)
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateList(newList: List<OrderItem>) {
        cartItems = newList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImageCart)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductNameCart)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPriceCart)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantityCart)
        private val btnDecrease: Button = itemView.findViewById(R.id.btnDecreaseQuantity)
        private val btnIncrease: Button = itemView.findViewById(R.id.btnIncreaseQuantity)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemoveFromCart)

        fun bind(item: OrderItem, onIncrease: (OrderItem) -> Unit, onDecrease: (OrderItem) -> Unit, onRemove: (OrderItem) -> Unit) {
            tvProductName.text = item.name
            tvProductPrice.text = String.format("$%.2f", item.price)
            tvQuantity.text = item.quantity.toString()

            // You might need to fetch the image URL from your products collection
            // For now, let's assume a placeholder
            // Glide.with(itemView.context).load(item.imageUrl).into(ivProductImage)

            btnIncrease.setOnClickListener { onIncrease(item) }
            btnDecrease.setOnClickListener { onDecrease(item) }
            btnRemove.setOnClickListener { onRemove(item) }
        }
    }
}
