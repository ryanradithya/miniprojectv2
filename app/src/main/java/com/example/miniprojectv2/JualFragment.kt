package com.example.miniprojectv2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class JualFragment : Fragment() {

    private var selectedImageUri: Uri? = null
    private lateinit var imagePreview: ImageView
    private val PICK_IMAGE_REQUEST = 100

    // Mode edit
    private var editMode = false
    private var productToEdit: Product? = null

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
        val descInput = view.findViewById<EditText>(R.id.input_description)
        val categorySpinner = view.findViewById<Spinner>(R.id.spinner_category)
        val btnAdd = view.findViewById<Button>(R.id.btn_add)
        val btnSelectImage = view.findViewById<Button>(R.id.btn_select_image)
        imagePreview = view.findViewById(R.id.image_preview)

        val categories = listOf("Kamera", "Lensa", "Aksesori", "Film", "Lainnya")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter

        // MODE EDIT: ambil data dari bundle
        arguments?.let { args ->
            if (args.getString("edit_mode") == "true") {
                editMode = true

                productToEdit = Product(
                    name = args.getString("product_name") ?: "",
                    price = args.getInt("product_price"),
                    stock = args.getInt("product_stock"),
                    description = args.getString("product_description") ?: "",
                    category = args.getString("product_category") ?: "Lainnya",
                    imageUri = args.getString("product_image_uri") // URI dari bundle
                )

                // isi form
                nameInput.setText(productToEdit?.name)
                priceInput.setText(productToEdit?.price.toString())
                stockInput.setText(productToEdit?.stock.toString())
                descInput.setText(productToEdit?.description)
                selectedImageUri = productToEdit?.imageUri?.let { Uri.parse(it) }
                if (selectedImageUri != null) {
                    imagePreview.setImageURI(selectedImageUri)
                } else {
                    imagePreview.setImageResource(R.drawable.ic_product_placeholder)
                }

                val pos = categories.indexOf(productToEdit?.category)
                if (pos != -1) categorySpinner.setSelection(pos)

                btnAdd.text = "Simpan Perubahan"
            }
        }

        // Pilih gambar
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Tambah / Simpan produk
        btnAdd.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val price = priceInput.text.toString().toIntOrNull() ?: 0
            val stock = stockInput.text.toString().toIntOrNull() ?: 0
            val description = descInput.text.toString().trim()
            val category = categorySpinner.selectedItem.toString()

            if (name.isBlank() || price <= 0 || stock < 0 || description.isBlank()) {
                Toast.makeText(requireContext(), "Isi semua data dengan benar!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (editMode && productToEdit != null) {
                val updatedProduct = productToEdit!!.copy(
                    name = name,
                    price = price,
                    stock = stock,
                    description = description,
                    category = category,
                    imageUri = selectedImageUri?.toString() ?: productToEdit!!.imageUri
                )
                ProductRepository.updateProduct(productToEdit!!, updatedProduct)
                Toast.makeText(requireContext(), "Produk berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            } else {
                val newProduct = Product(
                    name = name,
                    price = price,
                    stock = stock,
                    description = description,
                    category = category,
                    imageUri = selectedImageUri?.toString()
                )
                ProductRepository.addProduct(newProduct)
                Toast.makeText(requireContext(), "Produk \"$name\" berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            }

            // Reset form
            nameInput.text.clear()
            priceInput.text.clear()
            stockInput.text.clear()
            descInput.text.clear()
            categorySpinner.setSelection(0)
            selectedImageUri = null
            imagePreview.setImageResource(R.drawable.ic_product_placeholder)

            findNavController().popBackStack()
        }
    }

    // hasil pilih gambar
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            imagePreview.setImageURI(selectedImageUri)
        }
    }
}
