package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class JualFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_jual, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nameInput = view.findViewById<EditText>(R.id.input_name)
        val priceInput = view.findViewById<EditText>(R.id.input_price)
        val btn = view.findViewById<Button>(R.id.btn_add)

        btn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val price = priceInput.text.toString().toIntOrNull() ?: 0

            if (name.isNotBlank() && price > 0) {
                val newProduct = Product(name, price, R.drawable.ic_product_placeholder)
                ProductRepository.addProduct(newProduct)

                // feedback ke user
                Toast.makeText(requireContext(), "Produk \"$name\" berhasil ditambahkan!", Toast.LENGTH_SHORT).show()

                // Kosongkan input setelah simpan
                nameInput.text.clear()
                priceInput.text.clear()

                // Kembali ke tab Beli (pop back ke fragment sebelumnya)
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Nama & harga harus valid!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
