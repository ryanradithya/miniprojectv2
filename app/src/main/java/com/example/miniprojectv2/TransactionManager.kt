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

        val data = hashMapOf(
            "buyer" to buyer,
            "expedition" to expedition,
            "date" to System.currentTimeMillis().toString(),
            "status" to "Pesanan Masuk",
            "trackingNumber" to null,
            "items" to items,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("transactions")
            .add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
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
            .get()
            .addOnSuccessListener { snapshot ->

                val result = mutableListOf<Transaction>()

                for (doc in snapshot) {
                    val trx = doc.toObject(Transaction::class.java)
                    trx.transactionId = doc.id

                    val sellerItems = trx.items.filter {
                        it.sellerEmail == sellerEmail
                    }

                    if (sellerItems.isNotEmpty()) {
                        trx.items = sellerItems
                        result.add(trx)
                    }
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
