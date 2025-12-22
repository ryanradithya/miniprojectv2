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
import com.example.miniprojectv2.data.CategoryRepository
import com.example.miniprojectv2.utils.toTitleCase


class BeliFragment : Fragment() {

    private lateinit var adapter: ProductAdapter
    private lateinit var productRecycler: RecyclerView

    private var allProducts = mutableListOf<Product>()   // data firestore
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
        productRecycler.layoutManager =
            if (isSeller) {
                LinearLayoutManager(requireContext())
            } else {
                GridLayoutManager(requireContext(), 2)
            }
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter_price, null)

        val rgPrice = dialogView.findViewById<RadioGroup>(R.id.rg_price)
        val btnApply = dialogView.findViewById<Button>(R.id.btn_apply)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)

        // Set radio sesuai filter aktif
        for (i in 0 until rgPrice.childCount) {
            val rb = rgPrice.getChildAt(i) as RadioButton
            if (rb.text.toString() == currentFilter) {
                rb.isChecked = true
                break
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnApply.setOnClickListener {
            val checkedId = rgPrice.checkedRadioButtonId
            if (checkedId != -1) {
                val selectedRadio = dialogView.findViewById<RadioButton>(checkedId)
                currentFilter = selectedRadio.text.toString()
            }

            filterProducts(
                view?.findViewById<EditText>(R.id.search_input)?.text.toString(),
                currentFilter,
                currentCategory
            )

            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    //nav category
    private fun setupCategoryButtons(view: View) {
        val layout = view.findViewById<LinearLayout>(R.id.category_navbar)
        layout.removeAllViews()

        CategoryRepository.getAllCategories(
            onSuccess = { dbCategories ->
                val categories = mutableListOf("Semua Produk")
                categories.addAll(dbCategories)
                renderCategoryButtons(view, layout, categories)
            },
            onError = {
                Toast.makeText(requireContext(), "Gagal memuat kategori", Toast.LENGTH_SHORT).show()
            }
        )
    }
    private fun renderCategoryButtons(
        view: View,
        layout: LinearLayout,
        categories: List<String>
    ) {
        var activeButton: Button? = null

        categories.forEach { cat ->
            val btn = Button(requireContext()).apply {
                text = cat
                textSize = 14f
                isAllCaps = false
                setBackgroundResource(R.drawable.bg_category_normal)
                setTextColor(resources.getColor(android.R.color.black))

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 0, 8, 0)
                }
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
