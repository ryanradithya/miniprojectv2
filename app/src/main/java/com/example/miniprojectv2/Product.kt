package com.example.miniprojectv2

data class Product(
    val name: String,
    val price: Int,
    val imageUri: String? = null,
    var stock: Int,
    val description: String,
    val category: String = "",
    val rating: Float = 0f,
    val reviews: List<Review> = emptyList()
)

data class Review(
    val reviewerName: String,
    val comment: String,
    val rating: Float,
    val date: String
)
