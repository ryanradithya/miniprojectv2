package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.miniprojectv2.R

class CartFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_cart, container, false)

        // ambil layout container untuk cart list
        val cartList: LinearLayout = v.findViewById(R.id.cart_list)
        cartList.removeAllViews()

        if (CartManager.items.isEmpty()) {
            val tv = TextView(requireContext())
            tv.text = "Keranjang kosong"
            cartList.addView(tv)
        } else {
            CartManager.items.forEach { item ->
                val tv = TextView(requireContext())
                tv.text = "${item.name} x${item.qty} — Rp ${item.price * item.qty}"
                cartList.addView(tv)
            }
        }
        return v
    }
}

