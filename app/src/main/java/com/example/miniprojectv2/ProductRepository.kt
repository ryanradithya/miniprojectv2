package com.example.miniprojectv2

import com.example.miniprojectv2.Product

object ProductRepository {
    // Produk utama (mutable supaya bisa ditambah)
    val produkUtama = mutableListOf(
        Product("Kamera Analog A", 1200000, R.drawable.kamera_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend."),
        Product("Kamera Analog B", 900000, R.drawable.kamera_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend."),
        Product("Kamera Analog C", 750000, R.drawable.kamera_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend."),
        Product("Film Kodak Gold", 120000, R.drawable.roll_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend."),
        Product("Lensa 50mm f/1.8", 850000, R.drawable.roll_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend."),
        Product("Tas Kamera Vintage", 250000, R.drawable.roll_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend.")
    )

    // Produk rekomendasi (fix, tidak bisa diubah runtime, jadi pakai list biasa)
    val rekomendasiProduk = listOf(
        Product("Film Kodak Gold", 120000, R.drawable.roll_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend."),
        Product("Lensa 50mm f/1.8", 850000, R.drawable.roll_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend."),
        Product("Tas Kamera Vintage", 250000, R.drawable.roll_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend.")
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

    // ✅ Menghapus semua produk utama (opsional)
    fun clearProducts() {
        produkUtama.clear()
    }
}
