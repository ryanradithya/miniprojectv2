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
        val stockInput = view.findViewById<EditText>(R.id.input_stock)
        val descInput = view.findViewById<EditText>(R.id.input_desc)
        val btn = view.findViewById<Button>(R.id.btn_add)

        btn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val price = priceInput.text.toString().toIntOrNull() ?: 0
            val stock = stockInput.text.toString().toIntOrNull() ?: 0
            val description = descInput.text.toString().trim()

            if (name.isBlank() || price <= 0 || stock <= 0 || description.isBlank()) {
                Toast.makeText(requireContext(), "Isi semua data dengan benar!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newProduct = Product(
                name = name,
                price = price,
                imageRes = R.drawable.ic_product_placeholder,
                stock = stock,
                description = description
            )

            ProductRepository.addProduct(newProduct)

            Toast.makeText(requireContext(), "Produk \"$name\" berhasil ditambahkan!", Toast.LENGTH_SHORT).show()

            // Kosongkan input
            nameInput.text.clear()
            priceInput.text.clear()
            stockInput.text.clear()
            descInput.text.clear()

            // Kembali ke fragment sebelumnya (misalnya tab Beli)
            findNavController().popBackStack()
        }
    }
}
