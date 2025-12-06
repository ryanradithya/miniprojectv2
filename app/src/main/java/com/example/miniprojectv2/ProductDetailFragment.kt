package com.example.miniprojectv2

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.io.IOException

object CartManager {
    val items = mutableListOf<CartItem>()
}

data class CartItem(val name: String, val price: Int, var qty: Int = 1)

class ProductDetailFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvStock: TextView
    private lateinit var tvDesc: TextView
    private lateinit var btnAdd: View
    private lateinit var btnPlus: TextView
    private lateinit var btnMinus: TextView
    private lateinit var tvQty: TextView
    private lateinit var ratingStars: LinearLayout
    private lateinit var tvAverageRating: TextView
    private lateinit var reviewContainer: LinearLayout

    private var quantity = 1
    private var currentStock = 0
    private var productName: String = ""
    private var productPrice: Int = 0
    private var productImageUri: String? = null
    private var productDescription: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ambil view
        imageView = view.findViewById(R.id.product_image)
        tvTitle = view.findViewById(R.id.product_title)
        tvPrice = view.findViewById(R.id.product_price)
        tvStock = view.findViewById(R.id.product_stock)
        tvDesc = view.findViewById(R.id.product_desc)
        btnAdd = view.findViewById(R.id.btn_add_cart)
        btnPlus = view.findViewById(R.id.btn_plus)
        btnMinus = view.findViewById(R.id.btn_minus)
        tvQty = view.findViewById(R.id.tv_qty)
        ratingStars = view.findViewById(R.id.rating_stars)
        tvAverageRating = view.findViewById(R.id.tv_average_rating)
        reviewContainer = view.findViewById(R.id.review_container)

        // Ambil data dari Bundle
        productName = arguments?.getString("product_name") ?: "Produk"
        productPrice = arguments?.getInt("product_price") ?: 0
        currentStock = arguments?.getInt("product_stock") ?: 0
        productDescription = arguments?.getString("product_description") ?: "-"
        productImageUri = arguments?.getString("product_image_uri")

        setupBasicInfo()
        setupDescriptionToggle()
        setupQtyButtons()
        setupAddToCart()

        loadProductDetailsFirestore()
    }

    // ============================
    // TAMPILKAN INFO DASAR PRODUK
    // ============================
    private fun setupBasicInfo() {
        tvTitle.text = productName
        tvPrice.text = "Rp $productPrice"
        tvStock.text = "Stok : $currentStock"
        tvDesc.text = productDescription

        if (currentStock == 0) {
            btnAdd.isEnabled = false
            btnAdd.alpha = 0.5f
            Toast.makeText(requireContext(), "Stok habis", Toast.LENGTH_SHORT).show()
        }
    }

    // ============================
    // DESKRIPSI BISA EXPAND
    // ============================
    private fun setupDescriptionToggle() {
        tvDesc.maxLines = 2
        tvDesc.ellipsize = TextUtils.TruncateAt.END
        var expanded = false

        tvDesc.setOnClickListener {
            expanded = !expanded
            tvDesc.maxLines = if (expanded) Int.MAX_VALUE else 2
            tvDesc.ellipsize = if (expanded) null else TextUtils.TruncateAt.END
        }
    }

    // ============================
    // QUANTITY COUNTER
    // ============================
    private fun setupQtyButtons() {
        updateQtyText()

        btnPlus.setOnClickListener {
            if (quantity < currentStock) {
                quantity++
                updateQtyText()
            } else {
                Toast.makeText(requireContext(), "Stok tidak cukup", Toast.LENGTH_SHORT).show()
            }
        }

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQtyText()
            }
        }
    }

    private fun updateQtyText() {
        tvQty.text = quantity.toString()
    }

    // ============================
    // ADD TO CART
    // ============================
    private fun setupAddToCart() {
        btnAdd.setOnClickListener {
            val existing = CartManager.items.find { it.name == productName }
            val totalQty = (existing?.qty ?: 0) + quantity

            if (totalQty > currentStock) {
                Toast.makeText(requireContext(), "Jumlah melebihi stok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (existing != null) {
                existing.qty += quantity
            } else {
                CartManager.items.add(CartItem(productName, productPrice, quantity))
            }

            Toast.makeText(requireContext(), "Ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_p_to_cart)
        }
    }

    // ============================
    // LOAD DETAIL PRODUK DARI FIRESTORE
    // ============================
    private fun loadProductDetailsFirestore() {
        ProductRepository.findProductByName(
            name = productName,
            onComplete = { product ->

                if (product == null) {
                    Log.e("ProductDetail", "Produk tidak ditemukan di database")
                    return@findProductByName
                }
                loadProductImage(imageView, productImageUri)
                // Update rating & review
                loadReviews()
            },
            onError = {
                Toast.makeText(requireContext(), "Gagal memuat data produk", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ============================
    // LOAD REVIEW DARI FIRESTORE
    // ============================
    private fun loadReviews() {
        ProductRepository.getReviews(
            productName = productName,
            onComplete = { list ->

                // tampilkan rating rata-rata
                if (list.isEmpty()) {
                    tvAverageRating.text = "Belum ada ulasan"
                    ratingStars.removeAllViews()
                } else {
                    val avg = list.map { it.rating }.average().toFloat()
                    tvAverageRating.text = String.format("%.1f", avg)
                    showStars(avg)
                }

                // tampilkan review list
                reviewContainer.removeAllViews()
                if (list.isEmpty()) {
                    val tv = TextView(requireContext()).apply {
                        text = "Belum ada ulasan"
                        setPadding(16, 16, 16, 16)
                    }
                    reviewContainer.addView(tv)
                } else {
                    for (r in list) {
                        val card = layoutInflater.inflate(R.layout.item_review_card, reviewContainer, false)
                        card.findViewById<TextView>(R.id.tv_reviewer_name).text = r.reviewerName
                        card.findViewById<TextView>(R.id.tv_comment).text = r.comment
                        card.findViewById<TextView>(R.id.tv_review_rating).text = "⭐ ${r.rating}"
                        reviewContainer.addView(card)
                    }
                }
            },
            onError = {
                Toast.makeText(requireContext(), "Gagal memuat ulasan", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ============================
    // MENAMPILKAN BINTANG RATING
    // ============================
    private fun showStars(avg: Float) {
        ratingStars.removeAllViews()

        val full = avg.toInt()
        val half = (avg - full) >= 0.5f

        for (i in 1..5) {
            val star = ImageView(requireContext())
            val size = (20 * resources.displayMetrics.density).toInt()
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(4, 0, 4, 0)
            star.layoutParams = params

            when {
                i <= full -> star.setImageResource(R.drawable.ic_star_full)
                i == full + 1 && half -> star.setImageResource(R.drawable.ic_star_half)
                else -> star.setImageResource(R.drawable.ic_star_empty)
            }

            ratingStars.addView(star)
        }
    }

    fun loadProductImage(imageView: ImageView, productImageUri: String?) {
        if (productImageUri.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.ic_product_placeholder)
            return
        }
        try {
                val imageId = productImageUri.removePrefix("server://")
                ImageHandler.getImage(requireContext(), imageId) { bytes ->
                    if (bytes != null) {
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imageView.post { imageView.setImageBitmap(bitmap) }
                    } else {
                        imageView.post { imageView.setImageResource(R.drawable.ic_product_placeholder) }
                    }
                }
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.ic_product_placeholder)
        }
    }
}
