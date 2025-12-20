package com.example.miniprojectv2

import com.google.firebase.firestore.FirebaseFirestore

object SellerReportRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getReport(
        sellerEmail: String,
        startTime: Long,
        endTime: Long,
        onResult: (SellerReport) -> Unit
    ) {
        db.collection("transactions")
            .whereGreaterThanOrEqualTo("date", startTime)
            .whereLessThanOrEqualTo("date", endTime)
            .get()
            .addOnSuccessListener { snapshot ->

                var revenue = 0L
                var itemsSold = 0
                var transactionCount = 0

                snapshot.documents.forEach { doc ->
                    val items = doc.get("items") as? List<Map<String, Any>> ?: return@forEach

                    var hasSellerItem = false

                    items.forEach { item ->
                        if (item["sellerEmail"] == sellerEmail) {
                            val price = (item["price"] as Long)
                            val qty = (item["qty"] as Long).toInt()

                            revenue += price * qty
                            itemsSold += qty
                            hasSellerItem = true
                        }
                    }

                    if (hasSellerItem) transactionCount++
                }

                onResult(
                    SellerReport(
                        totalRevenue = revenue,
                        totalItems = itemsSold,
                        totalTransactions = transactionCount
                    )
                )
            }
    }
}
