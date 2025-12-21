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
import com.example.miniprojectv2.formatTimestamp


class BuyerOrdersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_orders, container, false)
        val listLayout: LinearLayout = v.findViewById(R.id.orders_list)

        val prefs =
            requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val buyerUsername =
            prefs.getString("active_username", "Guest") ?: "Guest"

        fun refreshOrders() {

            listLayout.removeAllViews()

            TransactionManager.getTransactionsForBuyer(
                buyer = buyerUsername,
                onSuccess = success@{ transactions ->
                    if (transactions.isEmpty()) {
                        val tv = TextView(requireContext()).apply {
                            text = "Belum ada pesanan."
                            textSize = 16f
                            setPadding(16, 16, 16, 16)
                        }
                        listLayout.addView(tv)
                        return@success
                    }

                    transactions.forEach { trx ->

                        val firstItem = trx.items.firstOrNull()
                        val total = trx.items.sumOf { it.price * it.qty }

                        val tv = TextView(requireContext()).apply {

                            val sb = StringBuilder()

                            if (firstItem != null) {
                                sb.append("${firstItem.name} x${firstItem.qty}")
                            } else {
                                sb.append("(Item kosong)")
                            }

                            sb.append("\nTotal: Rp $total")
                            sb.append("\nEkspedisi: ${trx.expedition}")
                            sb.append("\nStatus: ${trx.status}")

                            if (!trx.trackingNumber.isNullOrEmpty()) {
                                sb.append("\nResi: ${trx.trackingNumber}")
                            }

                            sb.append("\nTanggal: ${formatDate(trx.date)}")

                            text = sb.toString()
                            textSize = 14f
                            setPadding(16, 16, 16, 16)
                        }

                        tv.setOnClickListener {

                            if (trx.status == "Dikirim") {
                                TransactionManager.updateStatus(
                                    transactionId = trx.transactionId,
                                    newStatus = "Selesai",
                                    trackingNumber = trx.trackingNumber,
                                    onSuccess = {
                                        Toast.makeText(
                                            requireContext(),
                                            "Pesanan selesai!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        refreshOrders()
                                    }
                                )
                            }
                        }

                        listLayout.addView(tv)
                    }
                },
                onError = {
                    val tv = TextView(requireContext()).apply {
                        text = "Gagal memuat pesanan."
                        textSize = 16f
                        setPadding(16, 16, 16, 16)
                    }
                    listLayout.addView(tv)
                }
            )
        }

        refreshOrders()
        return v
    }
}
