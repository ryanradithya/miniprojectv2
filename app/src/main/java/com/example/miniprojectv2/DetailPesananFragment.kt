package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class DetailPesananFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_detail_pesanan, container, false)

        val name = arguments?.getString("product_name") ?: ""
        val price = arguments?.getInt("product_price") ?: 0
        val qty = arguments?.getInt("product_qty") ?: 1
        val status = arguments?.getString("product_status") ?: "Pesanan Masuk"
        val expedition = arguments?.getString("product_expedition") ?: "-"
        val tracking = arguments?.getString("product_tracking")
        val date = arguments?.getString("product_date") ?: "-"
        val total = price * qty

        // Ambil nama user aktif
        val prefs = requireContext().getSharedPreferences("UserPrefs", 0)
        val activeUser = prefs.getString("active_username", "User") ?: "User"

        // View
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

        // 🔹 Setup rating
        var selectedRating = 0f
        val stars = mutableListOf<ImageView>()

        for (i in 1..5) {
            val star = ImageView(requireContext())
            val size = (32 * resources.displayMetrics.density).toInt()
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(6, 0, 6, 0)
            star.layoutParams = params
            star.setImageResource(R.drawable.ic_star_empty)
            star.setOnClickListener {
                selectedRating = i.toFloat()
                stars.forEachIndexed { index, s ->
                    s.setImageResource(if (index < i) R.drawable.ic_star_full else R.drawable.ic_star_empty)
                }
            }
            stars.add(star)
            ratingStars.addView(star)
        }

        // 🔹 Jika user sudah pernah review produk ini, nonaktifkan input
        val product = ProductRepository.findProductByName(name)
        val hasReviewed = product?.reviews?.any { it.reviewerName == activeUser } == true
        if (hasReviewed) {
            etReview.isEnabled = false
            btnSubmit.isEnabled = false
            stars.forEach { it.isEnabled = false }
            Toast.makeText(requireContext(), "Kamu sudah memberi ulasan untuk produk ini.", Toast.LENGTH_SHORT).show()
        }

        // 🔹 Submit review
        btnSubmit.setOnClickListener {
            val comment = etReview.text.toString().trim()
            if (selectedRating == 0f || comment.isEmpty()) {
                Toast.makeText(requireContext(), "Isi rating dan komentar dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val success = ProductRepository.addReviewToProduct(name, activeUser, comment, selectedRating)
            if (success) {
                Toast.makeText(requireContext(), "Ulasan terkirim! ⭐", Toast.LENGTH_LONG).show()

                // Kunci input agar tidak bisa edit
                etReview.isEnabled = false
                btnSubmit.isEnabled = false
                stars.forEach { it.isEnabled = false }
            } else {
                Toast.makeText(requireContext(), "Kamu sudah mengulas produk ini.", Toast.LENGTH_SHORT).show()
            }
        }

        return v
    }
}
