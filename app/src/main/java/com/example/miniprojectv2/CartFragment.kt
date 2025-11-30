package com.example.miniprojectv2

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class CartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_cart, container, false)
        val cartList: LinearLayout = v.findViewById(R.id.cart_list)
        val btnCheckout: MaterialButton = v.findViewById(R.id.btn_checkout)

        val selectedItems = mutableListOf<CartItem>()

        fun refreshCart() {
            cartList.removeAllViews()
            selectedItems.clear()

            // Jika keranjang kosong, tampilkan pesan
            if (CartManager.items.isEmpty()) {
                val tv = TextView(requireContext())
                tv.text = "Keranjang kosong"
                cartList.addView(tv)
                btnCheckout.visibility = View.GONE
                return
            }

            btnCheckout.visibility = View.VISIBLE

            // Tambahkan setiap item ke keranjang
            CartManager.items.forEach { item ->
                val itemView = layoutInflater.inflate(R.layout.item_cart, cartList, false)

                val cbSelect = itemView.findViewById<CheckBox>(R.id.checkbox_select)
                val tvName = itemView.findViewById<TextView>(R.id.cart_item_name)
                val tvPrice = itemView.findViewById<TextView>(R.id.cart_item_price)
                val tvQty = itemView.findViewById<TextView>(R.id.tv_qty_cart)
                val btnMinus = itemView.findViewById<Button>(R.id.btn_minus_cart)
                val btnPlus = itemView.findViewById<Button>(R.id.btn_plus_cart)
                val btnDelete = itemView.findViewById<ImageButton>(R.id.btn_delete_cart)

                tvName.text = item.name
                tvQty.text = item.qty.toString()
                tvPrice.text = "Rp ${item.price * item.qty}"

                // kita akan isi stok dari Firestore
                var stock = 0

                ProductRepository.findProductByName(
                    item.name,
                    onComplete = { product ->
                        stock = product?.stock ?: 0

                        // Jika stok habis, nonaktifkan pilihan dan tombol plus
                        if (stock == 0) {
                            cbSelect.isEnabled = false
                            btnPlus.isEnabled = false
                            tvName.text = "${item.name} (Stok habis)"
                            tvName.setTextColor(
                                resources.getColor(
                                    android.R.color.darker_gray,
                                    null
                                )
                            )
                        }
                    },
                    onError = {
                        // jika gagal ambil stok, anggap saja 0 supaya aman
                        stock = 0
                        cbSelect.isEnabled = false
                        btnPlus.isEnabled = false
                        tvName.text = "${item.name} (Gagal cek stok)"
                        tvName.setTextColor(
                            resources.getColor(
                                android.R.color.darker_gray,
                                null
                            )
                        )
                    }
                )

                cbSelect.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        if (!selectedItems.contains(item)) selectedItems.add(item)
                    } else {
                        selectedItems.remove(item)
                    }
                }

                // Tombol tambah qty
                btnPlus.setOnClickListener {
                    if (stock == 0) {
                        Toast.makeText(
                            requireContext(),
                            "Stok tidak mencukupi!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    if (item.qty < stock) {
                        item.qty++
                        tvQty.text = item.qty.toString()
                        tvPrice.text = "Rp ${item.price * item.qty}"
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Stok tidak mencukupi!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // Tombol kurang qty
                btnMinus.setOnClickListener {
                    if (item.qty > 1) {
                        item.qty--
                        tvQty.text = item.qty.toString()
                        tvPrice.text = "Rp ${item.price * item.qty}"
                    }
                }

                // Tombol hapus
                btnDelete.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Hapus Produk")
                        .setMessage("Apakah Anda yakin ingin menghapus produk ini?")
                        .setPositiveButton("Ya") { _, _ ->
                            CartManager.items.remove(item)
                            refreshCart()
                        }
                        .setNegativeButton("Tidak", null)
                        .show()
                }

                cartList.addView(itemView)
            }
        }

        refreshCart()

        // Tombol checkout → pindah ke CheckoutFragment
        btnCheckout.setOnClickListener {
            if (selectedItems.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Pilih produk untuk checkout",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val bundle = Bundle().apply {
                putSerializable("selected_items", ArrayList(selectedItems.map { it.copy() }))
            }
            findNavController().navigate(R.id.action_cart_to_checkout, bundle)
        }

        return v
    }
}
