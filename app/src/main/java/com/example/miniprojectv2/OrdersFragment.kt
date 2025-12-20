package com.example.miniprojectv2

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class OrdersFragment : Fragment() {

    private lateinit var listLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_orders, container, false)
        listLayout = v.findViewById(R.id.orders_list)
        return v
    }

    override fun onResume() {
        super.onResume()
        refreshOrders()
    }

    private fun refreshOrders() {
        listLayout.removeAllViews()

        val prefs = requireContext()
            .getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)

        val sellerEmail = prefs.getString("active_email", "") ?: ""

        TransactionManager.getTransactionsForSeller(
            sellerEmail = sellerEmail,
            onSuccess = { transactions ->

                if (transactions.isEmpty()) {
                    val tv = TextView(requireContext()).apply {
                        text = "Belum ada pesanan."
                        textSize = 16f
                        setPadding(16, 16, 16, 16)
                    }
                    listLayout.addView(tv)
                    return@getTransactionsForSeller
                }

                transactions.forEach { trx ->
                    val itemView = layoutInflater.inflate(
                        R.layout.item_seller_transaction,
                        listLayout,
                        false
                    )

                    val firstItem = trx.items.firstOrNull()
                    val total = trx.items.sumOf { it.price * it.qty }

                    itemView.findViewById<TextView>(R.id.tvProduct).text =
                        firstItem?.let { "${it.name} x${it.qty}" } ?: "(Item kosong)"

                    itemView.findViewById<TextView>(R.id.tvBuyer).text =
                        "Buyer: ${trx.buyer}"

                    itemView.findViewById<TextView>(R.id.tvTotal).text =
                        "Total: Rp $total"

                    itemView.findViewById<TextView>(R.id.tvExpedition).text =
                        "Expedition: ${trx.expedition}"

                    itemView.findViewById<TextView>(R.id.tvStatus).text =
                        trx.status + (trx.trackingNumber?.let { "\nResi: $it" } ?: "")

                    itemView.setOnClickListener {
                        findNavController().navigate(
                            R.id.action_ordersFragment_to_detailPesananSellerFragment,
                            Bundle().apply {
                                putString("transaction_id", trx.transactionId)
                            }
                        )
                    }

                    listLayout.addView(itemView)
                }
            },
            onError = {
                Toast.makeText(requireContext(), "Gagal memuat pesanan", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
