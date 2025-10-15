package com.example.miniprojectv2

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

        // Ambil data dari Bundle
        val name = arguments?.getString("product_name") ?: "Produk"
        val price = arguments?.getInt("product_price") ?: 0
        val description = arguments?.getString("product_description") ?: "Tidak ada deskripsi produk!"
        val imageRes = arguments?.getInt("product_image") ?: R.drawable.ic_product_placeholder

        // Bind view
        val imageView: ImageView = v.findViewById(R.id.product_image)
        val tvTitle: TextView = v.findViewById(R.id.product_title)
        val tvPrice: TextView = v.findViewById(R.id.product_price)
        val tvDesc: TextView = v.findViewById(R.id.product_desc)
        val btnAdd: Button = v.findViewById(R.id.btn_add_cart)
        val btnPlus: TextView = v.findViewById(R.id.btn_plus)
        val btnMinus: TextView = v.findViewById(R.id.btn_minus)

        val tvQty: TextView = v.findViewById(R.id.tv_qty)

        // Set data
        tvTitle.text = name
        tvPrice.text = "Rp $price"
        tvDesc.text = description
        imageView.setImageResource(imageRes)

        // Deskripsi expandable
        tvDesc.maxLines = 2
        tvDesc.ellipsize = TextUtils.TruncateAt.END

        var isExpanded = false
        tvDesc.setOnClickListener {
            isExpanded = !isExpanded
            tvDesc.maxLines = if (isExpanded) Int.MAX_VALUE else 2
            tvDesc.ellipsize = if (isExpanded) null else TextUtils.TruncateAt.END
        }

        // Jumlah produk
        var quantity = 1
        fun updateQty() { tvQty.text = quantity.toString() }
        btnPlus.setOnClickListener { quantity++; updateQty() }
        btnMinus.setOnClickListener { if (quantity > 1) quantity--; updateQty() }

        // Tambah ke keranjang
        btnAdd.setOnClickListener {
            val existing = CartManager.items.find { it.name == name }
            if (existing != null) existing.qty += quantity
            else CartManager.items.add(CartItem(name, price, quantity))

            Toast.makeText(requireContext(), "Ditambahkan $quantity × \"$name\" ke keranjang!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_p_to_cart)
        }

        return v
    }
}
