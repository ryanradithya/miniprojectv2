package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class CheckoutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_checkout, container, false)
        val listLayout: LinearLayout = v.findViewById(R.id.checkout_list)
        val tvTotal: TextView = v.findViewById(R.id.checkout_total)
        val btnConfirm: Button = v.findViewById(R.id.btn_confirm_checkout)

        val selectedItems =
            arguments?.getSerializable("selected_items") as? ArrayList<CartItem> ?: arrayListOf()

        var total = 0
        selectedItems.forEach { item ->
            val tv = TextView(requireContext())
            tv.text = "${item.name} ×${item.qty} — Rp ${item.price * item.qty}"
            listLayout.addView(tv)
            total += item.price * item.qty
        }
        tvTotal.text = "Total: Rp $total"

        btnConfirm.setOnClickListener {
            if (selectedItems.isNotEmpty()) {
                selectedItems.forEach { item ->
                    TransactionManager.addTransaction(item.name, item.qty, item.price)
                }
                Toast.makeText(requireContext(), "Checkout berhasil!", Toast.LENGTH_SHORT).show()

                // hapus item yang sudah di-checkout
                CartManager.items.removeAll(selectedItems)
                findNavController().navigate(R.id.action_checkout_to_transaction)
            }
        }

        return v
    }
}
