package com.example.miniprojectv2

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
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

        //refresh cart
        fun refreshCart() {

            cartList.removeAllViews()

            CartRepository.getCart(
                onSuccess = { result ->

                    cartList.removeAllViews()
                    selectedItems.clear()

                    if (result.isEmpty()) {
                        val tv = TextView(requireContext()).apply {
                            text = "Keranjang kosong"
                            textSize = 20f
                            setTypeface(null, Typeface.BOLD)
                            gravity = Gravity.CENTER
                        }

                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.topMargin = 48
                        tv.layoutParams = params

                        cartList.addView(tv)
                        btnCheckout.visibility = View.GONE
                        return@getCart
                    }


                    btnCheckout.visibility = View.VISIBLE

                    result.forEach { (itemId, item) ->
                        val itemView = layoutInflater.inflate(
                            R.layout.item_cart,
                            cartList,
                            false
                        )
                        val ivImage = itemView.findViewById<ImageView>(R.id.cart_item_image)
                        loadCartImage(ivImage, item.imageId)

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

                        //ambil img
                        if (item.imageId.isNotEmpty()) {
                            ImageHandler.getImage(requireContext(), item.imageId) { bytes ->
                                if (bytes != null) {
                                    val bitmap = android.graphics.BitmapFactory
                                        .decodeByteArray(bytes, 0, bytes.size)
                                    ivImage.post {
                                        ivImage.setImageBitmap(bitmap)
                                    }
                                } else {
                                    ivImage.post {
                                        ivImage.setImageResource(R.drawable.ic_product_placeholder)
                                    }
                                }
                            }
                        } else {
                            ivImage.setImageResource(R.drawable.ic_product_placeholder)
                        }

                        var stock = 0

                        //cek stok
                        ProductRepository.findProductByName(
                            item.name,
                            onComplete = { product ->
                                stock = product?.stock ?: 0
                                if (stock == 0) {
                                    cbSelect.isEnabled = false
                                    btnPlus.isEnabled = false
                                    tvName.text = "${item.name} (Stok habis)"
                                    tvName.setTextColor(
                                        resources.getColor(android.R.color.darker_gray, null)
                                    )
                                }
                            },
                            onError = {
                                cbSelect.isEnabled = false
                                btnPlus.isEnabled = false
                            }
                        )

                        val selectedItemIds = mutableSetOf<String>()

                        cbSelect.isChecked = selectedItemIds.contains(itemId)

                        cbSelect.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                selectedItemIds.add(itemId)
                                selectedItems.removeAll { it.name == item.name }
                                selectedItems.add(item.copy())
                            } else {
                                selectedItemIds.remove(itemId)
                                selectedItems.removeAll { it.name == item.name }
                            }
                        }


                        btnPlus.setOnClickListener {
                            if (item.qty < stock) {
                                CartRepository.updateQty(itemId, item.qty + 1)
                                refreshCart()
                            } else {
                                Toast.makeText(requireContext(), "Stok tidak mencukupi", Toast.LENGTH_SHORT).show()
                            }
                        }

                        btnMinus.setOnClickListener {
                            if (item.qty > 1) {
                                CartRepository.updateQty(itemId, item.qty - 1)
                                refreshCart()
                            }
                        }

                        btnDelete.setOnClickListener {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Hapus Produk")
                                .setMessage("Hapus produk dari keranjang?")
                                .setPositiveButton("Ya") { _, _ ->
                                    CartRepository.deleteItem(itemId)
                                    refreshCart()
                                }
                                .setNegativeButton("Tidak", null)
                                .show()
                        }

                        cartList.addView(itemView)
                    }
                },
                onError = {
                    Toast.makeText(requireContext(), "Gagal memuat keranjang", Toast.LENGTH_SHORT).show()
                }
            )
        }

        refreshCart()

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
    private fun loadCartImage(
        imageView: ImageView,
        imageId: String
    ) {
        if (imageId.isEmpty()) {
            //kalau tdk ada
            imageView.setImageResource(R.drawable.ic_product_placeholder)
            return
        }

        //ambil dari funct di ImageHandler
        ImageHandler.getImage(requireContext(), imageId) { bytes ->
            if (bytes != null) {
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(
                    bytes, 0, bytes.size
                )
                imageView.post {
                    imageView.setImageBitmap(bitmap)
                }
            } else {
                imageView.post {
                    imageView.setImageResource(R.drawable.ic_product_placeholder)
                }
            }
        }
    }

}
