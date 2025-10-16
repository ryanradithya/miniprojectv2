package com.example.miniprojectv2

object ProductRepository {
    // Produk utama (mutable supaya bisa ditambah)
    val produkUtama = mutableListOf(
        Product(
            "Kamera Analog A",
            1200000,
            "android.resource://com.example.miniprojectv2/drawable/kamera_a",
            10,
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend.",
            "Kamera Analog"),
        Product(
            "Kamera Analog B",
            900000,
            "android.resource://com.example.miniprojectv2/drawable/kamera_a",
            10,
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend.",
            "Kamera Analog"),
        Product(
            "Kamera Analog C",
            750000,
            "android.resource://com.example.miniprojectv2/drawable/kamera_a",
            10,
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend.",
            "Kamera Analog"),
        Product("Film Kodak Gold",
            120000,
            "android.resource://com.example.miniprojectv2/drawable/roll_a",
            10,
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend.",
            "Roll Film"),
        Product(
            "Lensa 50mm f/1.8",
            850000,
            "android.resource://com.example.miniprojectv2/drawable/lensa_1",
            10,
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend.",
            "Lensa Analog"),
        Product(
            "Tas Kamera Vintage",
            90000,
            "android.resource://com.example.miniprojectv2/drawable/tas_1",
            10,
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend.",
            "Tas Kamera")
    )

    // Produk rekomendasi (fix, tidak bisa diubah runtime, jadi pakai list biasa)
    val rekomendasiProduk = listOf(
        Product("Film Kodak Gold",
            120000,
            "android.resource://com.example.miniprojectv2/drawable/roll_a",
            10,
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend.",
            "Roll Film"),
        Product(
            "Lensa 50mm f/1.8",
            850000,
            "android.resource://com.example.miniprojectv2/drawable/lensa_1",
            10,
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend.",
            "Lensa Analog"),
        Product(
            "Tas Kamera Vintage",
            90000,
            "android.resource://com.example.miniprojectv2/drawable/tas_1",
            10,
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed faucibus lorem tortor, sit amet sodales nisl sagittis auctor. Suspendisse pretium facilisis tincidunt. Phasellus non pretium nisi. Nunc lacus nisi, eleifend.",
            "Tas Kamera")
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

    fun updateStock(productName: String, quantity: Int): Boolean {
        // Cari produk berdasarkan nama
        val product = produkUtama.find { it.name == productName }

        // Jika produk ditemukan
        return if (product != null) {
            // Pastikan stok cukup
            if (product.stock >= quantity) {
                // Kurangi stok produk
                val updatedProduct = product.copy(stock = product.stock - quantity)
                updateProduct(product, updatedProduct)
                true // Berhasil update
            } else {
                false // Gagal karena stok tidak cukup
            }
        } else {
            false // Produk tidak ditemukan
        }
    }

}