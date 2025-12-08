package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class DetailPesananFragment : Fragment() {

    private var selectedRating = 0f
    private val starViews = mutableListOf<ImageView>()
    private var productAlreadyReviewed = false
    private var allowReview = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_detail_pesanan, container, false)

        // Ambil data bundle
        val name = arguments?.getString("product_name") ?: ""
        val price = arguments?.getInt("product_price") ?: 0
        val qty = arguments?.getInt("product_qty") ?: 1
        val status = arguments?.getString("product_status") ?: "Pesanan Masuk"
        val expedition = arguments?.getString("product_expedition") ?: "-"
        val tracking = arguments?.getString("product_tracking")
        val date = arguments?.getString("product_date") ?: "-"
        val total = price * qty

        // Ambil user aktif
        val prefs = requireContext().getSharedPreferences("UserPrefs", 0)
        val activeUser = prefs.getString("active_username", "User") ?: "User"

        // Set UI awal
        v.findViewById<TextView>(R.id.tv_product_name).text = name
        v.findViewById<TextView>(R.id.tv_product_price).text = "Rp $price"
        v.findViewById<TextView>(R.id.tv_product_qty).text = "x$qty"
        v.findViewById<TextView>(R.id.tv_product_total).text = "Total: Rp $total"
        v.findViewById<TextView>(R.id.tv_product_status).text = "Status: $status"
        v.findViewById<TextView>(R.id.tv_product_expedition).text = "Expedisi: $expedition"
        v.findViewById<TextView>(R.id.tv_product_tracking).text = tracking?.let { "Resi: $it" } ?: ""
        v.findViewById<TextView>(R.id.tv_product_date).text = "Tanggal: $date"

        val etReview: EditText = v.findViewById(R.id.et_review)
        val ratingStars: LinearLayout = v.findViewById(R.id.rating_stars)
        val btnSubmit: Button = v.findViewById(R.id.btn_submit_review)

        // review logic
        allowReview = status == "Pesanan Selesai"

        if (!allowReview) {
            disableReview(etReview, btnSubmit)
        }

        //previous review
        ProductRepository.findProductByName(
            name,
            onComplete = { product ->
                if (product != null) {

                    productAlreadyReviewed =
                        product.reviews.any { it.reviewerName == activeUser }

                    if (productAlreadyReviewed) {
                        disableReview(etReview, btnSubmit)
                        Toast.makeText(requireContext(),
                            "Kamu sudah mengulas produk ini.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onError = {
                Toast.makeText(requireContext(),
                    "Gagal memuat review.", Toast.LENGTH_SHORT).show()
            }
        )


        for (i in 1..5) {
            val star = ImageView(requireContext())
            val size = (32 * resources.displayMetrics.density).toInt()
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(6, 0, 6, 0)
            star.layoutParams = params
            star.setImageResource(R.drawable.ic_star_empty)

            star.setOnClickListener {
                if (!allowReview || productAlreadyReviewed) {
                    Toast.makeText(requireContext(),
                        "Hanya bisa memberi rating setelah pesanan selesai.",
                        Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                selectedRating = i.toFloat()
                starViews.forEachIndexed { index, img ->
                    img.setImageResource(
                        if (index < i) R.drawable.ic_star_full else R.drawable.ic_star_empty
                    )
                }
            }

            starViews.add(star)
            ratingStars.addView(star)
        }

        //review fs
        btnSubmit.setOnClickListener {
            val comment = etReview.text.toString().trim()

            if (selectedRating == 0f || comment.isEmpty()) {
                Toast.makeText(requireContext(),
                    "Isi rating dan komentar dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ProductRepository.addReviewToProduct(
                productName = name,
                reviewer = activeUser,
                comment = comment,
                rating = selectedRating,
                onComplete = {
                    Toast.makeText(requireContext(),
                        "Ulasan berhasil dikirim!", Toast.LENGTH_LONG).show()
                    disableReview(etReview, btnSubmit)
                },
                onError = {
                    Toast.makeText(requireContext(),
                        "Gagal mengirim ulasan.", Toast.LENGTH_SHORT).show()
                }
            )
        }

        return v
    }

    private fun disableReview(et: EditText, btn: Button) {
        et.isEnabled = false
        btn.isEnabled = false
        starViews.forEach { it.isEnabled = false }
    }
}
