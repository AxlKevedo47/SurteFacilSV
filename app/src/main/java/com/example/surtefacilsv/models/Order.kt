package com.example.surtefacilsv.models

import java.util.Date

data class Order(
    var id: String = "",
    val customerName: String = "",
    val orderDate: Date = Date(),
    val status: String = "Pendiente",
    val sellerId: String = "",
    val customerId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val customerAddress: String = ""
)
