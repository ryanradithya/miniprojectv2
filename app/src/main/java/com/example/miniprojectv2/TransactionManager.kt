package com.example.miniprojectv2

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Transaction(
    val itemName: String,
    val qty: Int,
    val totalPrice: Int,
    val date: String
)

object TransactionManager {
    val transactions = mutableListOf<Transaction>()

    fun addTransaction(itemName: String, qty: Int, price: Int) {
        val total = qty * price
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dateNow = dateFormat.format(Date())

        transactions.add(Transaction(itemName, qty, total, dateNow))
    }
}
