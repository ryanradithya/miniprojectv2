package com.example.miniprojectv2

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query

class BeliFragment : Fragment() {

    private lateinit var adapter: ProductAdapter
    private lateinit var productRecycler: RecyclerView

    private var allProducts = mutableListOf<Product>()   // FIRESTORE DATA
    private var currentFilter: String = "Semua Harga"
    private var currentCategory: String = "Semua Produk"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_beli, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isSeller = prefs.getBoolean("isSeller", false)


        val rekomendasiRecycler = view.findViewById<RecyclerView>(R.id.rekomendasi_recycler)

        ProductRepository.getTopRatedProducts(
            onComplete = { recommendationList ->

                if (recommendationList.isEmpty()) return@getTopRatedProducts

                val loopList = (recommendationList + recommendationList + recommendationList)

                val rekomendasiAdapter = ProductAdapter(loopList.toMutableList(), isRekomendasi = true)

                val layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                rekomendasiRecycler.layoutManager = layoutManager
                rekomendasiRecycler.adapter = rekomendasiAdapter

                // Auto-scroll animation
                val handler = Handler()
                var isUserTouching = false
                val scrollStep = 1
                val scrollInterval: Long = 10
                val resumeDelay: Long = 2000

                val continuousScrollRunnable = object : Runnable {
                    override fun run() {
                        if (!isUserTouching) {
                            rekomendasiRecycler.scrollBy(scrollStep, 0)
                            val total = rekomendasiAdapter.itemCount
                            val first = layoutManager.findFirstVisibleItemPosition()
                            if (first > total / 3 * 2) {
                                rekomendasiRecycler.scrollToPosition(total / 3)
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
            },
            onError = { Log.e("BeliFragment", "Gagal ambil rekomendasi: $it") }
        )

        //Load fs ke recyclerview
        productRecycler = view.findViewById(R.id.product_recycler)

        adapter = ProductAdapter(mutableListOf(), isSeller = isSeller)
        productRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        productRecycler.adapter = adapter

        loadAllProducts()

        //search
        val searchInput = view.findViewById<EditText>(R.id.search_input)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterProducts(s.toString(), currentFilter, currentCategory)
            }

            override fun beforeTextChanged(s: CharSequence?, s1: Int, s2: Int, s3: Int) {}
            override fun onTextChanged(s: CharSequence?, s1: Int, s2: Int, s3: Int) {}
        })

        //filter
        val btnFilter = view.findViewById<ImageButton>(R.id.btn_filter)
        btnFilter.setOnClickListener { showFilterDialog() }

        //nav ke cart
        val btnCart = view.findViewById<ImageButton>(R.id.btn_cart)
        if (isSeller) {
            btnCart.visibility = View.GONE
        } else {
            btnCart.setOnClickListener {
                try {
                    findNavController().navigate(R.id.cartFragment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Gagal membuka keranjang", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //category
        setupCategoryButtons(view)
    }

    private fun loadAllProducts() {
        ProductRepository.getProducts(
            onComplete = { productList ->

                val onlyProducts = productList.map { it.second }

                allProducts = onlyProducts.toMutableList()
                adapter.updateData(allProducts)
            },
            onError = { e ->
                Log.e("BeliFragment", "Gagal load produk: $e")
            }
        )
    }


    //filter func
    private fun filterProducts(query: String, priceFilter: String, category: String) {
        val q = query.trim()

        val filtered = allProducts.filter { product ->
            val matchQuery =
                if (q.isEmpty()) true else product.name.contains(q, ignoreCase = true)

            val matchPrice = when (priceFilter) {
                "Di bawah 100rb" -> product.price < 100_000
                "100rb - 300rb" -> product.price in 100_000..300_000
                "300rb - 500rb" -> product.price in 300_000..500_000
                "500rb - 1jt" -> product.price in 500_000..1_000_000
                "Di atas 1jt" -> product.price > 1_000_000
                else -> true
            }

            val matchCategory =
                if (category == "Semua Produk") true
                else product.category.equals(category, ignoreCase = true)

            matchQuery && matchPrice && matchCategory
        }

        adapter.updateData(filtered)
    }

    //filter dialog
    private fun showFilterDialog() {
        val options = arrayOf(
            "Semua Harga",
            "Di bawah 100rb",
            "100rb - 300rb",
            "300rb - 500rb",
            "500rb - 1jt",
            "Di atas 1jt"
        )
        val selectedIndex = options.indexOf(currentFilter).coerceIn(0, options.size - 1)

        AlertDialog.Builder(requireContext())
            .setTitle("Filter Harga")
            .setSingleChoiceItems(options, selectedIndex) { _, which ->
                currentFilter = options[which]
            }
            .setPositiveButton("Terapkan") { dialog, _ ->
                filterProducts(
                    view?.findViewById<EditText>(R.id.search_input)?.text.toString(),
                    currentFilter,
                    currentCategory
                )
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    //nav category
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

            if (cat.equals(currentCategory, ignoreCase = true)) {
                btn.setBackgroundResource(R.drawable.bg_category_selected)
                btn.setTextColor(resources.getColor(android.R.color.white))
                activeButton = btn
            }

            btn.setOnClickListener {
                currentCategory = cat

                activeButton?.setBackgroundResource(R.drawable.bg_category_normal)
                activeButton?.setTextColor(resources.getColor(android.R.color.black))

                btn.setBackgroundResource(R.drawable.bg_category_selected)
                btn.setTextColor(resources.getColor(android.R.color.white))
                activeButton = btn

                val q = view.findViewById<EditText>(R.id.search_input).text.toString()
                filterProducts(q, currentFilter, currentCategory)
            }

            layout.addView(btn)
        }
    }
}
