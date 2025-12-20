package com.example.miniprojectv2

import com.google.firebase.firestore.FirebaseFirestore

object TransactionManager {

    private val db = FirebaseFirestore.getInstance()

    fun addTransaction(
        buyer: String,
        expedition: String,
        items: List<TransactionItem>,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {

        if (items.isEmpty()) {
            onError(Exception("Item kosong"))
            return
        }

        val groupedItems = items.groupBy { it.sellerEmail }

        var successCounter = 0
        val totalTransaction = groupedItems.size

        groupedItems.forEach { (sellerEmail, sellerItems) ->

            val data = hashMapOf(
                "buyer" to buyer,
                "sellerEmail" to sellerEmail,
                "expedition" to expedition,
                "date" to System.currentTimeMillis(),
                "status" to "Pesanan Masuk",
                "trackingNumber" to null,
                "items" to sellerItems,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("transactions")
                .add(data)
                .addOnSuccessListener {
                    successCounter++
                    if (successCounter == totalTransaction) {
                        onSuccess()
                    }
                }
                .addOnFailureListener {
                    onError(it)
                }
        }
    }

    fun getTransactionsForBuyer(
        buyer: String,
        onSuccess: (List<Transaction>) -> Unit,
        onError: (Exception) -> Unit
    ) {

        db.collection("transactions")
            .whereEqualTo("buyer", buyer)
            .get()
            .addOnSuccessListener { snapshot ->

                val transactions = snapshot.map { doc ->
                    val trx = doc.toObject(Transaction::class.java)
                    trx.transactionId = doc.id
                    trx
                }

                onSuccess(transactions)
            }
            .addOnFailureListener { onError(it) }
    }

    fun getTransactionsForSeller(
        sellerEmail: String,
        onSuccess: (List<Transaction>) -> Unit,
        onError: (Exception) -> Unit
    ) {

        db.collection("transactions")
            .whereEqualTo("sellerEmail", sellerEmail)
            .get()
            .addOnSuccessListener { snapshot ->

                val result = snapshot.map { doc ->
                    val trx = doc.toObject(Transaction::class.java)
                    trx.transactionId = doc.id
                    trx
                }

                onSuccess(result)
            }
            .addOnFailureListener { onError(it) }
    }

    fun updateStatus(
        transactionId: String,
        newStatus: String,
        trackingNumber: String? = null,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {

        val updateData = mutableMapOf<String, Any>(
            "status" to newStatus,
            "updatedAt" to System.currentTimeMillis()
        )

        trackingNumber?.let {
            updateData["trackingNumber"] = it
        }

        db.collection("transactions")
            .document(transactionId)
            .update(updateData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getTransactionById(
        transactionId: String,
        onSuccess: (Transaction?) -> Unit,
        onError: (Exception) -> Unit
    ) {

        db.collection("transactions")
            .document(transactionId)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) {
                    onSuccess(null)
                    return@addOnSuccessListener
                }

                val trx = doc.toObject(Transaction::class.java)
                trx?.transactionId = doc.id
                onSuccess(trx)
            }
            .addOnFailureListener { onError(it) }
    }
}
