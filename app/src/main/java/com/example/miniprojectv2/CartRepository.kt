package com.example.miniprojectv2

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object CartRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userEmail(): String? =
        auth.currentUser?.email

    //firestore referensi
    private fun cartRef() =
        userEmail()?.let { email ->
            db.collection("carts")
                .document(email)
                .collection("items")
        }

    fun addItem(productId: String, item: CartItem) {
        cartRef()?.document(productId)?.set(item)
    }

    fun updateQty(itemId: String, qty: Int) {
        cartRef()?.document(itemId)?.update("qty", qty)
    }

    fun deleteItem(itemId: String) {
        cartRef()?.document(itemId)?.delete()
    }

    //kosongkan semua
    fun clearCart(onComplete: () -> Unit = {}) {
        val ref = cartRef() ?: return
        ref.get().addOnSuccessListener { snap ->
            val batch = db.batch()
            snap.documents.forEach {
                batch.delete(it.reference)
            }
            batch.commit().addOnSuccessListener { onComplete() }
        }
    }

    fun getCart(
        onSuccess: (List<Pair<String, CartItem>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ref = cartRef()
        if (ref == null) {
            onSuccess(emptyList())
            return
        }

        ref.get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull {
                    val item = it.toObject(CartItem::class.java)
                    item?.let { ci -> it.id to ci }
                }
                onSuccess(list)
            }
            .addOnFailureListener(onError)
    }
}
