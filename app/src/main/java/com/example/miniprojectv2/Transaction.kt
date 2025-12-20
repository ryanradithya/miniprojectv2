package com.example.miniprojectv2

data class Transaction(
    val buyer: String = "",
    val expedition: String = "",
    val date: String = "",
    var status: String = "Pesanan Masuk",
    var trackingNumber: String? = null,
    var items: List<TransactionItem> = emptyList(),
    var transactionId: String = "",
    val updatedAt: Long = 0L
)
