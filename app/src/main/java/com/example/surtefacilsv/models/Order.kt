package com.example.surtefacilsv.models

import java.util.Date

data class Order(
    var id: String = "",
    val customerName: String = "",
    val orderDate: Date = Date(),
    val status: String = "Pendiente", // Can be "Pendiente" or "Entregado"
    val sellerId: String = "",
    val customerId: String = "",
    val items: List<OrderItem> = emptyList(), // Changed from Product to OrderItem
    val totalPrice: Double = 0.0,
    val customerAddress: String = "" // Added customer address field
)
