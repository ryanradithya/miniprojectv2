package com.example.miniprojectv2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private val items: List<Product>,
    private val isRekomendasi: Boolean = false   // <-- default false (produk utama)
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView? = view.findViewById(R.id.product_tile) ?: view.findViewById(R.id.rekomendasi_tile)
        val title: TextView? = view.findViewById(R.id.product_title) ?: view.findViewById(R.id.rekomendasi_title)
        val price: TextView? = view.findViewById(R.id.product_price) ?: view.findViewById(R.id.rekomendasi_price)
        val image: ImageView? = view.findViewById(R.id.product_image) ?: view.findViewById(R.id.rekomendasi_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val layout = if (isRekomendasi) R.layout.item_rekomendasi else R.layout.item_product
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = items[position]
        holder.title?.text = product.name
        holder.price?.text = "Rp ${product.price}"
        holder.image?.setImageResource(product.imageRes)

        holder.card?.setOnClickListener {
            Log.d("ProductAdapter", "Klik produk: ${product.name}, harga: ${product.price}")
            try {
                val b = Bundle().apply {
                    putString("product_name", product.name)
                    putInt("product_price", product.price)
                }
                Log.d("ProductAdapter", "Navigasi ke detail dengan bundle: $b")
                it.findNavController().navigate(R.id.productDetailFragment, b)
            } catch (e: Exception) {
                Log.e("ProductAdapter", "Error saat navigate", e)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}