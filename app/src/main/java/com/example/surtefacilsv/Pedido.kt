package com.example.surtefacilsv

import java.util.Date

data class Pedido(
    val id: String = "",
    val productos: List<ProductoCarrito> = emptyList(),
    val total: Double = 0.0,
    val fecha: Date = Date(),
    val usuarioEmail: String = "" // Agregar para identificar al usuario
) {
    // Constructor sin parámetros necesario para Firestore
    constructor() : this("", emptyList(), 0.0, Date(), "")
}