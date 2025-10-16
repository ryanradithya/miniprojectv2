package com.example.miniprojectv2

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

object CartManager {
    val items = mutableListOf<CartItem>()
}

data class CartItem(val name: String, val price: Int, var qty: Int = 1)

class ProductDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_product_detail, container, false)

        // 🔹 Ambil data dari argument
        val name = arguments?.getString("product_name") ?: "Produk"
        val price = arguments?.getInt("product_price") ?: 0
        val stock = arguments?.getInt("product_stock") ?: 0
        val description = arguments?.getString("product_description") ?: "Tidak ada deskripsi!"
        val imageUriString = arguments?.getString("product_image_uri")

        // 🔹 Hubungkan View
        val imageView: ImageView = v.findViewById(R.id.product_image)
        val tvTitle: TextView = v.findViewById(R.id.product_title)
        val tvPrice: TextView = v.findViewById(R.id.product_price)
        val tvStock: TextView = v.findViewById(R.id.product_stock)
        val tvDesc: TextView = v.findViewById(R.id.product_desc)
        val btnAdd: View = v.findViewById(R.id.btn_add_cart)
        val btnPlus: TextView = v.findViewById(R.id.btn_plus)
        val btnMinus: TextView = v.findViewById(R.id.btn_minus)
        val tvQty: TextView = v.findViewById(R.id.tv_qty)

        val ratingStars: LinearLayout = v.findViewById(R.id.rating_stars)
        val tvAverageRating: TextView = v.findViewById(R.id.tv_average_rating)
        val reviewContainer: LinearLayout = v.findViewById(R.id.review_container)

        // 🔹 Set info produk
        tvTitle.text = name
        tvPrice.text = "Rp $price"
        tvStock.text = "Stok : $stock"
        tvDesc.text = description

        // 🔹 Gambar produk
        if (!imageUriString.isNullOrEmpty()) {
            imageView.setImageURI(Uri.parse(imageUriString))
        } else {
            imageView.setImageResource(R.drawable.ic_product_placeholder)
        }

        if (stock == 0) {
            btnAdd.isEnabled = false
            btnAdd.alpha = 0.5f
            Toast.makeText(requireContext(), "Stok habis, tidak bisa dibeli", Toast.LENGTH_SHORT).show()
        }


        // 🔹 Expand/collapse deskripsi
        tvDesc.maxLines = 2
        tvDesc.ellipsize = TextUtils.TruncateAt.END
        var isExpanded = false
        tvDesc.setOnClickListener {
            isExpanded = !isExpanded
            tvDesc.maxLines = if (isExpanded) Int.MAX_VALUE else 2
            tvDesc.ellipsize = if (isExpanded) null else TextUtils.TruncateAt.END
            tvDesc.requestLayout()
            tvDesc.invalidate()
        }

        // 🔹 Quantity control
        var quantity = 1
        fun updateQty() {
            tvQty.text = quantity.toString()
        }

        // Tombol tambah
        btnPlus.setOnClickListener {
            if (quantity < stock) {
                quantity++
                updateQty()
            } else {
                Toast.makeText(requireContext(), "Stok tidak mencukupi!", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol kurang
        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQty()
            }
        }

        // 🔹 Tambah ke keranjang
        btnAdd.setOnClickListener {
            if (price <= 0) {
                Toast.makeText(requireContext(), "Harga produk tidak valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val existing = CartManager.items.find { it.name == name }
            val totalQty = (existing?.qty ?: 0) + quantity

            if (totalQty > stock) {
                Toast.makeText(requireContext(), "Jumlah melebihi stok tersedia!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (stock == 0) {
                Toast.makeText(requireContext(), "Stok produk ini sudah habis!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (existing != null) existing.qty += quantity
            else CartManager.items.add(CartItem(name, price, quantity))

            Toast.makeText(requireContext(), "Ditambahkan $quantity × \"$name\" ke keranjang!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_p_to_cart)
        }


        // 🔹 Ambil review dari repository
        val product = ProductRepository.findProductByName(name)
        val reviews = product?.reviews ?: emptyList()

        // 🔹 Hitung dan tampilkan rating rata-rata
        if (reviews.isEmpty()) {
            tvAverageRating.text = "Belum ada ulasan"
            ratingStars.removeAllViews()
        } else {
            val avgRating = reviews.map { it.rating }.average().toFloat()
            tvAverageRating.text = String.format("%.1f", avgRating)

            ratingStars.removeAllViews()
            val fullStars = avgRating.toInt()
            val halfStar = (avgRating - fullStars) >= 0.5f

            for (i in 1..5) {
                val star = ImageView(requireContext())
                when {
                    i <= fullStars -> star.setImageResource(R.drawable.ic_star_full)
                    i == fullStars + 1 && halfStar -> star.setImageResource(R.drawable.ic_star_half)
                    else -> star.setImageResource(R.drawable.ic_star_empty)
                }
                val size = (20 * resources.displayMetrics.density).toInt()
                val params = LinearLayout.LayoutParams(size, size)
                params.setMargins(4, 0, 4, 0)
                star.layoutParams = params
                ratingStars.addView(star)
            }
        }

        // 🔹 Tampilkan review dalam card
        reviewContainer.removeAllViews()
        if (reviews.isEmpty()) {
            val noReview = TextView(requireContext())
            noReview.text = "Belum ada ulasan untuk produk ini."
            noReview.setPadding(16, 16, 16, 16)
            reviewContainer.addView(noReview)
        } else {
            for (review in reviews) {
                val card = inflater.inflate(R.layout.item_review_card, reviewContainer, false)
                val tvReviewer = card.findViewById<TextView>(R.id.tv_reviewer_name)
                val tvComment = card.findViewById<TextView>(R.id.tv_comment)
                val tvRating = card.findViewById<TextView>(R.id.tv_review_rating)

                tvReviewer.text = review.reviewerName
                tvComment.text = review.comment
                tvRating.text = "⭐ ${review.rating}"

                tvComment.maxLines = 2
                tvComment.ellipsize = TextUtils.TruncateAt.END

                reviewContainer.addView(card)
            }
        }

        return v
    }
}
