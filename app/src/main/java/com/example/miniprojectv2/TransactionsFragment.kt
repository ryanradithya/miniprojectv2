package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_transactions, container, false)

        val transactionList: LinearLayout = v.findViewById(R.id.transactions_list)
        transactionList.removeAllViews()

        if (TransactionManager.transactions.isEmpty()) {
            val tv = TextView(requireContext())
            tv.text = "Belum ada transaksi"
            transactionList.addView(tv)
        } else {
            TransactionManager.transactions.forEach { trx ->
                // Buat CardView
                val card = CardView(requireContext()).apply {
                    radius = 16f
                    cardElevation = 6f
                    useCompatPadding = true
                    setContentPadding(24, 24, 24, 24)
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
        return v
    }
}
