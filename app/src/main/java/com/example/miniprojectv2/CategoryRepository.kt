package com.example.miniprojectv2.data

import com.example.miniprojectv2.utils.toTitleCase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object CategoryRepository {

    fun getAllCategories(
        onSuccess: (List<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        Firebase.firestore
            .collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                val normalizedSet = mutableSetOf<String>()

                snapshot.documents.forEach { doc ->
                    val raw = doc.getString("category") ?: return@forEach
                    normalizedSet.add(raw.trim().lowercase())
                }

                onSuccess(
                    normalizedSet
                        .map { it.toTitleCase() }
                        .sorted()
                )
            }
            .addOnFailureListener { onError(it) }
    }
}
