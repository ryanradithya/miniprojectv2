package com.example.miniprojectv2

data class Product(
    val name: String,
    val price: Int,
    val imageRes: Int, // pakai drawable resource ->> id aset
    val stock: Int,
    val description: String
)
