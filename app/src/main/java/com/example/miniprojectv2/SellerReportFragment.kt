package com.example.miniprojectv2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class SellerReportFragment : Fragment() {

    private lateinit var tvRevenue: TextView
    private lateinit var tvItems: TextView
    private lateinit var tvTransactions: TextView

    private lateinit var btnDaily: MaterialButton
    private lateinit var btnWeekly: MaterialButton
    private lateinit var btnMonthly: MaterialButton

    private val db = FirebaseFirestore.getInstance()
    private lateinit var sellerEmail: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_seller_report, container, false)

        // UI
        tvRevenue = v.findViewById(R.id.tv_total_revenue)
        tvItems = v.findViewById(R.id.tv_total_items)
        tvTransactions = v.findViewById(R.id.tv_total_transactions)

        btnDaily = v.findViewById(R.id.btn_daily)
        btnWeekly = v.findViewById(R.id.btn_weekly)
        btnMonthly = v.findViewById(R.id.btn_monthly)

        // Ambil email seller
        val prefs = requireContext()
            .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        sellerEmail = prefs.getString("active_email", "") ?: ""

        if (sellerEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Email seller tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        // Default → laporan harian
        loadDailyReport()
        setActiveButton(btnDaily)

        // Listener
        btnDaily.setOnClickListener {
            setActiveButton(btnDaily)
            loadDailyReport()
        }

        btnWeekly.setOnClickListener {
            setActiveButton(btnWeekly)
            loadWeeklyReport()
        }

        btnMonthly.setOnClickListener {
            setActiveButton(btnMonthly)
            loadMonthlyReport()
        }

        return v
    }

    /* =========================
       LOAD REPORT
       ========================= */

    private fun loadDailyReport() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        loadReport(cal.timeInMillis, System.currentTimeMillis())
    }

    private fun loadWeeklyReport() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        loadReport(cal.timeInMillis, System.currentTimeMillis())
    }

    private fun loadMonthlyReport() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        loadReport(cal.timeInMillis, System.currentTimeMillis())
    }

    /* =========================
       CORE QUERY
       ========================= */

    private fun loadReport(startTime: Long, endTime: Long) {

        db.collection("transactions")
            .whereGreaterThanOrEqualTo("date", startTime)
            .whereLessThanOrEqualTo("date", endTime)
            .get()
            .addOnSuccessListener { snapshot ->

                var totalRevenue = 0L
                var totalItems = 0
                var totalTransactions = 0

                snapshot.documents.forEach { doc ->

                    val items =
                        doc.get("items") as? List<Map<String, Any>> ?: return@forEach

                    var hasSellerItem = false

                    items.forEach { item ->
                        val seller = item["sellerEmail"] as? String ?: return@forEach
                        if (seller == sellerEmail) {

                            val price = (item["price"] as Long)
                            val qty = (item["qty"] as Long).toInt()

                            totalRevenue += price * qty
                            totalItems += qty
                            hasSellerItem = true
                        }
                    }

                    if (hasSellerItem) totalTransactions++
                }

                updateUI(totalRevenue, totalItems, totalTransactions)
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Gagal memuat laporan",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /* =========================
       UI HELPER
       ========================= */

    private fun updateUI(revenue: Long, items: Int, transactions: Int) {

        val rupiah = NumberFormat
            .getCurrencyInstance(Locale("id", "ID"))
            .format(revenue)

        tvRevenue.text = rupiah
        tvItems.text = "$items item"
        tvTransactions.text = "$transactions transaksi"
    }

    private fun setActiveButton(active: MaterialButton) {
        listOf(btnDaily, btnWeekly, btnMonthly).forEach {
            it.isChecked = false
        }
        active.isChecked = true
    }
}
