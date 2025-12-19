package com.example.miniprojectv2

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.OkHttpClient
import okio.IOException
import com.example.miniprojectv2.data.CategoryRepository
import com.google.android.material.textfield.TextInputLayout


class JualFragment : Fragment() {

    private var selectedImageUri: Uri? = null
    private lateinit var imagePreview: ImageView
    private val PICK_IMAGE_REQUEST = 100

    private var editMode = false
    private var productIdToEdit: String? = null
    private var productToEdit: Product? = null

    private val categories = mutableListOf<String>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("JualFragment", "onCreateView called")
        return inflater.inflate(R.layout.fragment_jual, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("JualFragment", "onViewCreated initialized")

        val nameInput = view.findViewById<EditText>(R.id.input_name)
        val priceInput = view.findViewById<EditText>(R.id.input_price)
        val stockInput = view.findViewById<EditText>(R.id.input_stock)
        val descInput = view.findViewById<EditText>(R.id.input_description)
        val categorySpinner =
            view.findViewById<AutoCompleteTextView>(R.id.spinner_category)

        val customCategoryLayout =
            view.findViewById<TextInputLayout>(R.id.layout_custom_category)
        val btnAdd = view.findViewById<Button>(R.id.btn_add)
        val btnSelectImage = view.findViewById<Button>(R.id.btn_select_image)
        imagePreview = view.findViewById(R.id.image_preview)

        val customCategoryInput = view.findViewById<EditText>(R.id.input_custom_category)

        loadCategoriesFromDatabase(
            categorySpinner,
            customCategoryLayout,
            customCategoryInput,
            arguments
        )




        //edit
        arguments?.let { args ->
            if (args.getString("edit_mode") == "true") {
                Log.d("JualFragment", "Edit mode enabled")
                editMode = true
                productIdToEdit = args.getString("product_id")

                productToEdit = Product(
                    id = productIdToEdit,
                    name = args.getString("product_name") ?: "",
                    price = args.getInt("product_price"),
                    stock = args.getInt("product_stock"),
                    description = args.getString("product_description") ?: "",
                    category = args.getString("product_category") ?: "",
                    imageUri = args.getString("product_image_uri")
                )

                Log.d("JualFragment", "Loaded product for editing: $productToEdit")

                nameInput.setText(productToEdit!!.name)
                priceInput.setText(productToEdit!!.price.toString())
                stockInput.setText(productToEdit!!.stock.toString())
                descInput.setText(productToEdit!!.description)

                loadProductImage(imagePreview, productToEdit!!.imageUri)

                val matchedCategory = categories.firstOrNull {
                    it.equals(productToEdit!!.category, ignoreCase = true)
                }

                if (matchedCategory != null) {
                    categorySpinner.setText(matchedCategory, false)
                } else {
                    categorySpinner.setText("Lainnya", false)
                    customCategoryInput.visibility = View.VISIBLE
                    customCategoryInput.setText(productToEdit!!.category)
                }



                btnAdd.text = "Simpan Perubahan"
            }
        }

        //image
        btnSelectImage.setOnClickListener {
            Log.d("JualFragment", "Select image button clicked")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        //edit/add
        btnAdd.setOnClickListener {
            Log.d("JualFragment", "Add/Save button clicked")

            val name = nameInput.text.toString().trim()
            val price = priceInput.text.toString().toIntOrNull() ?: 0
            val stock = stockInput.text.toString().toIntOrNull() ?: 0
            val desc = descInput.text.toString().trim()
            val selectedCategory = categorySpinner.text.toString()
            val customCategory = customCategoryInput.text.toString().trim()

            val category = if (selectedCategory == "Lainnya") {
                if (customCategory.isEmpty()) {
                    Toast.makeText(requireContext(), "Masukkan kategori baru", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                customCategory
            } else {
                selectedCategory
            }

            if (name.isEmpty() || desc.isEmpty() || price <= 0) {
                Toast.makeText(requireContext(), "Lengkapi semua data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Select image first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("JualFragment", "Starting image upload: $selectedImageUri")

            val isServerImage = selectedImageUri.toString().startsWith("server://")

            val categoryToSave = category.trim().lowercase()

            val proceedToSave: (String) -> Unit = { imageId ->
                saveProduct(name, price, stock, desc, categoryToSave, imageId, productIdToEdit)
            }

            if (editMode && productIdToEdit != null) {
                if (isServerImage) {
                    // Existing image, no upload needed
                    val existingImageId = selectedImageUri.toString().removePrefix("server://")
                    proceedToSave(existingImageId)
                } else {
                    // New image selected, upload first
                    ImageHandler.uploadImage(requireContext(), selectedImageUri!!) { imageId ->
                        if (imageId == null) {
                            Toast.makeText(requireContext(), "Gagal upload gambar!", Toast.LENGTH_SHORT).show()
                            return@uploadImage
                        }
                        proceedToSave(imageId)
                    }
                }
            } else {
                // Adding new product
                ImageHandler.uploadImage(requireContext(), selectedImageUri!!) { imageId ->
                    if (imageId == null) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Gagal upload gambar!", Toast.LENGTH_SHORT).show()
                        }
                        return@uploadImage
                    }
                    proceedToSave(imageId)
                }
            }
        }

    }
    private fun loadCategoriesFromDatabase(
        categoryInput: AutoCompleteTextView,
        customCategoryLayout: TextInputLayout,
        customCategoryInput: EditText,
        args: Bundle?
    ) {
        CategoryRepository.getAllCategories(
            onSuccess = { dbCategories ->
                categories.clear()
                categories.addAll(dbCategories)

                if (!categories.any { it.equals("Lainnya", true) }) {
                    categories.add("Lainnya")
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    categories
                )
                categoryInput.setAdapter(adapter)

                categoryInput.setOnItemClickListener { _, _, position, _ ->
                    val selected = categories[position]
                    if (selected.equals("Lainnya", true)) {
                        customCategoryLayout.visibility = View.VISIBLE
                    } else {
                        customCategoryLayout.visibility = View.GONE
                        customCategoryInput.text?.clear()
                    }
                }

                // HANDLE EDIT MODE
                args?.let {
                    if (it.getString("edit_mode") == "true") {
                        val productCategory = it.getString("product_category") ?: return@let

                        val matched = categories.firstOrNull {
                            it.equals(productCategory, ignoreCase = true)
                        }

                        if (matched != null) {
                            categoryInput.setText(matched, false)
                        } else {
                            categoryInput.setText("Lainnya", false)
                            customCategoryLayout.visibility = View.VISIBLE
                            customCategoryInput.setText(productCategory)
                        }
                    }
                }
            },
            onError = {
                Toast.makeText(requireContext(), "Gagal memuat kategori", Toast.LENGTH_SHORT).show()
            }
        )
    }

    //saveproduct func
    private fun saveProduct(
        name: String,
        price: Int,
        stock: Int,
        desc: String,
        category: String,
        imageId: String,
        productId: String? = null
    ) {
        val product = Product(
            name = name,
            price = price,
            stock = stock,
            description = desc,
            category = category,
            imageUri = imageId
        )

        if (editMode && productId != null) {
                ProductRepository.updateProductById(productId, product,
                    onComplete = {
                        Toast.makeText(requireContext(), "Produk diperbarui!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    },
                    onError = { e ->
                        Toast.makeText(requireContext(), "Gagal update produk: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )
        } else {
            ProductRepository.addProduct(product,
                onComplete = {
                    Toast.makeText(requireContext(), "Produk berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                },
                onError = { e ->
                    Toast.makeText(requireContext(), "Gagal menyimpan produk: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            Log.d("JualFragment", "Image selected: $selectedImageUri")

            selectedImageUri?.let { uri ->
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imagePreview.setImageURI(uri)
            }
        }
    }

    fun loadProductImage(imageView: ImageView, productImageUri: String?) {
        if (productImageUri.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.ic_product_placeholder)
            return
        }

        try {
                val imageId = productImageUri.removePrefix("server://")
                ImageHandler.getImage(requireContext(), imageId) { bytes ->
                    if (bytes != null) {
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imageView.post { imageView.setImageBitmap(bitmap) }
                    } else {
                        imageView.post { imageView.setImageResource(R.drawable.ic_product_placeholder) }
                    }
                }
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.ic_product_placeholder)
        }
    }



}
