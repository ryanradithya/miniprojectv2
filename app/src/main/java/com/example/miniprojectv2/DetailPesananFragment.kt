package com.example.miniprojectv2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetailPesananFragment : Fragment() {

    private var selectedRating = 0f
    private val starViews = mutableListOf<ImageView>()
    private var productAlreadyReviewed = false
    private var allowReview = false

    private lateinit var rvReviews: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter

    private var productName: String = ""
    private var transactionId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_detail_pesanan, container, false)

        // ===============================
        // Ambil transactionId
        // ===============================
        transactionId = arguments?.getString("transaction_id") ?: ""

        if (transactionId.isEmpty()) {
            Toast.makeText(requireContext(), "Transaction ID tidak ditemukan", Toast.LENGTH_LONG).show()
            return v
        }

        // ===============================
        // User aktif
        // ===============================
        val prefs = requireContext().getSharedPreferences("UserPrefs", 0)
        val activeUser = prefs.getString("active_username", "User") ?: "User"

        // ===============================
        // Komponen Review
        // ===============================
        val etReview: EditText = v.findViewById(R.id.et_review)
        val ratingStars: LinearLayout = v.findViewById(R.id.rating_stars)
        val btnSubmit: Button = v.findViewById(R.id.btn_submit_review)

        // ===============================
        // RecyclerView Review
        // ===============================
        rvReviews = v.findViewById(R.id.rv_reviews)
        rvReviews.layoutManager = LinearLayoutManager(requireContext())
        rvReviews.isNestedScrollingEnabled = false
        reviewAdapter = ReviewAdapter(emptyList())
        rvReviews.adapter = reviewAdapter

        // ===============================
        // LOAD DETAIL PESANAN (INI INTI PERBAIKAN)
        // ===============================
        TransactionManager.getTransactionById(
            transactionId,
            onComplete = { trx ->

                if (trx == null) {
                    Toast.makeText(requireContext(), "Pesanan tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@getTransactionById
                }

                val firstItem = trx.items.firstOrNull()
                val total = trx.items.sumOf { it.price * it.qty }

                productName = firstItem?.name ?: ""

                v.findViewById<TextView>(R.id.tv_product_name).text =
                    productName.ifEmpty { "(Item kosong)" }

                v.findViewById<TextView>(R.id.tv_product_price).text =
                    "Rp ${firstItem?.price ?: 0}"

                v.findViewById<TextView>(R.id.tv_product_qty).text =
                    "x${firstItem?.qty ?: 0}"

                v.findViewById<TextView>(R.id.tv_product_total).text =
                    "Total: Rp $total"

                v.findViewById<TextView>(R.id.tv_product_status).text =
                    "Status: ${trx.status}"

                v.findViewById<TextView>(R.id.tv_product_expedition).text =
                    "Expedisi: ${trx.expedition}"

                v.findViewById<TextView>(R.id.tv_product_tracking).text =
                    trx.trackingNumber?.let { "Resi: $it" } ?: ""

                v.findViewById<TextView>(R.id.tv_product_date).text =
                    "Tanggal: ${trx.date}"

                // ===============================
                // Review permission
                // ===============================
                allowReview = trx.status.contains("selesai", ignoreCase = true)

                if (!allowReview) {
                    disableReview(etReview, btnSubmit)
                }

                // load review setelah productName valid
                loadReviews(activeUser, etReview, btnSubmit)
            },
            onError = {
                Toast.makeText(requireContext(), "Gagal memuat pesanan", Toast.LENGTH_SHORT).show()
            }
        )

        // ===============================
        // Generate Bintang Rating
        // ===============================
        for (i in 1..5) {
            val star = ImageView(requireContext())
            val size = (32 * resources.displayMetrics.density).toInt()
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(6, 0, 6, 0)
            star.layoutParams = params
            star.setImageResource(R.drawable.ic_star_empty)

            star.setOnClickListener {
                if (!allowReview || productAlreadyReviewed) {
                    Toast.makeText(
                        requireContext(),
                        "Rating hanya bisa setelah pesanan selesai.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                selectedRating = i.toFloat()
                starViews.forEachIndexed { index, img ->
                    img.setImageResource(
                        if (index < i) R.drawable.ic_star_full
                        else R.drawable.ic_star_empty
                    )
                }
            }

            starViews.add(star)
            ratingStars.addView(star)
        }

        // ===============================
        // Submit Review
        // ===============================
        btnSubmit.setOnClickListener {
            val comment = etReview.text.toString().trim()

            if (selectedRating == 0f || comment.isEmpty()) {
                Toast.makeText(requireContext(), "Isi rating dan komentar dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ProductRepository.addReviewToProduct(
                productName = productName,
                reviewer = activeUser,
                transactionId = transactionId,
                comment = comment,
                rating = selectedRating,
                onComplete = {
                    Toast.makeText(requireContext(), "Ulasan berhasil dikirim!", Toast.LENGTH_LONG).show()
                    etReview.setText("")
                    selectedRating = 0f
                    starViews.forEach { it.setImageResource(R.drawable.ic_star_empty) }
                    loadReviews(activeUser, etReview, btnSubmit)
                },
                onError = {
                    Toast.makeText(requireContext(), it.message ?: "Gagal mengirim ulasan", Toast.LENGTH_SHORT).show()
                }
            )
        }

        return v
    }

    private fun loadReviews(
        activeUser: String,
        etReview: EditText,
        btnSubmit: Button
    ) {
        if (productName.isEmpty()) return

        ProductRepository.findProductByName(
            productName,
            onComplete = { product ->
                if (product == null) return@findProductByName

                val sortedReviews = product.reviews.sortedByDescending { it.date }
                rvReviews.adapter = ReviewAdapter(sortedReviews)

                productAlreadyReviewed =
                    product.reviews.any {
                        it.reviewerName == activeUser &&
                                it.transactionId == transactionId
                    }

                if (productAlreadyReviewed) {
                    disableReview(etReview, btnSubmit)
                }
            },
            onError = {
                Toast.makeText(requireContext(), "Gagal memuat review", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun disableReview(et: EditText, btn: Button) {
        et.isEnabled = false
        btn.isEnabled = false
        starViews.forEach { it.isEnabled = false }
    }
}
