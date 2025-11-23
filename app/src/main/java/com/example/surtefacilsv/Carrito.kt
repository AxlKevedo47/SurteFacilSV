package com.example.surtefacilsv

object Carrito {
    val productos = mutableListOf<ProductoCarrito>()

    fun agregarProducto(producto: Product, cantidad: Int) {
        val productoExistente = productos.find { it.producto.id == producto.id }
        if (productoExistente != null) {
            productoExistente.cantidad += cantidad
        } else {
            productos.add(ProductoCarrito(producto, cantidad))
        }
    }

    fun removerProducto(productoCarrito: ProductoCarrito) {
        productos.remove(productoCarrito)
    }

    fun getTotal(): Double {
        return productos.sumOf { it.producto.price * it.cantidad }
    }

    fun limpiarCarrito() {
        productos.clear()
    }
}