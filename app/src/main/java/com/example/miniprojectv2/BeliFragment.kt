package com.example.miniprojectv2

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BeliFragment : Fragment() {

    private lateinit var adapter: ProductAdapter
    private lateinit var productRecycler: RecyclerView
    private var allProducts = ProductRepository.produkUtama.toMutableList()
    private var currentFilter: String = "Semua Harga"
    private var currentCategory: String = "Semua Produk"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // restore kategori jika ada
        savedInstanceState?.getString("currentCategory")?.let {
            currentCategory = it
        }
        return inflater.inflate(R.layout.fragment_beli, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentCategory", currentCategory)
        outState.putString("currentFilter", currentFilter) // opsional kalau mau restore filter harga juga
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isSeller = prefs.getBoolean("isSeller", false)

        //Rekomendasi Produk (Animasi scroll)
        val rekomendasiRecycler = view.findViewById<RecyclerView>(R.id.rekomendasi_recycler)
        val originalList = ProductRepository.rekomendasiProduk.toMutableList()
        val loopList = (originalList + originalList + originalList).toMutableList()
        val rekomendasiAdapter = ProductAdapter(loopList, isRekomendasi = true)

        //recyclerview untuk object pooling
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rekomendasiRecycler.layoutManager = layoutManager
        rekomendasiRecycler.adapter = rekomendasiAdapter
        val handler = android.os.Handler()
        var isUserTouching = false
        val scrollStep = 1
        val scrollInterval: Long = 10
        val resumeDelay: Long = 2000
        val continuousScrollRunnable = object : Runnable {
            override fun run() {
                if (!isUserTouching) {
                    rekomendasiRecycler.scrollBy(scrollStep, 0)
                    val totalItem = rekomendasiAdapter.itemCount
                    val firstVisible = layoutManager.findFirstVisibleItemPosition()
                    if (firstVisible > totalItem / 3 * 2) {
                        rekomendasiRecycler.scrollToPosition(totalItem / 3)
                    }
                }
                handler.postDelayed(this, scrollInterval)
            }
        }
        rekomendasiRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        isUserTouching = true
                        handler.removeCallbacks(continuousScrollRunnable)
                    }
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        isUserTouching = false
                        handler.removeCallbacks(continuousScrollRunnable)
                        handler.postDelayed(continuousScrollRunnable, resumeDelay)
                    }
                }
            }
        })
        handler.postDelayed(continuousScrollRunnable, 1000)
        viewLifecycleOwner.lifecycle.addObserver(
            object : androidx.lifecycle.DefaultLifecycleObserver {
                override fun onPause(owner: androidx.lifecycle.LifecycleOwner) {
                    handler.removeCallbacks(continuousScrollRunnable)
                }

                override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                    handler.postDelayed(continuousScrollRunnable, 1000)
                }

                override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) {
                    handler.removeCallbacks(continuousScrollRunnable)
                }
            }
        )



        //Setup Produk Utama (vertical)
        productRecycler = view.findViewById(R.id.product_recycler)
        adapter = ProductAdapter(allProducts.toMutableList(), isSeller = isSeller)
        productRecycler.layoutManager = LinearLayoutManager(requireContext())
        productRecycler.adapter = adapter

        // Search Bar
        val searchInput = view.findViewById<EditText>(R.id.search_input)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString() ?: ""
                filterProducts(q, currentFilter, currentCategory)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //  Tombol Filter Harga
        val btnFilter = view.findViewById<ImageButton>(R.id.btn_filter)
        btnFilter.setOnClickListener { showFilterDialog() }

        // Tombol Cart (hanya untuk pembeli)
        val btnCart = view.findViewById<ImageButton>(R.id.btn_cart)
        if (isSeller) {
            btnCart.visibility = View.GONE
        } else {
            btnCart.visibility = View.VISIBLE
            btnCart.setOnClickListener {
                try {
                    findNavController().navigate(R.id.cartFragment)
                } catch (e: Exception) {
                    Log.e("BeliFragment", "Navigation to cart failed: ${e.message}")
                    Toast.makeText(requireContext(), "Gagal membuka keranjang", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        // Kategori Navbar
        setupCategoryButtons(view)
    }

    override fun onResume() {
        super.onResume()
        allProducts = ProductRepository.produkUtama.toMutableList()
        adapter.updateData(allProducts)

        val searchText = view?.findViewById<EditText>(R.id.search_input)?.text?.toString() ?: ""
        filterProducts(searchText, currentFilter, currentCategory)
    }

    // Filter Produk
    private fun filterProducts(query: String, priceFilter: String, category: String) {
        val qTrim = query.trim()

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
            val matchCategory = when (category) {
                "Semua Produk" -> true
                else -> product.category.equals(category, ignoreCase = true)
            }
            matchQuery && matchPrice && matchCategory
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
                filterProducts(searchText, currentFilter, currentCategory)
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // Navbar Kategori
    private fun setupCategoryButtons(view: View) {
        val layout = view.findViewById<LinearLayout>(R.id.category_navbar)
        val categories = listOf("Semua Produk", "Kamera Analog", "Roll Film", "Lensa Analog", "Tas Kamera")
        layout.removeAllViews()
        var activeButton: Button? = null

        categories.forEach { cat ->
            val btn = Button(requireContext()).apply {
                text = cat
                textSize = 14f
                isAllCaps = false
                setBackgroundResource(R.drawable.bg_category_normal)
                setTextColor(resources.getColor(android.R.color.black))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(8, 0, 8, 0)
                layoutParams = params
            }

            // Jika ini tombol yang sesuai currentCategory, jadikan aktif
            if (cat.equals(currentCategory, ignoreCase = true)) {
                btn.setBackgroundResource(R.drawable.bg_category_selected)
                btn.setTextColor(resources.getColor(android.R.color.white))
                activeButton = btn
            }

            btn.setOnClickListener {
                // update state
                currentCategory = cat
                activeButton?.setBackgroundResource(R.drawable.bg_category_normal)
                activeButton?.setTextColor(resources.getColor(android.R.color.black))

                btn.setBackgroundResource(R.drawable.bg_category_selected)
                btn.setTextColor(resources.getColor(android.R.color.white))
                activeButton = btn

                val searchText = view.findViewById<EditText>(R.id.search_input).text.toString()
                filterProducts(searchText, currentFilter, currentCategory)
            }

            layout.addView(btn)
        }

        if (activeButton == null) {
            val firstChild = layout.getChildAt(0)
            if (firstChild is Button) {
                firstChild.setBackgroundResource(R.drawable.bg_category_selected)
                firstChild.setTextColor(resources.getColor(android.R.color.white))
                activeButton = firstChild
                currentCategory = "Semua Produk"
            }
        }
    }
}
