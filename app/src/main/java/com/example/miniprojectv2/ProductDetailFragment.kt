package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

object CartManager {
    val items = mutableListOf<CartItem>()
}

data class CartItem(val name: String, val price: Int, var qty: Int = 1)

class ProductDetailFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_product_detail, container, false)

        // Ambil argumen
        val name = arguments?.getString("product_name") ?: "Produk"
        val price = arguments?.getInt("product_price") ?: 0

        // View dari layout
        val tvTitle: TextView = v.findViewById(R.id.product_title)
        val tvPrice: TextView = v.findViewById(R.id.product_price)
        val tvDesc: TextView = v.findViewById(R.id.product_desc)
        val btnAdd: Button = v.findViewById(R.id.btn_add_cart)

        // Set data ke UI
        tvTitle.text = name
        tvPrice.text = "Rp $price"
        tvDesc.text = "Deskripsi singkat untuk $name"

        // Listener tombol tambah keranjang
        btnAdd.setOnClickListener {
            val existing = CartManager.items.find { it.name == name }
            if (existing != null) {
                existing.qty += 1
            } else {
                CartManager.items.add(CartItem(name, price))
            }
            Toast.makeText(requireContext(), "Produk \"$name\" ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show()
            // pindah ke cart
            findNavController().navigate(R.id.cartFragment)
        }

        return v
    }
}
