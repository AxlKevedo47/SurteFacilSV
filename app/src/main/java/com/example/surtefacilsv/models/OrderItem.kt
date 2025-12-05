package com.example.surtefacilsv.models

data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    var quantity: Int = 0,
    val sellerId: String = "",
    val stock: Int = 0, // Available stock for this product
    val imageUrl: String = ""
)
