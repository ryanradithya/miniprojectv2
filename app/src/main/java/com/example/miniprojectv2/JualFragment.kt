package com.example.miniprojectv2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

    private var editMode = false
    private var productIdToEdit: String? = null
    private var productToEdit: Product? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

        // kategori
        val categories = listOf("Kamera Analog", "Lensa Analog", "Tas Kamera", "Roll Film", "Lainnya")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter

        // ===============================
        // MODE EDIT
        // ===============================
        arguments?.let { args ->
            if (args.getString("edit_mode") == "true") {
                editMode = true
                productIdToEdit = args.getString("product_id")

                productToEdit = Product(
                    name = args.getString("product_name") ?: "",
                    price = args.getInt("product_price"),
                    stock = args.getInt("product_stock"),
                    description = args.getString("product_description") ?: "",
                    category = args.getString("product_category") ?: "",
                    imageUri = args.getString("product_image_uri")
                )

                // isi form edit
                nameInput.setText(productToEdit!!.name)
                priceInput.setText(productToEdit!!.price.toString())
                stockInput.setText(productToEdit!!.stock.toString())
                descInput.setText(productToEdit!!.description)

                selectedImageUri = productToEdit!!.imageUri?.let { Uri.parse(it) }
                try {
                    imagePreview.setImageURI(selectedImageUri)
                }
                catch(e: Exception)
                {
                    imagePreview.setImageResource(R.drawable.ic_product_placeholder)
                }

                val pos = categories.indexOf(productToEdit!!.category)
                if (pos != -1) categorySpinner.setSelection(pos)

                btnAdd.text = "Simpan Perubahan"
            }
        }

        // ===============================
        // PILIH GAMBAR
        // ===============================
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // ===============================
        // SIMPAN / TAMBAH PRODUK
        // ===============================
        btnAdd.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val price = priceInput.text.toString().toIntOrNull() ?: 0
            val stock = stockInput.text.toString().toIntOrNull() ?: 0
            val desc = descInput.text.toString().trim()
            val category = categorySpinner.selectedItem.toString()

            if (name.isEmpty() || desc.isEmpty() || price <= 0) {
                Toast.makeText(requireContext(), "Lengkapi semua data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newProduct = Product(
                name = name,
                price = price,
                stock = stock,
                description = desc,
                category = category,
                imageUri = selectedImageUri?.toString() ?: ""
            )

            // ===============================
            // UPDATE PRODUK
            // ===============================
            if (editMode && productIdToEdit != null) {

                ProductRepository.updateProductById(
                    productIdToEdit!!,
                    newProduct,
                    onComplete = {
                        Toast.makeText(requireContext(), "Produk diperbarui!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    },
                    onError = {
                        Toast.makeText(requireContext(), "Gagal update produk", Toast.LENGTH_SHORT).show()
                    }
                )
                return@setOnClickListener
            }

            // ===============================
            // TAMBAH PRODUK BARU
            // ===============================
            ProductRepository.addProduct(
                newProduct,
                onComplete = {
                    Toast.makeText(requireContext(), "Produk berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                },
                onError = {
                    Toast.makeText(requireContext(), "Gagal menambah produk", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // preview gambar
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let { uri ->
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imagePreview.setImageURI(uri)
            }
        }
    }


}
