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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_transactions, container, false)

        val transactionList: LinearLayout = v.findViewById(R.id.transactions_list)
        val btnBack: ImageButton = v.findViewById(R.id.btn_back_transactions)
        val bottomNav =
            (requireActivity() as MainActivity).findViewById<View>(R.id.bottom_nav)

        val fromCheckout = arguments?.getBoolean("from_checkout", false) ?: false

        btnBack.visibility = if (fromCheckout) View.VISIBLE else View.GONE
        bottomNav.visibility = if (fromCheckout) View.GONE else View.VISIBLE

        val prefs = requireContext().getSharedPreferences("UserPrefs", 0)
        val activeUser = prefs.getString("active_username", "") ?: ""

        // LOAD TRANSAKSI (FIRESTORE)
        fun refreshList() {
            transactionList.removeAllViews()

            TransactionManager.getTransactionsForBuyer(
                buyer = activeUser,
                onComplete = { trxList ->

                    if (trxList.isEmpty()) {
                        val tv = TextView(requireContext()).apply {
                            text = "Belum ada transaksi"
                            textSize = 16f
                            setPadding(16, 16, 16, 16)
                        }
                        transactionList.addView(tv)
                        return@getTransactionsForBuyer
                    }

                    trxList.forEach { trx ->

                        val firstItem = trx.items.firstOrNull()

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

                        // TITLE (Nama produk pertama)
                        val tvTitle = TextView(requireContext()).apply {
                            text = if (firstItem != null)
                                "${firstItem.name} x${firstItem.qty}"
                            else
                                "(Item tidak ditemukan)"

                            textSize = 16f
                            setPadding(0, 0, 0, 8)
                        }

                        // TOTAL BELANJA
                        val total = trx.items.sumOf { it.price * it.qty }
                        val tvTotal = TextView(requireContext()).apply {
                            text = "Total: Rp $total"
                            setPadding(0, 0, 0, 8)
                        }

                        val tvExpedition = TextView(requireContext()).apply {
                            text = "Ekspedisi: ${trx.expedition}"
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
                        layout.addView(tvTotal)
                        layout.addView(tvExpedition)
                        layout.addView(tvStatus)
                        if (!trx.trackingNumber.isNullOrEmpty()) layout.addView(tvTracking)
                        layout.addView(tvDate)


                        // BUTTON KONFIRMASI PENERIMAAN BARANG
                        if (trx.status == "Pesanan Dikirim") {
                            val btnConfirm = Button(requireContext()).apply {
                                text = "Konfirmasi Terima"
                                setOnClickListener {
                                    TransactionManager.updateStatus(
                                        transactionId = trx.transactionId,
                                        newStatus = "Pesanan Selesai",
                                        trackingNumber = trx.trackingNumber,
                                        onComplete = {
                                            Toast.makeText(
                                                requireContext(),
                                                "Pesanan diterima!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            refreshList()
                                        },
                                        onError = {
                                            Toast.makeText(
                                                requireContext(),
                                                "Gagal update status",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }
                            layout.addView(btnConfirm)
                        }

                        // Klik card -> buka detail transaksi
                        card.setOnClickListener {
                            val bundle = Bundle().apply {
                                putString("transaction_id", trx.transactionId)
                            }
                            findNavController().navigate(R.id.detailPesananFragment, bundle)
                        }

                        card.addView(layout)
                        transactionList.addView(card)
                    }
                },
                onError = {
                    val tv = TextView(requireContext())
                    tv.text = "Gagal memuat transaksi"
                    transactionList.addView(tv)
                }
            )
        }

        refreshList()

        btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
            bottomNav.visibility = View.VISIBLE
            (requireActivity() as MainActivity).setSelectedTabFromFragment("home")
        }

        return v
    }
}
