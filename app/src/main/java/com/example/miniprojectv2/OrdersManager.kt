package com.example.miniprojectv2

object OrdersManager {
    val orders = mutableListOf<OrderItem>()
}

data class OrderItem(val name: String, val qty: Int, val price: Int)
