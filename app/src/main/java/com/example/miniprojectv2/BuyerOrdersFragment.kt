package com.example.miniprojectv2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class BuyerOrdersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_orders, container, false)
        val listLayout: LinearLayout = v.findViewById(R.id.orders_list)

        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val buyerUsername = prefs.getString("active_username", "Guest") ?: "Guest"
        val transactions = TransactionManager.getTransactionsForBuyer(buyerUsername)

        listLayout.removeAllViews()

        if (transactions.isEmpty()) {
            val tvEmpty = TextView(requireContext())
            tvEmpty.text = "Belum ada pesanan."
            listLayout.addView(tvEmpty)
        } else {
            transactions.forEach { t ->
                val tv = TextView(requireContext())
                tv.text = "${t.itemName} x${t.qty} — Rp ${t.totalPrice}\nStatus: ${t.status}" +
                        (t.trackingNumber?.let { "\nResi: $it" } ?: "")
                tv.setPadding(8, 8, 8, 8)

                // Allow buyer to mark as "Selesai"
                tv.setOnClickListener {
                    if (t.status == "Pesanan Dikirim") {
                        TransactionManager.updateStatus(t, "Pesanan Selesai")
                        Toast.makeText(requireContext(), "Pesanan selesai!", Toast.LENGTH_SHORT).show()
                        fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
                    }
                }

                listLayout.addView(tv)
            }
        }

        return v
    }
}
