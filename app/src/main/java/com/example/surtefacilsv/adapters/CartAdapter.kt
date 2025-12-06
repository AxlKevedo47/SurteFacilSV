package com.example.surtefacilsv.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.surtefacilsv.R
import com.example.surtefacilsv.models.OrderItem
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class CartAdapter(
    private var cartItems: List<OrderItem>,
    private val onIncrease: (OrderItem) -> Unit,
    private val onDecrease: (OrderItem) -> Unit,
    private val onRemove: (OrderItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_product, parent, false)
        return ViewHolder(view, firestore)
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

    class ViewHolder(
        itemView: View,
        private val firestore: FirebaseFirestore
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImageCart)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductNameCart)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPriceCart)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantityCart)
        private val btnDecrease: MaterialButton = itemView.findViewById(R.id.btnDecreaseQuantity)
        private val btnIncrease: MaterialButton = itemView.findViewById(R.id.btnIncreaseQuantity)
        private val btnRemove: MaterialButton = itemView.findViewById(R.id.btnRemoveFromCart)

        fun bind(
            item: OrderItem,
            onIncrease: (OrderItem) -> Unit,
            onDecrease: (OrderItem) -> Unit,
            onRemove: (OrderItem) -> Unit
        ) {
            tvProductName.text = item.name
            tvProductPrice.text = String.format("$%.2f", item.price)
            tvQuantity.text = item.quantity.toString()

            loadProductImage(item.productId)

            btnIncrease.setOnClickListener { onIncrease(item) }
            btnDecrease.setOnClickListener { onDecrease(item) }
            btnRemove.setOnClickListener { onRemove(item) }
        }

        private fun loadProductImage(productId: String) {
            if (productId.isEmpty()) {
                ivProductImage.setImageResource(R.drawable.ic_image_placeholder)
                return
            }

            firestore.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener { document ->
                    val imageUrl = document.getString("imageUrl")
                    Log.d("CartAdapter", "Image URL from Firestore: $imageUrl")

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(itemView.context)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_placeholder)
                            .centerCrop()
                            .into(ivProductImage)
                    } else {
                        ivProductImage.setImageResource(R.drawable.ic_image_placeholder)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("CartAdapter", "Error loading image: ${e.message}")
                    ivProductImage.setImageResource(R.drawable.ic_image_placeholder)
                }
        }
    }
}