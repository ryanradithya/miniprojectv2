package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class OrdersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_orders, container, false)
        val listLayout: LinearLayout = v.findViewById(R.id.orders_list)

        fun refreshOrders() {

            listLayout.removeAllViews()

            TransactionManager.getTransactionsForSeller(
                onComplete = { transactions ->

                    if (transactions.isEmpty()) {
                        val tv = TextView(requireContext())
                        tv.text = "Belum ada pesanan."
                        tv.textSize = 16f
                        tv.setPadding(16, 16, 16, 16)
                        listLayout.addView(tv)
                        return@getTransactionsForSeller
                    }

                    transactions.forEach { trx ->

                        val firstItem = trx.items.firstOrNull()
                        val total = trx.items.sumOf { it.price * it.qty }

                        val card = CardView(requireContext()).apply {
                            radius = 16f
                            cardElevation = 8f
                            setContentPadding(24, 24, 24, 24)
                        }

                        val layout = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.VERTICAL
                        }

                        val tvTitle = TextView(requireContext()).apply {
                            text = if (firstItem != null)
                                "${firstItem.name} x${firstItem.qty}"
                            else
                                "(Item kosong)"
                            textSize = 18f
                        }

                        val tvBuyer = TextView(requireContext()).apply {
                            text = "Buyer: ${trx.buyer}"
                        }

                        val tvPrice = TextView(requireContext()).apply {
                            text = "Total: Rp $total"
                        }

                        val tvExpedition = TextView(requireContext()).apply {
                            text = "Expedition: ${trx.expedition}"
                        }

                        val tvStatus = TextView(requireContext()).apply {
                            text =
                                "Status: ${trx.status}" +
                                        (trx.trackingNumber?.let { "\nResi: $it" } ?: "")
                        }

                        val btnAction = Button(requireContext()).apply {

                            when (trx.status) {
                                "Pesanan Masuk" -> text = "Accept Order"
                                "Pesanan Diproses" -> text = "Input Tracking Number"
                                "Pesanan Dikirim" -> {
                                    text = "Waiting Buyer"
                                    isEnabled = false
                                }
                                "Pesanan Selesai" -> {
                                    text = "Completed"
                                    isEnabled = false
                                }
                            }

                            setOnClickListener {
                                when (trx.status) {
                                    "Pesanan Masuk" -> {
                                        TransactionManager.updateStatus(
                                            trx.transactionId,
                                            "Pesanan Diproses",
                                            onComplete = { refreshOrders() }
                                        )
                                    }

                                    "Pesanan Diproses" -> {
                                        val input = EditText(requireContext())
                                        input.hint = "Masukkan nomor resi"

                                        val dialog = android.app.AlertDialog.Builder(requireContext())
                                            .setTitle("Input Resi")
                                            .setView(input)
                                            .setPositiveButton("OK") { _, _ ->
                                                val resi = input.text.toString().trim()
                                                if (resi.isNotEmpty()) {
                                                    TransactionManager.updateStatus(
                                                        trx.transactionId,
                                                        "Pesanan Dikirim",
                                                        trackingNumber = resi,
                                                        onComplete = { refreshOrders() }
                                                    )
                                                }
                                            }
                                            .setNegativeButton("Cancel", null)
                                            .create()
                                        dialog.show()
                                    }
                                }
                            }
                        }

                        layout.addView(tvTitle)
                        layout.addView(tvBuyer)
                        layout.addView(tvPrice)
                        layout.addView(tvExpedition)
                        layout.addView(tvStatus)
                        layout.addView(btnAction)

                        card.addView(layout)
                        listLayout.addView(card)
                    }
                },
                onError = {
                    Toast.makeText(requireContext(), "Gagal memuat pesanan", Toast.LENGTH_SHORT).show()
                }
            )
        }

        refreshOrders()
        return v
    }
}
