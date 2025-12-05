package com.example.surtefacilsv

import java.util.Date

data class Pedido(
    val id: String = "",
    val productos: List<ProductoCarrito> = emptyList(),
    val total: Double = 0.0,
    val subtotal: Double = 0.0,
    val envio: Double = 0.0,
    val fecha: Date = Date(),
    val direccion: String = "",
    val metodoPago: String = "Contra Entrega",
    val estado: String = "Pendiente",
    val usuarioEmail: String = ""
) {
    // Constructor sin parámetros necesario para Firestore
    constructor() : this("", emptyList(), 0.0, 0.0, 0.0, Date(), "", "Contra Entrega", "Pendiente", "")
}