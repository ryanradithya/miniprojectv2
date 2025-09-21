package com.example.miniprojectv2

object ProductRepository {
    // Produk utama (mutable supaya bisa ditambah)
    val produkUtama = mutableListOf(
        Product("Kamera Analog A", 1200000, R.drawable.ic_product_placeholder),
        Product("Kamera Analog B", 900000, R.drawable.ic_product_placeholder),
        Product("Kamera Analog C", 750000, R.drawable.ic_product_placeholder)
    )

    // Produk rekomendasi (fix, tidak bisa diubah runtime, jadi pakai list biasa)
    val rekomendasiProduk = listOf(
        Product("Film Kodak Gold", 120000, R.drawable.ic_product_placeholder),
        Product("Lensa 50mm f/1.8", 850000, R.drawable.ic_product_placeholder),
        Product("Tas Kamera Vintage", 250000, R.drawable.ic_product_placeholder)
    )

    // Method untuk menambahkan produk baru ke list utama
    fun addProduct(product: Product) {
        produkUtama.add(product)
    }

    // (opsional) method untuk clear data produk utama
    fun clearProducts() {
        produkUtama.clear()
    }
}
