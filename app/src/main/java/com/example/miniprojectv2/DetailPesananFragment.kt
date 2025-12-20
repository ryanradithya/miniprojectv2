package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class DetailPesananFragment : Fragment() {

    private lateinit var mode: String
    private lateinit var transactionId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var sellerActionLocked = false

        val v = inflater.inflate(R.layout.fragment_detail_pesanan, container, false)

        val reviewContainer = v.findViewById<LinearLayout>(R.id.review_container)

        transactionId = arguments?.getString("transaction_id") ?: ""
        mode = arguments?.getString("mode") ?: "buyer"

        if (transactionId.isEmpty()) {
            Toast.makeText(requireContext(), "Transaction ID tidak ditemukan", Toast.LENGTH_LONG).show()
            return v
        }

        val prefs = requireContext().getSharedPreferences("UserPrefs", 0)
        val activeUser = prefs.getString("active_username", "User") ?: "User"

        val tvTotal = v.findViewById<TextView>(R.id.tv_product_total)
        val tvStatus = v.findViewById<TextView>(R.id.tv_product_status)
        val tvExpedition = v.findViewById<TextView>(R.id.tv_product_expedition)
        val tvTracking = v.findViewById<TextView>(R.id.tv_product_tracking)
        val tvDate = v.findViewById<TextView>(R.id.tv_product_date)
        val layoutProducts = v.findViewById<LinearLayout>(R.id.layout_products)
        val tvBuyer = v.findViewById<TextView>(R.id.tv_product_buyer)


        var buyer: String = ""
        var expedition: String = ""

        val sellerSection = v.findViewById<LinearLayout>(R.id.seller_action_section)
        val btnAccept = v.findViewById<Button>(R.id.btn_accept_order)
        val btnShip = v.findViewById<Button>(R.id.btn_ship_order)

        if (mode == "buyer") {
            reviewContainer.visibility = View.VISIBLE
            sellerSection.visibility = View.GONE
        } else {
            reviewContainer.visibility = View.GONE
            sellerSection.visibility = View.VISIBLE
        }


        TransactionManager.getTransactionById(
            transactionId,
            onSuccess = { trx ->

                if (trx == null) {
                    Toast.makeText(requireContext(), "Pesanan tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@getTransactionById
                }

                val total = trx.items.sumOf { it.price * it.qty }

                while (layoutProducts.childCount > 1) {
                    layoutProducts.removeViewAt(0)
                }

                reviewContainer.removeAllViews()
                reviewContainer.visibility = View.VISIBLE

                trx.items.forEach { item ->

                    val row = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 4, 0, 4)
                    }

                    row.addView(TextView(requireContext()).apply {
                        text = item.name
                        layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                    })

                    row.addView(TextView(requireContext()).apply {
                        text = "x${item.qty}"
                        setPadding(8, 0, 8, 0)
                    })

                    row.addView(TextView(requireContext()).apply {
                        text = "Rp ${item.price * item.qty}"
                    })

                    layoutProducts.addView(row, 0)
                }

                tvBuyer.text = "Pembeli: ${trx.buyer}"
                tvTotal.text = "Total: Rp $total"
                tvStatus.text = "Status: ${trx.status}"
                tvExpedition.text = "Ekspedisi: ${trx.expedition}"
                tvTracking.text = trx.trackingNumber?.let { "Resi: $it" } ?: ""
                tvDate.text = "Tanggal: ${formatDate(trx.date)}"


                if (mode == "buyer" && trx.status.contains("selesai", true)) {

                    trx.items.forEach { item ->

                        var alreadyReviewed = false
                        var selectedRating = 0f
                        var isSubmitting = false


                        val section = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(0, 16, 0, 16)
                        }

                        val title = TextView(requireContext()).apply {
                            text = "Beri Ulasan: ${item.name}"
                            textSize = 16f
                            setTypeface(null, android.graphics.Typeface.BOLD)
                        }

                        val starLayout = LinearLayout(requireContext())
                        val stars = mutableListOf<ImageView>()

                        for (i in 1..5) {
                            val star = ImageView(requireContext()).apply {
                                setImageResource(R.drawable.ic_star_empty)
                                val size = (32 * resources.displayMetrics.density).toInt()
                                layoutParams = LinearLayout.LayoutParams(size, size)
                                setOnClickListener {
                                    if (alreadyReviewed) return@setOnClickListener
                                    selectedRating = i.toFloat()
                                    stars.forEachIndexed { idx, img ->
                                        img.setImageResource(
                                            if (idx < i) R.drawable.ic_star_full
                                            else R.drawable.ic_star_empty
                                        )
                                    }
                                }
                            }
                            stars.add(star)
                            starLayout.addView(star)
                        }

                        val etComment = EditText(requireContext()).apply {
                            hint = "Tulis ulasan untuk ${item.name}"
                        }

                        val btnSubmit = Button(requireContext()).apply {
                            text = "Kirim Ulasan"
                        }

                        ProductRepository.findProductByName(
                            item.name,
                            onComplete = { product ->

                                val existingReview = product?.reviews?.firstOrNull {
                                    it.transactionId == transactionId &&
                                            it.reviewerName == activeUser
                                }

                                if (existingReview != null) {
                                    alreadyReviewed = true
                                    selectedRating = existingReview.rating

                                    etComment.setText(existingReview.comment)
                                    etComment.isEnabled = false
                                    btnSubmit.isEnabled = false

                                    stars.forEachIndexed { idx, img ->
                                        img.setImageResource(
                                            if (idx < selectedRating) R.drawable.ic_star_full
                                            else R.drawable.ic_star_empty
                                        )
                                        img.isEnabled = false
                                    }
                                }
                            },
                            onError = {}
                        )


                        btnSubmit.setOnClickListener {
                            if (isSubmitting || alreadyReviewed) return@setOnClickListener

                            val comment = etComment.text.toString().trim()
                            if (selectedRating == 0f || comment.isEmpty()) {
                                Toast.makeText(requireContext(), "Lengkapi rating dan ulasan", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            isSubmitting = true
                            btnSubmit.isEnabled = false

                            ProductRepository.addReviewToProduct(
                                productName = item.name,
                                reviewer = activeUser,
                                transactionId = transactionId,
                                comment = comment,
                                rating = selectedRating,
                                onComplete = {
                                    Toast.makeText(requireContext(), "Ulasan ${item.name} dikirim", Toast.LENGTH_SHORT).show()

                                    alreadyReviewed = true
                                    etComment.isEnabled = false
                                    stars.forEach { it.isEnabled = false }
                                },
                                onError = {
                                    isSubmitting = false
                                    btnSubmit.isEnabled = true
                                    Toast.makeText(requireContext(), it.message ?: "Gagal kirim ulasan", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        section.addView(title)
                        section.addView(starLayout)
                        section.addView(etComment)
                        section.addView(btnSubmit)
                        reviewContainer.addView(section)
                    }
                }

                if (mode == "seller") {
                    btnAccept.isEnabled = trx.status == "Pesanan Masuk"
                    btnShip.isEnabled = trx.status == "Pesanan Diproses"
                }
            },
            onError = {
                Toast.makeText(requireContext(), "Gagal memuat detail pesanan", Toast.LENGTH_SHORT).show()
            }
        )

        btnAccept.setOnClickListener {
            if (sellerActionLocked) return@setOnClickListener
            sellerActionLocked = true
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
            if (sellerActionLocked) return@setOnClickListener

            val dialogView = layoutInflater.inflate(R.layout.dialog_input_resi, null)
            val etResi = dialogView.findViewById<EditText>(R.id.et_resi)
            val btnKirim = dialogView.findViewById<Button>(R.id.btn_submit)
            val btnBatal = dialogView.findViewById<Button>(R.id.btn_cancel)

            val dialog = android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            btnKirim.setOnClickListener {
                val resi = etResi.text.toString().trim()
                if (resi.isEmpty()) {
                    Toast.makeText(requireContext(), "Resi tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                sellerActionLocked = true
                btnShip.isEnabled = false
                btnKirim.isEnabled = false

                TransactionManager.updateStatus(
                    transactionId,
                    "Pesanan Dikirim",
                    resi
                ) {
                    Toast.makeText(requireContext(), "Pesanan dikirim", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    findNavController().popBackStack()
                }
            }
            btnBatal.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
        return v
    }
}

private fun formatDate(time: Long): String {
    if (time == 0L) return "-"
    val sdf = java.text.SimpleDateFormat(
        "dd MMM yyyy, HH:mm",
        java.util.Locale("id", "ID")
    )
    return sdf.format(java.util.Date(time))
}


