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
            listLayout.removeAllViews() // avoid duplicates

            val transactions = TransactionManager.getTransactionsForSeller()
            if (transactions.isEmpty()) {
                val tvEmpty = TextView(requireContext()).apply {
                    text = "Belum ada pesanan."
                    textSize = 16f
                    setPadding(16, 16, 16, 16)
                }
                listLayout.addView(tvEmpty)
                return
            }

            transactions.forEach { t ->
                val card = CardView(requireContext()).apply {
                    radius = 16f
                    cardElevation = 6f
                    useCompatPadding = true
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 24)
                    layoutParams = params
                    setContentPadding(24, 24, 24, 24)
                }

                val layout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val tvTitle = TextView(requireContext()).apply {
                    text = "${t.itemName} x${t.qty}"
                    textSize = 18f
                    setPadding(0, 0, 0, 8)
                }

                val tvBuyer = TextView(requireContext()).apply {
                    text = "Buyer: ${t.buyer}"
                    setPadding(0, 0, 0, 8)
                }

                val tvPrice = TextView(requireContext()).apply {
                    text = "Total: Rp ${t.totalPrice}"
                    setPadding(0, 0, 0, 8)
                }

                val tvExpedition = TextView(requireContext()).apply {
                    text = "Expedition: ${t.expedition}"  // show buyer's chosen expedition
                    setPadding(0, 0, 0, 8)
                }

                val tvStatus = TextView(requireContext()).apply {
                    text = "Status: ${t.status}${t.trackingNumber?.let { "\nResi: $it" } ?: ""}"
                    setPadding(0, 0, 0, 8)
                }

                val actionButton = Button(requireContext()).apply {
                    when (t.status) {
                        "Pesanan Masuk" -> text = "Accept Order"
                        "Pesanan Diproses" -> text = "Input Tracking Number"
                        "Pesanan Dikirim" -> {
                            text = "Waiting for Buyer Confirmation"
                            isEnabled = false
                        }
                        "Pesanan Selesai" -> {
                            text = "Completed"
                            isEnabled = false
                        }
                    }
                    setOnClickListener {
                        when (t.status) {
                            "Pesanan Masuk" -> {
                                TransactionManager.updateStatus(t, "Pesanan Diproses")
                                Toast.makeText(requireContext(), "Pesanan diproses!", Toast.LENGTH_SHORT).show()
                                refreshOrders()
                            }
                            "Pesanan Diproses" -> {
                                val input = EditText(requireContext())
                                input.hint = "Masukkan resi"
                                val dialog = android.app.AlertDialog.Builder(requireContext())
                                    .setTitle("Masukkan Resi")
                                    .setView(input)
                                    .setPositiveButton("OK") { _, _ ->
                                        val resi = input.text.toString().trim()
                                        if (resi.isNotEmpty()) {
                                            TransactionManager.updateStatus(t, "Pesanan Dikirim", resi)
                                            Toast.makeText(requireContext(), "Pesanan dikirim!", Toast.LENGTH_SHORT).show()
                                            refreshOrders()
                                        } else {
                                            Toast.makeText(requireContext(), "Resi tidak boleh kosong!", Toast.LENGTH_SHORT).show()
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
                layout.addView(actionButton)
                card.addView(layout)
                listLayout.addView(card)
            }
        }
        refreshOrders()
        return v
    }
}
