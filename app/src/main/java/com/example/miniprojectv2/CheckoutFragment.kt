package com.example.miniprojectv2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

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
        val btnConfirm: Button = v.findViewById(R.id.btn_confirm_checkout)

        val selectedItems = arguments?.getSerializable("selected_items") as? ArrayList<CartItem> ?: arrayListOf()
        val prefs = requireContext().getSharedPreferences("UserPrefs", 0)
        val buyerUsername = prefs.getString("active_username", "Guest") ?: "Guest"

        // ===== Show items & total =====
        var total = 0
        selectedItems.forEach { item ->
            val tv = TextView(requireContext())
            tv.text = "${item.name} ×${item.qty} — Rp ${item.price * item.qty}"
            listLayout.addView(tv)
            total += item.price * item.qty
        }
        tvTotal.text = "Total: Rp $total"

        // ===== Delivery Expedition Spinner =====
        val tvExpeditionLabel = TextView(requireContext()).apply {
            text = "Pilih Ekspedisi:"
            textSize = 16f
        }
        spinner = Spinner(requireContext())
        listLayout.addView(tvExpeditionLabel)
        listLayout.addView(spinner)

        spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        // ===== Confirm checkout =====
        btnConfirm.setOnClickListener {
            if (selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "Keranjang kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedExpedition = spinner.selectedItem?.toString() ?: ""

            // 🔹 Cek stok sebelum transaksi
            for (item in selectedItems) {
                val product = ProductRepository.findProductByName(item.name)
                if (product == null) {
                    Toast.makeText(requireContext(), "Produk ${item.name} tidak ditemukan!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (product.stock < item.qty) {
                    Toast.makeText(requireContext(), "Stok ${item.name} tidak mencukupi!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            //Proses transaksi dan update stok
            selectedItems.forEach { item ->
                val product = ProductRepository.findProductByName(item.name)
                product?.let {
                    it.stock -= item.qty  // Kurangi stok produk sesuai jumlah dibeli
                }

                TransactionManager.addTransaction(
                    item.name,
                    item.qty,
                    item.price,
                    buyerUsername,
                    selectedExpedition
                )
            }

            Toast.makeText(requireContext(), "Checkout berhasil!", Toast.LENGTH_SHORT).show()

            // 🔹 Hapus dari keranjang
            CartManager.items.removeAll(selectedItems)

            // 🔹 Kembali ke halaman transaksi seperti sistem kamu
            val bundle = Bundle().apply { putBoolean("from_checkout", true) }
            findNavController().navigate(R.id.action_checkout_to_transaction, bundle)
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
}
