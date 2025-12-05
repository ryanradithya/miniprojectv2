package com.example.miniprojectv2

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object ProductRepository {

    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("products")

    // =====================================================
    // GET ALL PRODUCTS
    // =====================================================
    fun getProducts(
        onComplete: (List<Pair<String, Product>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        col.get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply { id = doc.id }?.let { Pair(doc.id, it) }
                }
                onComplete(list)
            }
            .addOnFailureListener(onError)
    }

    // =====================================================
    // ADD PRODUCT
    // =====================================================
    fun addProduct(
        product: Product,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Generate a new document reference
        val docRef = col.document()
        val productWithId = product.copy(id = docRef.id)

        docRef.set(productWithId)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener(onError)
    }

    // =====================================================
    // FIND PRODUCT ID BY NAME
    // =====================================================
    fun findProductIdByName(
        name: String,
        onResult: (String?) -> Unit
    ) {
        col.whereEqualTo("name", name)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) onResult(null)
                else onResult(result.documents.first().id)
            }
            .addOnFailureListener { onResult(null) }
    }

    // =====================================================
    // FIND PRODUCT BY NAME (RETURN PRODUCT)
    // =====================================================
    fun findProductByName(
        name: String,
        onComplete: (Product?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        col.whereEqualTo("name", name)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) onComplete(null)
                else onComplete(result.documents.first().toObject(Product::class.java))
            }
            .addOnFailureListener(onError)
    }

    // =====================================================
    // UPDATE PRODUCT BY DOCUMENT ID
    // =====================================================
    fun updateProductById(
        productId: String,
        updated: Product,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        col.document(productId)
            .set(updated)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener(onError)
    }

    // =====================================================
    // UPDATE PRODUCT BY NAME (OLD METHOD, STILL AVAILABLE)
    // =====================================================
    fun updateProduct(
        name: String,
        updated: Product,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        col.whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    col.document(doc.id).set(updated)
                }
                onComplete()
            }
            .addOnFailureListener(onError)
    }

    // =====================================================
    // DELETE PRODUCT (BY NAME)
    // =====================================================
    fun deleteProduct(
        name: String,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        col.whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    col.document(doc.id).delete()
                }
                onComplete()
            }
            .addOnFailureListener(onError)
    }

    // =====================================================
    // REDUCE STOCK
    // =====================================================
    fun reduceStock(
        productName: String,
        qty: Int,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        col.whereEqualTo("name", productName)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    onError(Exception("Produk tidak ditemukan"))
                    return@addOnSuccessListener
                }

                val doc = result.documents.first()
                val product = doc.toObject(Product::class.java) ?: run {
                    onError(Exception("Produk invalid"))
                    return@addOnSuccessListener
                }

                if (product.stock < qty) {
                    onError(Exception("Stock tidak cukup"))
                    return@addOnSuccessListener
                }

                val updated = product.copy(stock = product.stock - qty)

                col.document(doc.id)
                    .set(updated)
                    .addOnSuccessListener { onComplete() }
                    .addOnFailureListener(onError)
            }
            .addOnFailureListener(onError)
    }

    // =====================================================
    // ADD REVIEW (SUBCOLLECTION)
    // =====================================================
    fun addReviewToProduct(
        productName: String,
        reviewer: String,
        comment: String,
        rating: Float,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        col.whereEqualTo("name", productName)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    onError(Exception("Produk tidak ditemukan"))
                    return@addOnSuccessListener
                }

                val productId = result.documents.first().id

                val review = Review(
                    reviewerName = reviewer,
                    comment = comment,
                    rating = rating,
                    date = System.currentTimeMillis().toString()
                )

                col.document(productId)
                    .collection("reviews")
                    .add(review)
                    .addOnSuccessListener { onComplete() }
                    .addOnFailureListener(onError)
            }
            .addOnFailureListener(onError)
    }

    // =====================================================
    // GET TOP RATED PRODUCTS
    // =====================================================
    fun getTopRatedProducts(
        onComplete: (List<Product>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        col.orderBy("rating", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { result ->
                val list = result.map { it.toObject(Product::class.java) }
                onComplete(list)
            }
            .addOnFailureListener(onError)
    }

    fun getReviews(
        productName: String,
        onComplete: (List<Review>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("products")
            .whereEqualTo("name", productName)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    onComplete(emptyList())
                    return@addOnSuccessListener
                }

                val productId = result.documents[0].id

                db.collection("products")
                    .document(productId)
                    .collection("reviews")
                    .get()
                    .addOnSuccessListener { reviews ->
                        val list = reviews.map { it.toObject(Review::class.java) }
                        onComplete(list)
                    }
                    .addOnFailureListener(onError)
            }
            .addOnFailureListener(onError)
    }

}
