package com.example.miniprojectv2

data class Product(
    val name: String,
    val price: Int,
    val imageUri: String? = null,
    val stock: Int,
    val description: String,
    val category: String = ""
)
