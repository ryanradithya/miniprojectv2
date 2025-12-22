package com.example.miniprojectv2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore

class CheckoutFragment : Fragment() {

    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var spinner: Spinner
    private val expeditionList = mutableListOf<Expedition>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_checkout, container, false)
        val productContainer =
            v.findViewById<LinearLayout>(R.id.container_products)
        val tvTotal: TextView = v.findViewById(R.id.checkout_total)
        val btnConfirm: MaterialButton = v.findViewById(R.id.btn_confirm_checkout)
        spinner = v.findViewById(R.id.spinner_expedition)

        val selectedItems =
            arguments?.getSerializable("selected_items") as? ArrayList<CartItem> ?: arrayListOf()

        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val buyerEmail = prefs.getString("active_email", "") ?: ""

        var totalCost = 0
        selectedItems.forEach { item ->
            val itemView = layoutInflater.inflate(
                R.layout.item_checkout_product,
                productContainer,
                false
            )

            itemView.findViewById<TextView>(R.id.tv_product_name).text = item.name
            itemView.findViewById<TextView>(R.id.tv_product_qty).text = "×${item.qty}"
            itemView.findViewById<TextView>(R.id.tv_product_price).text =
                "Rp ${item.price * item.qty}"

            productContainer.addView(itemView)
            totalCost += item.price * item.qty
        }

        tvTotal.text = "Total: Rp $totalCost"

        spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf()
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        loadExpeditions()

        btnConfirm.setOnClickListener {

            if (expeditionList.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Ekspedisi belum tersedia",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val selectedIndex = spinner.selectedItemPosition
            if (selectedIndex < 0) {
                Toast.makeText(
                    requireContext(),
                    "Pilih ekspedisi terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val expedition = expeditionList[selectedIndex].name

            startCheckout(
                selectedItems = selectedItems,
                buyer = buyerEmail,
                expedition = expedition
            )
        }


        return v
    }

    private fun loadExpeditions() {

        FirebaseFirestore.getInstance()
            .collection("expeditions")
            .orderBy("name")
            .get()
            .addOnSuccessListener { snapshot ->

                expeditionList.clear()
                spinnerAdapter.clear()

                snapshot.forEach { doc ->
                    val e = doc.toObject(Expedition::class.java)
                    e.id = doc.id
                    expeditionList.add(e)
                    spinnerAdapter.add(e.name)
                }

                spinnerAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Gagal memuat ekspedisi",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    //checkout fs
    private fun startCheckout(
        selectedItems: List<CartItem>,
        buyer: String,
        expedition: String
    ) {

        Toast.makeText(requireContext(), "Memproses checkout...", Toast.LENGTH_SHORT).show()

        var checked = 0
        selectedItems.forEach { item ->
            ProductRepository.findProductByName(
                item.name,
                onComplete = { product ->
                    if (product == null) {
                        Toast.makeText(requireContext(), "Produk ${item.name} tidak ditemukan!", Toast.LENGTH_SHORT).show()
                        return@findProductByName
                    }

                    if (product.stock < item.qty) {
                        Toast.makeText(requireContext(),
                            "Stok tidak mencukupi untuk ${item.name}", Toast.LENGTH_SHORT).show()
                        return@findProductByName
                    }

                    checked++
                    if (checked == selectedItems.size) {
                        reduceAllStock(selectedItems, buyer, expedition)
                    }
                },
                onError = {
                    Toast.makeText(requireContext(), "Gagal mengambil data produk", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    //reduce stok
    private fun reduceAllStock(
        selectedItems: List<CartItem>,
        buyer: String,
        expedition: String
    ) {
        var completed = 0
        val totalItems = selectedItems.size

        selectedItems.forEach { item ->
            ProductRepository.reduceStock(
                productName = item.name,
                qty = item.qty,
                onComplete = {
                    completed++
                    if (completed == totalItems) {
                        //semua stok berhasil dikurangi
                        createOneTransaction(selectedItems, buyer, expedition)
                    }
                },
                onError = {
                    Toast.makeText(requireContext(), "Gagal update stok ${item.name}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    //func transaksi
    private fun createOneTransaction(
        selectedItems: List<CartItem>,
        buyer: String,
        expedition: String
    ) {

        val transactionItems = selectedItems.map {
            TransactionItem(
                name = it.name,
                price = it.price,
                qty = it.qty,
                sellerEmail = it.sellerEmail
            )
        }

        TransactionManager.addTransaction(
            buyer = buyer,
            expedition = expedition,
            items = transactionItems,
            onSuccess = {

                Toast.makeText(requireContext(), "Checkout berhasil!", Toast.LENGTH_SHORT).show()

                CartRepository.clearCart {

                    if (!isAdded) return@clearCart

                    findNavController().navigate(
                        R.id.action_checkout_to_transaction,
                        Bundle().apply {
                            putBoolean("from_checkout", true)
                        }
                    )
                }
            },
            onError = {
                Toast.makeText(requireContext(), "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
            }
        )
    }

}
