package com.example.surtefacilsv.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.surtefacilsv.models.OrderItem
import com.example.surtefacilsv.models.Product

object CartManager {

    private val _cartItems = MutableLiveData<List<OrderItem>>(emptyList())
    val cartItems: LiveData<List<OrderItem>> = _cartItems

    private val _subtotal = MutableLiveData(0.0)
    val subtotal: LiveData<Double> = _subtotal

    fun addProduct(product: Product): Boolean {
        val currentItems = _cartItems.value?.toMutableList() ?: mutableListOf()
        val existingItem = currentItems.find { it.productId == product.id }

        val success = if (existingItem != null) {
            if (existingItem.quantity < existingItem.stock) {
                existingItem.quantity++
                true
            } else {
                false
            }
        } else {
            if (product.stock > 0) {
                val newItem = OrderItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    quantity = 1,
                    sellerId = product.sellerId,
                    stock = product.stock
                )
                currentItems.add(newItem)
                true
            } else {
                false
            }
        }

        if (success) {
            _cartItems.value = currentItems
            calculateSubtotal()
        }
        return success
    }

    fun removeProduct(item: OrderItem) {
        val currentItems = _cartItems.value?.toMutableList() ?: return
        if (currentItems.remove(item)) {
            _cartItems.value = currentItems
            calculateSubtotal()
        }
    }

    fun increaseQuantity(item: OrderItem): Boolean {
        val currentItems = _cartItems.value?.toMutableList() ?: return false
        val existingItem = currentItems.find { it.productId == item.productId }

        return if (existingItem != null && existingItem.quantity < existingItem.stock) {
            existingItem.quantity++
            _cartItems.value = currentItems
            calculateSubtotal()
            true
        } else {
            false
        }
    }

    fun decreaseQuantity(item: OrderItem) {
        val currentItems = _cartItems.value?.toMutableList() ?: return
        val existingItem = currentItems.find { it.productId == item.productId }

        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                existingItem.quantity--
            } else {
                currentItems.remove(existingItem)
            }
            _cartItems.value = currentItems
            calculateSubtotal()
        }
    }

    private fun calculateSubtotal() {
        val currentItems = _cartItems.value ?: emptyList()
        _subtotal.value = currentItems.sumOf { it.price * it.quantity }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        calculateSubtotal()
    }
}
