package com.example.miniprojectv2

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BeliFragment : Fragment() {

    private lateinit var adapter: ProductAdapter
    private lateinit var productRecycler: RecyclerView
    private var allProducts = ProductRepository.produkUtama.toMutableList()
    private var currentFilter: String = "Semua Harga"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_beli, container, false)

//        val btnLogout: Button = v.findViewById(R.id.btn_logout)
//        btnLogout.setOnClickListener {
//            androidx.appcompat.app.AlertDialog.Builder(requireContext())
//                .setTitle("Konfirmasi Logout")
//                .setMessage("Apakah Anda yakin ingin logout?")
//                .setPositiveButton("Ya") { _, _ ->
//                    Toast.makeText(requireContext(), "Logout berhasil!", Toast.LENGTH_SHORT).show()
//
//                    val intent = Intent(requireContext(), LoginActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    startActivity(intent)
//                }
//                .setNegativeButton("Batal", null)
//                .show()
//        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // cek apakah seller atau bukan
        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isSeller = prefs.getBoolean("isSeller", false)

        // Rekomendasi (horizontal)
        val rekomendasiRecycler = view.findViewById<RecyclerView>(R.id.rekomendasi_recycler)
        rekomendasiRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rekomendasiRecycler.adapter =
            ProductAdapter(ProductRepository.rekomendasiProduk.toMutableList(), isRekomendasi = true)

        // Produk utama (vertical)
        productRecycler = view.findViewById(R.id.product_recycler)
        adapter = ProductAdapter(allProducts.toMutableList(), isSeller = isSeller)
        productRecycler.layoutManager = LinearLayoutManager(requireContext())
        productRecycler.adapter = adapter

        // Search input
        val searchInput = view.findViewById<EditText>(R.id.search_input)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString() ?: ""
                filterProducts(q, currentFilter)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Tombol filter
        val btnFilter = view.findViewById<ImageButton>(R.id.btn_filter)
        btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    // Refresh otomatis setiap fragment aktif lagi
    override fun onResume() {
        super.onResume()

        // Ambil data terbaru dari repository
        allProducts = ProductRepository.produkUtama.toMutableList()

        Log.d("BeliFragment", "onResume dipanggil, produk: ${allProducts.size}")
        adapter.updateData(allProducts)

        // Terapkan kembali filter & search jika sebelumnya ada
        val searchText = view?.findViewById<EditText>(R.id.search_input)?.text?.toString() ?: ""
        filterProducts(searchText, currentFilter)
    }

    // Filter produk berdasarkan nama & harga
    private fun filterProducts(query: String, priceFilter: String) {
        val qTrim = query.trim()
        if (qTrim.isEmpty() && priceFilter == "Semua Harga") {
            adapter.updateData(allProducts)
            return
        }

        val filtered = allProducts.filter { product ->
            val matchQuery =
                if (qTrim.isEmpty()) true else product.name.contains(qTrim, ignoreCase = true)
            val matchPrice = when (priceFilter) {
                "Di bawah 100rb" -> product.price < 100_000
                "100rb - 300rb" -> product.price in 100_000..300_000
                "300rb - 500rb" -> product.price in 300_000..500_000
                "500rb - 1jt" -> product.price in 500_000..1_000_000
                "Di atas 1jt" -> product.price > 1_000_000
                else -> true
            }
            matchQuery && matchPrice
        }

        adapter.updateData(filtered)
    }

    // Dialog Filter Harga
    private fun showFilterDialog() {
        val options = arrayOf(
            "Semua Harga",
            "Di bawah 100rb",
            "100rb - 300rb",
            "300rb - 500rb",
            "500rb - 1jt",
            "Di atas 1jt"
        )
        val selectedIndex = options.indexOf(currentFilter).let { if (it >= 0) it else 0 }

        AlertDialog.Builder(requireContext())
            .setTitle("Filter Harga")
            .setSingleChoiceItems(options, selectedIndex) { _, which ->
                currentFilter = options[which]
            }
            .setPositiveButton("Terapkan") { dialog, _ ->
                val searchText = view?.findViewById<EditText>(R.id.search_input)?.text?.toString() ?: ""
                filterProducts(searchText, currentFilter)
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
