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

        // ganti id ke transactions_container (LinearLayout di dalam ScrollView)
        val transactionContainer: LinearLayout = v.findViewById(R.id.transactions_container)
        val btnBack: ImageButton = v.findViewById(R.id.btn_back_transactions)
        val bottomNav = (requireActivity() as MainActivity)
            .findViewById<BottomNavigationView>(R.id.bottom_nav)

        val fromCheckout = arguments?.getBoolean("from_checkout", false) ?: false

        if (fromCheckout) {
            btnBack.visibility = View.VISIBLE
            bottomNav.visibility = View.GONE
        } else {
            btnBack.visibility = View.GONE
            bottomNav.visibility = View.VISIBLE
        }

        // kosongkan container dulu
        transactionContainer.removeAllViews()

        if (TransactionManager.transactions.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "Belum ada transaksi"
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            transactionContainer.addView(tv)
        } else {
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
                transactionContainer.addView(card)
            }
        }

        btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
            bottomNav.visibility = View.VISIBLE
            bottomNav.selectedItemId = R.id.homeFragment
        }

        return v
    }
}

