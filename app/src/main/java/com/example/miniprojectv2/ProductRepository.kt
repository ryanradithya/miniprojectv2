package com.example.miniprojectv2

object ProductRepository {
    // Produk utama (mutable supaya bisa ditambah)
    val produkUtama = mutableListOf(
        Product("Kamera Analog A", 1200000, R.drawable.kamera_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend."),
        Product("Kamera Analog B", 900000, R.drawable.kamera_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend."),
        Product("Kamera Analog C", 750000, R.drawable.kamera_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend.")
    )

    // Produk rekomendasi (fix, tidak bisa diubah runtime, jadi pakai list biasa)
    val rekomendasiProduk = listOf(
        Product("Film Kodak Gold", 120000, R.drawable.roll_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend."),
        Product("Lensa 50mm f/1.8", 850000, R.drawable.roll_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend."),
        Product("Tas Kamera Vintage", 250000, R.drawable.roll_a, 10, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend.")
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
