package com.example.miniprojectv2

import com.google.firebase.firestore.FirebaseFirestore

object TransactionManager {

    private val db = FirebaseFirestore.getInstance()

    // TAMBAH TRANSAKSI BARU (1 transaksi = banyak item)
    fun addTransaction(
        buyer: String,
        expedition: String,
        items: List<TransactionItem>,
        onComplete: () -> Unit = {},
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
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener(onError)
    }

    // BUYER: AMBIL TRANSAKSI MILIKNYA
    fun getTransactionsForBuyer(
        buyer: String,
        onComplete: (List<Transaction>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("transactions")
            .whereEqualTo("buyer", buyer)
            .get()
            .addOnSuccessListener { result ->

                val list = result.map { doc ->
                    val trx = doc.toObject(Transaction::class.java)
                    trx.transactionId = doc.id
                    trx
                }

                onComplete(list)
            }
            .addOnFailureListener(onError)
    }

    // SELLER: LIHAT SEMUA TRANSAKSI
    fun getTransactionsForSeller(
        onComplete: (List<Transaction>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("transactions")
            .get()
            .addOnSuccessListener { result ->

                val list = result.map { doc ->
                    val trx = doc.toObject(Transaction::class.java)
                    trx.transactionId = doc.id
                    trx
                }

                onComplete(list)
            }
            .addOnFailureListener(onError)
    }

    // UPDATE STATUS (Diproses, Dikirim, Selesai)
    fun updateStatus(
        transactionId: String,
        newStatus: String,
        trackingNumber: String? = null,
        onComplete: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {

        val updateData = mapOf(
            "status" to newStatus,
            "trackingNumber" to trackingNumber
        )

        db.collection("transactions")
            .document(transactionId)
            .update(updateData)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener(onError)
    }

    fun getTransactionById(
        transactionId: String,
        onComplete: (Transaction?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("transactions")
            .document(transactionId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    onComplete(null)
                    return@addOnSuccessListener
                }

                val trx = doc.toObject(Transaction::class.java)
                if (trx != null) {
                    trx.transactionId = doc.id
                }

                onComplete(trx)
            }
            .addOnFailureListener(onError)
    }

}
