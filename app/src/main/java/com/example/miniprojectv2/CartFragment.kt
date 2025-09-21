package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class CartFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_cart, container, false)

        val cartList: LinearLayout = v.findViewById(R.id.cart_list)
        val btnCheckout: Button = v.findViewById(R.id.btn_checkout)

        fun refreshCart() {
            cartList.removeAllViews()
            if (CartManager.items.isEmpty()) {
                val tv = TextView(requireContext())
                tv.text = "Keranjang kosong"
                cartList.addView(tv)
                btnCheckout.visibility = View.GONE
            } else {
                btnCheckout.visibility = View.VISIBLE
                CartManager.items.forEach { item ->
                    val tv = TextView(requireContext())
                    tv.text = "${item.name} x${item.qty} — Rp ${item.price * item.qty}"
                    cartList.addView(tv)
                }
            }
        }

        refreshCart()

        // aksi checkout
        btnCheckout.setOnClickListener {
            if (CartManager.items.isNotEmpty()) {
                CartManager.items.forEach { item ->
                    TransactionManager.addTransaction(item.name, item.qty, item.price)
                }
                CartManager.items.clear()
                Toast.makeText(requireContext(), "Checkout berhasil!", Toast.LENGTH_SHORT).show()
                refreshCart()
            }
        }

        return v
    }
}
