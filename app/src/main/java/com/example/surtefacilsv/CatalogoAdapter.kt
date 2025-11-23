package com.example.surtefacilsv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CatalogoAdapter(
    private val productList: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<CatalogoAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_catalogo, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)

        // ✅ SOLO el botón agrega al carrito, NO la imagen
        holder.btnAgregar.setOnClickListener {
            onItemClick(product)
        }

        // La imagen solo muestra el producto, no agrega al carrito
        holder.imageProduct.setOnClickListener {
            // Podemos mostrar un mensaje o detalles del producto
            // pero NO agregar al carrito
        }
    }

    override fun getItemCount(): Int = productList.size

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ✅ CAMBIAR de private a internal/val
        val imageProduct: ImageView = itemView.findViewById(R.id.imageProduct)
        private val textName: TextView = itemView.findViewById(R.id.textProductName)
        private val textPrice: TextView = itemView.findViewById(R.id.textProductPrice)
        val btnAgregar: Button = itemView.findViewById(R.id.btnAgregarCarrito)

        fun bind(product: Product) {
            textName.text = product.name
            textPrice.text = String.format("$%.2f", product.price)

            Glide.with(itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder) // Imagen por si hay error
                .into(imageProduct)
        }
    }
}