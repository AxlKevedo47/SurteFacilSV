package com.example.surtefacilsv

data class Product(
    var id: String = "",
    var name: String = "",
    var price: Double = 0.0,
    var imageUrl: String = ""
) {
    // Constructor sin parámetros para Firestore
    constructor() : this("", "", 0.0, "")
}