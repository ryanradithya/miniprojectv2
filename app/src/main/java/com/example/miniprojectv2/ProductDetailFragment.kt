package com.example.miniprojectv2

import android.net.Uri
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

        val name = arguments?.getString("product_name") ?: "Produk"
        val price = arguments?.getInt("product_price") ?: 0
        val stock = arguments?.getInt("product_stock") ?: 0
        val description = arguments?.getString("product_description") ?: "Tidak ada deskripsi!"
        val imageUriString = arguments?.getString("product_image_uri")

        val imageView: ImageView = v.findViewById(R.id.product_image)
        val tvTitle: TextView = v.findViewById(R.id.product_title)
        val tvPrice: TextView = v.findViewById(R.id.product_price)
        val tvStock: TextView = v.findViewById(R.id.product_stock)
        val tvDesc: TextView = v.findViewById(R.id.product_desc)
        val btnAdd: View = v.findViewById(R.id.btn_add_cart) // pakai View agar aman untuk Button / ImageButton
        val btnPlus: TextView = v.findViewById(R.id.btn_plus)
        val btnMinus: TextView = v.findViewById(R.id.btn_minus)
        val tvQty: TextView = v.findViewById(R.id.tv_qty)

        tvTitle.text = name
        tvPrice.text = "Rp $price"
        tvStock.text = "Stok : $stock"
        tvDesc.text = description

        if (!imageUriString.isNullOrEmpty()) {
            imageView.setImageURI(Uri.parse(imageUriString))
        } else {
            imageView.setImageResource(R.drawable.ic_product_placeholder)
        }

        tvDesc.maxLines = 2
        tvDesc.ellipsize = TextUtils.TruncateAt.END
        var isExpanded = false
        tvDesc.setOnClickListener {
            isExpanded = !isExpanded
            tvDesc.maxLines = if (isExpanded) Int.MAX_VALUE else 2
            tvDesc.ellipsize = if (isExpanded) null else TextUtils.TruncateAt.END
        }

        var quantity = 1
        fun updateQty() { tvQty.text = quantity.toString() }
        btnPlus.setOnClickListener { quantity++; updateQty() }
        btnMinus.setOnClickListener { if (quantity > 1) quantity--; updateQty() }

        btnAdd.setOnClickListener {
            if (price <= 0) {
                Toast.makeText(requireContext(), "Harga produk tidak valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val existing = CartManager.items.find { it.name == name }
            if (existing != null) existing.qty += quantity
            else CartManager.items.add(CartItem(name, price, quantity))

            Toast.makeText(requireContext(), "Ditambahkan $quantity × \"$name\" ke keranjang!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_p_to_cart)
        }

        return v
    }
}
