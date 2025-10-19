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
        val btnBack: ImageButton = v.findViewById(R.id.btn_back_transactions)
        val bottomNav = (requireActivity() as MainActivity)
            .findViewById<BottomNavigationView>(R.id.bottom_nav)

        val fromCheckout = arguments?.getBoolean("from_checkout", false) ?: false

        // Atur visibilitas tombol & navbar
        btnBack.visibility = if (fromCheckout) View.VISIBLE else View.GONE
        bottomNav.visibility = if (fromCheckout) View.GONE else View.VISIBLE

        val prefs = requireContext().getSharedPreferences("UserPrefs", 0)
        val activeUser = prefs.getString("active_username", "")

        fun refreshList() {
            transactionList.removeAllViews()
            val buyerTransactions = TransactionManager.getTransactionsForBuyer(activeUser ?: "")

            if (buyerTransactions.isEmpty()) {
                val tv = TextView(requireContext()).apply {
                    text = "Belum ada transaksi"
                    textSize = 16f
                    setPadding(16, 16, 16, 16)
                }
                transactionList.addView(tv)
            } else {
                buyerTransactions.forEach { trx ->
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
                        text = "${trx.itemName} x${trx.qty}"
                        textSize = 16f
                        setPadding(0, 0, 0, 8)
                    }

                    val tvPrice = TextView(requireContext()).apply {
                        text = "Total: Rp ${trx.totalPrice}"
                        setPadding(0, 0, 0, 8)
                    }

                    val tvExpedition = TextView(requireContext()).apply {
                        text = "Expedition: ${trx.expedition}"
                        setPadding(0, 0, 0, 8)
                    }

                    val tvStatus = TextView(requireContext()).apply {
                        text = "Status: ${trx.status}"
                        setPadding(0, 0, 0, 8)
                    }

                    val tvTracking = TextView(requireContext()).apply {
                        text = trx.trackingNumber?.let { "Resi: $it" } ?: ""
                        setPadding(0, 0, 0, 8)
                    }

                    val tvDate = TextView(requireContext()).apply {
                        text = "Tanggal: ${trx.date}"
                        setPadding(0, 0, 0, 8)
                    }

                    layout.addView(tvTitle)
                    layout.addView(tvPrice)
                    layout.addView(tvExpedition)
                    layout.addView(tvStatus)
                    if (trx.trackingNumber != null) layout.addView(tvTracking)
                    layout.addView(tvDate)

                    // Tombol konfirmasi penerimaan barang
                    if (trx.status == "Pesanan Dikirim") {
                        val btnConfirm = Button(requireContext()).apply {
                            text = "Confirm Delivery"
                            setOnClickListener {
                                TransactionManager.updateStatus(trx, "Pesanan Selesai")
                                Toast.makeText(requireContext(), "Pesanan diterima!", Toast.LENGTH_SHORT).show()
                                refreshList()
                            }
                        }
                        layout.addView(btnConfirm)
                    }

                    card.addView(layout)

                    // card detail pesanan
                    card.setOnClickListener {
                        val bundle = Bundle().apply {
                            putString("product_name", trx.itemName)
                            putInt("product_price", trx.totalPrice / trx.qty)
                            putInt("product_qty", trx.qty)
                            putInt("product_total", trx.totalPrice)
                            putString("product_status", trx.status)
                            putString("product_expedition", trx.expedition)
                            putString("product_tracking", trx.trackingNumber)
                            putString("product_date", trx.date)
                        }
                        findNavController().navigate(R.id.detailPesananFragment, bundle)
                    }

                    transactionList.addView(card)
                }
            }
        }
        refreshList()

        btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
            bottomNav.visibility = View.VISIBLE
            bottomNav.selectedItemId = R.id.homeFragment
        }
        return v
    }
}
