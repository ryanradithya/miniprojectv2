package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class TransactionsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_transactions, container, false)

        val transactionList: LinearLayout = v.findViewById(R.id.transactions_list)
        val btnGoToBeli: Button = v.findViewById(R.id.btn_go_to_beli)
        transactionList.removeAllViews()

        // 🧾 Jika tidak ada transaksi, tampilkan teks
        if (TransactionManager.transactions.isEmpty()) {
            val tv = TextView(requireContext())
            tv.text = "Belum ada transaksi"
            tv.textSize = 16f
            tv.setPadding(16, 16, 16, 16)
            transactionList.addView(tv)
        } else {
            // 🧾 Tampilkan setiap transaksi sebagai CardView
            TransactionManager.transactions.forEach { trx ->
                val card = CardView(requireContext()).apply {
                    radius = 16f
                    cardElevation = 6f
                    useCompatPadding = true
                    setContentPadding(24, 24, 24, 24)
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 24)
                    layoutParams = params
                }

                val layout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val tvTitle = TextView(requireContext()).apply {
                    text = "Checkout: ${trx.itemName} x${trx.qty}"
                    textSize = 16f
                    setPadding(0, 0, 0, 8)
                }

                val tvPrice = TextView(requireContext()).apply {
                    text = "Total: Rp ${trx.totalPrice}"
                    setPadding(0, 0, 0, 8)
                }

                val tvDate = TextView(requireContext()).apply {
                    text = "Tanggal: ${trx.date}"
                }

                layout.addView(tvTitle)
                layout.addView(tvPrice)
                layout.addView(tvDate)

                card.addView(layout)
                transactionList.addView(card)
            }
        }

        // 🛒 Tombol "Beli Lagi"
        btnGoToBeli.setOnClickListener {
            // Navigasi ke HomeFragment (bukan Checkout lagi)
            val navController = findNavController()
            navController.navigate(R.id.homeFragment)

            // 🔥 Update state bottom navigation agar ikon Home aktif
            (requireActivity() as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_nav)
                .selectedItemId = R.id.homeFragment
        }

        return v
    }
}
