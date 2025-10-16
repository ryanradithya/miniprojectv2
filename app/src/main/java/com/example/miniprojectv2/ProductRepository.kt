package com.example.miniprojectv2

import com.google.type.Date
import java.text.SimpleDateFormat
import java.util.Locale

object ProductRepository {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Contoh ulasan dummy
    private val reviewListA = listOf(
        Review("John Doe", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", 5f, "2025-10-10"),
        Review("Jane Smith", "Produk sesuai deskripsi, hasil foto memuaskan!", 4.5f, "2025-10-12")
    )

    private val reviewListB = listOf(
        Review("John Wick", "Bagus, tapi pengiriman agak lama.", 4f, "2025-10-13"),
        Review("Alice Johnson", "Kualitas build mantap, recommended.", 5f, "2025-10-11")
    )

    private val reviewListC = listOf(
        Review("Tony Stark", "Desain klasik, berfungsi dengan baik.", 4.8f, "2025-10-14")
    )

    private val reviewListRoll = listOf(
        Review("Bruce Wayne", "Film warna cerah dan tajam.", 5f, "2025-10-09"),
        Review("Clark Kent", "Hasil foto vintage banget, suka!", 4.7f, "2025-10-10")
    )

    private val reviewListLensa = listOf(
        Review("Natasha Romanoff", "Bokeh halus, fokus cepat!", 5f, "2025-10-15"),
        Review("Peter Parker", "Kualitas tajam di harga segini, worth it.", 4.9f, "2025-10-14")
    )

    private val reviewListTas = listOf(
        Review("Steve Rogers", "Tasnya kuat dan desainnya keren.", 4.8f, "2025-10-13")
    )

    // Produk utama (mutable supaya bisa ditambah)
    val produkUtama = mutableListOf(
        Product(
            name = "Kamera Analog A",
            price = 1200000,
            imageUri = "android.resource://com.example.miniprojectv2/drawable/kamera_a",
            stock = 10,
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent vitae lacus lobortis, rutrum metus nec, congue turpis. Donec non convallis libero. Phasellus facilisis egestas eros id vulputate. Nam pretium sollicitudin arcu, a varius purus sodales a. Vivamus tincidunt, velit in.",
            category = "Kamera Analog",
            rating = 4.7f,
            reviews = reviewListA
        ),
        Product(
            name = "Kamera Analog B",
            price = 900000,
            imageUri = "android.resource://com.example.miniprojectv2/drawable/kamera_a",
            stock = 10,
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor.",
            category = "Kamera Analog",
            rating = 4.4f,
            reviews = reviewListB
        ),
        Product(
            name = "Kamera Analog C",
            price = 750000,
            imageUri = "android.resource://com.example.miniprojectv2/drawable/kamera_a",
            stock = 10,
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor.",
            category = "Kamera Analog",
            rating = 4.8f,
            reviews = reviewListC
        ),
        Product(
            name = "Film Kodak Gold",
            price = 120000,
            imageUri = "android.resource://com.example.miniprojectv2/drawable/roll_a",
            stock = 10,
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor.",
            category = "Roll Film",
            rating = 4.9f,
            reviews = reviewListRoll
        ),
        Product(
            name = "Lensa 50mm f/1.8",
            price = 850000,
            imageUri = "android.resource://com.example.miniprojectv2/drawable/lensa_1",
            stock = 10,
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor.",
            category = "Lensa Analog",
            rating = 4.95f,
            reviews = reviewListLensa
        ),
        Product(
            name = "Tas Kamera Vintage",
            price = 90000,
            imageUri = "android.resource://com.example.miniprojectv2/drawable/tas_1",
            stock = 10,
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor.",
            category = "Tas Kamera",
            rating = 4.8f,
            reviews = reviewListTas
        )
    )

    // Produk rekomendasi
    val rekomendasiProduk = listOf(
        Product(
            name = "Film Kodak Gold",
            price = 120000,
            imageUri = "android.resource://com.example.miniprojectv2/drawable/roll_a",
            stock = 10,
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            category = "Roll Film",
            rating = 4.9f,
            reviews = reviewListRoll
        ),
        Product(
            name = "Lensa 50mm f/1.8",
            price = 850000,
            imageUri = "android.resource://com.example.miniprojectv2/drawable/lensa_1",
            stock = 10,
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            category = "Lensa Analog",
            rating = 4.95f,
            reviews = reviewListLensa
        ),
        Product(
            name = "Tas Kamera Vintage",
            price = 90000,
            imageUri = "android.resource://com.example.miniprojectv2/drawable/tas_1",
            stock = 10,
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            category = "Tas Kamera",
            rating = 4.8f,
            reviews = reviewListTas
        )
    )

    // ✅ Mendapatkan semua produk utama
    fun getProducts(): MutableList<Product> = produkUtama

    // ✅ Menambah produk baru
    fun addProduct(product: Product) {
        produkUtama.add(product)
    }

    // ✅ Menghapus produk
    fun removeProduct(product: Product): Boolean {
        return produkUtama.remove(product)
    }

    // ✅ Mengupdate produk
    fun updateProduct(oldProduct: Product, newProduct: Product) {
        val index = produkUtama.indexOf(oldProduct)
        if (index != -1) produkUtama[index] = newProduct
    }

    fun findProductByName(name: String): Product? {
        return produkUtama.find { it.name == name }
    }
    fun addReviewToProduct(productName: String, reviewer: String, comment: String, rating: Float): Boolean {
        val product = findProductByName(productName) ?: return false

        // Cegah user menulis ulang review
        if (product.reviews.any { it.reviewerName == reviewer }) return false

        val newReview = Review(reviewer, comment, rating, dateFormat.format(java.util.Date()))
        val updatedReviews = product.reviews.toMutableList().apply { add(newReview) }

        // Hitung ulang rata-rata rating
        val newAvg = updatedReviews.map { it.rating }.average().toFloat()

        val updatedProduct = product.copy(
            reviews = updatedReviews,
            rating = newAvg
        )

        updateProduct(product, updatedProduct)
        return true
    }

    // ✅ Menghapus semua produk utama (opsional)
    fun clearProducts() {
        produkUtama.clear()
    }
}
