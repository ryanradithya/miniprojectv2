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

class CheckoutFragment : Fragment() {

    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var spinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_checkout, container, false)
        val listLayout: LinearLayout = v.findViewById(R.id.checkout_list)
        val tvTotal: TextView = v.findViewById(R.id.checkout_total)
        val btnConfirm: MaterialButton = v.findViewById(R.id.btn_confirm_checkout)

        val selectedItems =
            arguments?.getSerializable("selected_items") as? ArrayList<CartItem> ?: arrayListOf()

        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val buyerUsername = prefs.getString("active_username", "Guest") ?: "Guest"

        // Tampilkan ringkasan item checkout
        var totalCost = 0
        selectedItems.forEach { item ->
            val tv = TextView(requireContext())
            tv.text = "${item.name} × ${item.qty} — Rp ${item.price * item.qty}"
            listLayout.addView(tv)
            totalCost += item.price * item.qty
        }
        tvTotal.text = "Total: Rp $totalCost"

        // Ekspedisi
        val tvExpedition = TextView(requireContext()).apply {
            text = "Pilih Ekspedisi:"
            textSize = 16f
        }
        spinner = Spinner(requireContext())
        listLayout.addView(tvExpedition)
        listLayout.addView(spinner)

        spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        btnConfirm.setOnClickListener {
            if (selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "Keranjang kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expedition = spinner.selectedItem?.toString() ?: ""
            startCheckout(selectedItems, buyerUsername, expedition)
        }

        return v
    }

    override fun onResume() {
        super.onResume()
        val prefs = requireContext().getSharedPreferences("ExpeditionPrefs", Context.MODE_PRIVATE)
        val expeditions = prefs.getStringSet("expeditions_set", setOf("JNE","Tiki","SiCepat"))?.toList() ?: listOf()
        spinnerAdapter.clear()
        spinnerAdapter.addAll(expeditions)
        spinnerAdapter.notifyDataSetChanged()
    }

    // ================================
    // CHECKOUT FIRESTORE AMAN
    // ================================
    private fun startCheckout(
        selectedItems: List<CartItem>,
        buyer: String,
        expedition: String
    ) {

        Toast.makeText(requireContext(), "Memproses checkout...", Toast.LENGTH_SHORT).show()

        // 1) CEK STOK SATU PERSATU
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
                        // Semua stok aman → lanjut proses
                        reduceAllStock(selectedItems, buyer, expedition)
                    }
                },
                onError = {
                    Toast.makeText(requireContext(), "Gagal mengambil data produk", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // ================================
    // KURANGI SEMUA STOK (BATAS RACE CONDITION)
    // ================================
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
                        // Semua stok berhasil dikurangi
                        createOneTransaction(selectedItems, buyer, expedition)
                    }
                },
                onError = {
                    Toast.makeText(requireContext(), "Gagal update stok ${item.name}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // ================================
    // BUAT SATU TRANSAKSI FIRESTORE
    // ================================
    private fun createOneTransaction(
        selectedItems: List<CartItem>,
        buyer: String,
        expedition: String
    ) {

        val totalPrice = selectedItems.sumOf { it.price * it.qty }
        val date = System.currentTimeMillis().toString()

        val data = hashMapOf(
            "items" to selectedItems.map {
                mapOf(
                    "name" to it.name,
                    "qty" to it.qty,
                    "price" to it.price
                )
            },
            "totalPrice" to totalPrice,
            "buyer" to buyer,
            "expedition" to expedition,
            "status" to "Pesanan Masuk",
            "trackingNumber" to null,
            "date" to date,
            "updatedAt" to System.currentTimeMillis()
        )

        // Simpan transaksi
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("transactions")
            .add(data)
            .addOnSuccessListener {

                Toast.makeText(requireContext(), "Checkout berhasil!", Toast.LENGTH_SHORT).show()

                // Kosongkan keranjang
                CartManager.items.removeAll(selectedItems)

                // Pindah ke halaman transaksi
                val bundle = Bundle().apply { putBoolean("from_checkout", true) }
                findNavController().navigate(R.id.action_checkout_to_transaction, bundle)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
            }
    }
}
