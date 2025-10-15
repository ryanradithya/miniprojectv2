package com.example.miniprojectv2

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private val items: MutableList<Product>,
    private val isRekomendasi: Boolean = false,
    private val isSeller: Boolean = false
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // =========================
    // 🔹 ViewHolder
    // =========================
    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView? =
            view.findViewById(R.id.product_tile) ?: view.findViewById(R.id.rekomendasi_tile)
        val title: TextView? =
            view.findViewById(R.id.product_title) ?: view.findViewById(R.id.rekomendasi_title)
        val price: TextView? =
            view.findViewById(R.id.product_price) ?: view.findViewById(R.id.rekomendasi_price)
        val image: ImageView? =
            view.findViewById(R.id.product_image) ?: view.findViewById(R.id.rekomendasi_image)

        // Tombol khusus untuk mode Seller
        val btnEdit: ImageButton? = view.findViewById(R.id.btn_edit)
        val btnDelete: ImageButton? = view.findViewById(R.id.btn_delete)
    }

    // =========================
    // 🔹 ViewHolder Creation
    // =========================
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val layout = when {
            isRekomendasi -> R.layout.item_rekomendasi
            isSeller -> R.layout.item_product_seller
            else -> R.layout.item_product
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ProductViewHolder(view)
    }

    // =========================
    // 🔹 Data Binding
    // =========================
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = items[position]

        holder.title?.text = product.name
        holder.price?.text = "Rp ${product.price}"

        // Gambar produk (bisa dari URI atau fallback placeholder)
        if (!product.imageUri.isNullOrEmpty()) {
            holder.image?.setImageURI(Uri.parse(product.imageUri))
        } else {
            holder.image?.setImageResource(R.drawable.ic_product_placeholder)
        }

        // Mode Seller (tampilkan tombol edit & delete)
        if (isSeller) {
            holder.btnEdit?.visibility = View.VISIBLE
            holder.btnDelete?.visibility = View.VISIBLE

            holder.btnEdit?.setOnClickListener {
                Log.d("ProductAdapter", "Edit produk: ${product.name}")
                val bundle = Bundle().apply {
                    putString("edit_mode", "true")
                    putString("product_name", product.name)
                    putInt("product_price", product.price)
                    putInt("product_stock", product.stock)
                    putString("product_description", product.description)
                    putString("product_category", product.category)
                    putString("product_image_uri", product.imageUri)
                }
                it.findNavController().navigate(R.id.jualFragment, bundle)
            }

            holder.btnDelete?.setOnClickListener {
                Log.d("ProductAdapter", "Hapus produk: ${product.name}")
                val removed = ProductRepository.removeProduct(product)
                if (removed) {
                    items.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(
                        holder.itemView.context,
                        "Produk dihapus",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Gagal menghapus produk",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            holder.card?.setOnClickListener(null) // Nonaktifkan klik untuk buka detail

        } else {
            // Mode Pembeli (klik buka detail)
            holder.btnEdit?.visibility = View.GONE
            holder.btnDelete?.visibility = View.GONE

            holder.card?.setOnClickListener {
                Log.d("ProductAdapter", "Klik produk: ${product.name}")
                try {
                    val b = Bundle().apply {
                        putString("product_name", product.name)
                        putInt("product_price", product.price)
                        putString("product_description", product.description)
                        putInt("product_stock", product.stock)
                        putString("product_category", product.category)
                        putString("product_image_uri", product.imageUri)
                    }
                    it.findNavController().navigate(R.id.productDetailFragment, b)
                } catch (e: Exception) {
                    Log.e("ProductAdapter", "Error saat navigate", e)
                }
            }
        }
    }

    // =========================
    // 🔹 Item Count
    // =========================
    override fun getItemCount(): Int = items.size

    // =========================
    // 🔹 Update Data (untuk Filter)
    // =========================
    fun updateData(newList: List<Product>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
