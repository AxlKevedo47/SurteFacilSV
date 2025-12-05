package com.example.surtefacilsv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.surtefacilsv.R
import com.example.surtefacilsv.models.Product

class BuyerProductAdapter(
    private var productList: List<Product>,
    private val onAddToCartClick: (Product) -> Unit
) : RecyclerView.Adapter<BuyerProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_buyer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product, onAddToCartClick)
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImageBuyer)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductNameBuyer)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPriceBuyer)
        private val btnAddToCart: Button = itemView.findViewById(R.id.btnAddToCart)

        fun bind(product: Product, onAddToCartClick: (Product) -> Unit) {
            tvProductName.text = product.name
            tvProductPrice.text = String.format("$%.2f", product.price)

            // Only show the image if it's a valid web URL
            if (product.imageUrl.startsWith("http")) {
                ivProductImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .into(ivProductImage)
            } else {
                ivProductImage.visibility = View.GONE
            }

            btnAddToCart.setOnClickListener { onAddToCartClick(product) }
        }
    }
}
