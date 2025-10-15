package com.example.miniprojectv2

import android.app.AlertDialog
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
    // gunakan MutableList supaya update mudah
    private var allProducts = ProductRepository.produkUtama.toMutableList()

    private var currentFilter: String = "Semua Harga"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_beli, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Rekomendasi (horizontal)
        val rekomendasiRecycler = view.findViewById<RecyclerView>(R.id.rekomendasi_recycler)
        rekomendasiRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rekomendasiRecycler.adapter =
            ProductAdapter(ProductRepository.rekomendasiProduk.toMutableList(), isRekomendasi = true)

        // Produk utama (vertical)
        productRecycler = view.findViewById(R.id.product_recycler)
        adapter = ProductAdapter(allProducts.toMutableList()) // gunakan mutable copy
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

    // Filter produk berdasarkan nama & harga
    private fun filterProducts(query: String, priceFilter: String) {
        val qTrim = query.trim()
        // jika tidak ada query dan filter = Semua Harga -> kembalikan semua produk
        if (qTrim.isEmpty() && priceFilter == "Semua Harga") {
            Log.d("BeliFragment", "Restore semua produk (no query, no filter)")
            adapter.updateData(allProducts)
            return
        }

        // jika ada query kosong tapi filter aktif -> tetap terapkan filter harga
        val filtered = allProducts.filter { product ->
            val matchQuery = if (qTrim.isEmpty()) true else product.name.contains(qTrim, ignoreCase = true)
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

        Log.d("BeliFragment", "Filter result: ${filtered.size} produk ditemukan (query='$qTrim', filter='$priceFilter')")
        adapter.updateData(filtered)
    }

    // Dialog Filter Harga
    private fun showFilterDialog() {
        val options = arrayOf("Semua Harga", "Di bawah 100rb", "100rb - 300rb","300rb - 500rb", "500rb - 1jt", "Di atas 1jt")
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
