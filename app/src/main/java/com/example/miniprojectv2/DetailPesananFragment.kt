package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetailPesananFragment : Fragment() {

    // ===== MODE =====
    private lateinit var mode: String // "buyer" | "seller"

    // ===== DATA =====
    private lateinit var transactionId: String
    private var productName: String = ""
    private var allowReview = false
    private var productAlreadyReviewed = false
    private var selectedRating = 0f

    // ===== UI =====
    private lateinit var rvReviews: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private val starViews = mutableListOf<ImageView>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_detail_pesanan, container, false)

        // ===============================
        // Ambil Argument
        // ===============================
        transactionId = arguments?.getString("transaction_id") ?: ""
        mode = arguments?.getString("mode") ?: "buyer"

        if (transactionId.isEmpty()) {
            Toast.makeText(requireContext(), "Transaction ID tidak ditemukan", Toast.LENGTH_LONG).show()
            return v
        }

        val prefs = requireContext()
            .getSharedPreferences("UserPrefs", 0)
        val activeUser = prefs.getString("active_username", "User") ?: "User"

        // ===============================
        // UI REFERENCES
        // ===============================
        val tvName = v.findViewById<TextView>(R.id.tv_product_name)
        val tvPrice = v.findViewById<TextView>(R.id.tv_product_price)
        val tvQty = v.findViewById<TextView>(R.id.tv_product_qty)
        val tvTotal = v.findViewById<TextView>(R.id.tv_product_total)
        val tvStatus = v.findViewById<TextView>(R.id.tv_product_status)
        val tvExpedition = v.findViewById<TextView>(R.id.tv_product_expedition)
        val tvTracking = v.findViewById<TextView>(R.id.tv_product_tracking)
        val tvDate = v.findViewById<TextView>(R.id.tv_product_date)

        // ===== Review Section =====
        val reviewSection = v.findViewById<LinearLayout>(R.id.review_section)
        val etReview = v.findViewById<EditText>(R.id.et_review)
        val ratingStars = v.findViewById<LinearLayout>(R.id.rating_stars)
        val btnSubmit = v.findViewById<Button>(R.id.btn_submit_review)

        // ===== Seller Action Section =====
        val sellerSection = v.findViewById<LinearLayout>(R.id.seller_action_section)
        val btnAccept = v.findViewById<Button>(R.id.btn_accept_order)
        val btnShip = v.findViewById<Button>(R.id.btn_ship_order)

        // ===============================
        // MODE SETUP
        // ===============================
        when (mode) {
            "buyer" -> {
                reviewSection.visibility = View.VISIBLE
                sellerSection.visibility = View.GONE
            }
            "seller" -> {
                reviewSection.visibility = View.GONE
                sellerSection.visibility = View.VISIBLE
            }
        }

        // ===============================
        // RecyclerView Review
        // ===============================
        rvReviews = v.findViewById(R.id.rv_reviews)
        rvReviews.layoutManager = LinearLayoutManager(requireContext())
        rvReviews.isNestedScrollingEnabled = false
        reviewAdapter = ReviewAdapter(emptyList())
        rvReviews.adapter = reviewAdapter

        // ===============================
        // LOAD TRANSACTION
        // ===============================
        TransactionManager.getTransactionById(
            transactionId,
            onSuccess = { trx ->

                if (trx == null) {
                    Toast.makeText(requireContext(), "Pesanan tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@getTransactionById
                }

                val firstItem = trx.items.firstOrNull()
                val total = trx.items.sumOf { it.price * it.qty }

                productName = firstItem?.name ?: ""

                tvName.text = productName.ifEmpty { "(Item kosong)" }
                tvPrice.text = "Rp ${firstItem?.price ?: 0}"
                tvQty.text = "x${firstItem?.qty ?: 0}"
                tvTotal.text = "Total: Rp $total"
                tvStatus.text = "Status: ${trx.status}"
                tvExpedition.text = "Ekspedisi: ${trx.expedition}"
                tvTracking.text = trx.trackingNumber?.let { "Resi: $it" } ?: ""
                tvDate.text = "Tanggal: ${trx.date}"

                // ===== Review Permission =====
                allowReview = trx.status.contains("selesai", true)
                if (!allowReview) disableReview(etReview, btnSubmit)

                // ===== Seller Button State =====
                if (mode == "seller") {
                    btnAccept.isEnabled = false
                    btnShip.isEnabled = false

                    when (trx.status) {
                        "Pesanan Masuk" -> {
                            btnAccept.isEnabled = true
                        }

                        "Pesanan Diproses" -> {
                            btnShip.isEnabled = true
                        }
                    }
                }

                loadReviews(activeUser, etReview, btnSubmit)
            },
            onError = {
                Toast.makeText(requireContext(), "Gagal memuat detail pesanan", Toast.LENGTH_SHORT).show()
            }
        )

        // ===============================
        // Generate Rating Stars
        // ===============================
        for (i in 1..5) {
            val star = ImageView(requireContext())
            val size = (32 * resources.displayMetrics.density).toInt()
            star.layoutParams = LinearLayout.LayoutParams(size, size)
            star.setImageResource(R.drawable.ic_star_empty)

            star.setOnClickListener {
                if (!allowReview || productAlreadyReviewed) return@setOnClickListener
                selectedRating = i.toFloat()
                starViews.forEachIndexed { idx, img ->
                    img.setImageResource(
                        if (idx < i) R.drawable.ic_star_full
                        else R.drawable.ic_star_empty
                    )
                }
            }

            starViews.add(star)
            ratingStars.addView(star)
        }

        // ===============================
        // Submit Review (BUYER)
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
                    Toast.makeText(requireContext(), "Ulasan dikirim!", Toast.LENGTH_SHORT).show()
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

        // ===============================
        // SELLER ACTION
        // ===============================
        btnAccept.setOnClickListener {
            btnAccept.isEnabled = false

            TransactionManager.updateStatus(
                transactionId,
                "Pesanan Diproses",
                onSuccess = {
                    Toast.makeText(requireContext(), "Pesanan diterima", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            )
        }


        btnShip.setOnClickListener {
            val dialogView = layoutInflater.inflate(
                R.layout.dialog_input_resi,
                null,
                false
            )

            val etResi = dialogView.findViewById<EditText>(R.id.et_resi)
            val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
            val btnSubmit = dialogView.findViewById<Button>(R.id.btn_submit)

            val dialog = android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create()

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnSubmit.setOnClickListener {
                val resi = etResi.text.toString().trim()

                if (resi.isEmpty()) {
                    etResi.error = "Nomor resi wajib diisi"
                    return@setOnClickListener
                }

                btnSubmit.isEnabled = false

                TransactionManager.updateStatus(
                    transactionId,
                    "Pesanan Dikirim",
                    trackingNumber = resi,
                    onSuccess = {
                        Toast.makeText(
                            requireContext(),
                            "Pesanan berhasil dikirim",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                        findNavController().popBackStack()
                    },
                    onError = {
                        btnSubmit.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            "Gagal mengirim pesanan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            dialog.show()
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

                rvReviews.adapter =
                    ReviewAdapter(product.reviews.sortedByDescending { it.date })

                productAlreadyReviewed =
                    product.reviews.any {
                        it.reviewerName == activeUser &&
                                it.transactionId == transactionId
                    }

                if (productAlreadyReviewed) disableReview(etReview, btnSubmit)
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
