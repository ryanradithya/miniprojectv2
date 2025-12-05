package com.example.miniprojectv2

data class Product(
    var id: String? = null,   // <-- new field
    val name: String = "",
    val price: Int = 0,
    var imageUri: String? = null,
    var stock: Int = 0,
    val description: String = "",
    val category: String = "",
    val rating: Float = 0f,
    val reviews: List<Review> = emptyList()
)


data class Review(
    val reviewerName: String = "",
    val comment: String = "",
    val rating: Float = 0f,
    val date: String = ""
)

