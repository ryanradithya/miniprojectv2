package com.example.miniprojectv2

data class Transaction(
    val buyer: String = "",
    val expedition: String = "",
    val date: String = "",
    var status: String = "Pesanan Masuk",
    var trackingNumber: String? = null,
    val items: List<TransactionItem> = emptyList(),
    var transactionId: String = ""      // key dokumen Firestore
)
