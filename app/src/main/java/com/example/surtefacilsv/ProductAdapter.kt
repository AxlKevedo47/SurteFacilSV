package com.example.surtefacilsv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.surtefacilsv.R

class ProductAdapter(
    private var productList: MutableList<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val txtName: TextView = itemView.findViewById(R.id.txtProductName)
        val txtPrice: TextView = itemView.findViewById(R.id.txtProductPrice)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEditProduct)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDeleteProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.txtName.text = product.name
        holder.txtPrice.text = "$${"%.2f".format(product.price)}"

        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(holder.imgProduct)

        // BOTON EDITAR - Agregar logs
        holder.btnEdit.setOnClickListener {
            println("DEBUG: Editando producto: ${product.name}")
            onEditClick(product)
        }

        // BOTON ELIMINAR - Agregar logs
        holder.btnDelete.setOnClickListener {
            println("DEBUG: Eliminando producto: ${product.name}")
            onDeleteClick(product)
        }
    }

    fun updateList(newList: MutableList<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}
