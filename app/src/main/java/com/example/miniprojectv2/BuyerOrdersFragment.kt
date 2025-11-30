package com.example.miniprojectv2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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

        fun refreshOrders() {

            listLayout.removeAllViews()

            TransactionManager.getTransactionsForBuyer(
                buyer = buyerUsername,
                onComplete = { transactions ->

                    if (transactions.isEmpty()) {
                        val tv = TextView(requireContext())
                        tv.text = "Belum ada pesanan."
                        tv.textSize = 16f
                        tv.setPadding(16, 16, 16, 16)
                        listLayout.addView(tv)
                        return@getTransactionsForBuyer
                    }

                    transactions.forEach { trx ->

                        val firstItem = trx.items.firstOrNull()

                        val tv = TextView(requireContext()).apply {

                            val total = trx.items.sumOf { it.price * it.qty }

                            val sb = StringBuilder()

                            if (firstItem != null) {
                                sb.append("${firstItem.name} x${firstItem.qty}")
                            } else {
                                sb.append("(Item kosong)")
                            }

                            sb.append("\nTotal: Rp $total")
                            sb.append("\nExpedisi: ${trx.expedition}")
                            sb.append("\nStatus: ${trx.status}")

                            if (!trx.trackingNumber.isNullOrEmpty()) {
                                sb.append("\nResi: ${trx.trackingNumber}")
                            }

                            sb.append("\nTanggal: ${trx.date}")

                            text = sb.toString()
                            textSize = 14f
                            setPadding(16, 16, 16, 16)
                        }

                        tv.setOnClickListener {
                            if (trx.status == "Pesanan Dikirim") {
                                TransactionManager.updateStatus(
                                    transactionId = trx.transactionId,
                                    newStatus = "Pesanan Selesai",
                                    trackingNumber = trx.trackingNumber,
                                    onComplete = {
                                        Toast.makeText(requireContext(), "Pesanan selesai!", Toast.LENGTH_SHORT).show()
                                        refreshOrders()
                                    }
                                )
                            }
                        }

                        listLayout.addView(tv)
                    }
                },
                onError = {
                    val tv = TextView(requireContext())
                    tv.text = "Gagal memuat pesanan."
                    tv.textSize = 16f
                    tv.setPadding(16, 16, 16, 16)
                    listLayout.addView(tv)
                }
            )
        }

        refreshOrders()
        return v
    }
}
