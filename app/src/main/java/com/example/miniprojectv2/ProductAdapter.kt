package com.example.miniprojectv2

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentId
import okhttp3.OkHttpClient
import okio.IOException
import android.widget.Button


class ProductAdapter(
    private val items: MutableList<Product>,
    private val isRekomendasi: Boolean = false,
    private val isSeller: Boolean = false
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

//    view
    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView? =
            view.findViewById(R.id.product_tile) ?: view.findViewById(R.id.rekomendasi_tile)
        val title: TextView? =
            view.findViewById(R.id.product_title) ?: view.findViewById(R.id.rekomendasi_title)
        val price: TextView? =
            view.findViewById(R.id.product_price) ?: view.findViewById(R.id.rekomendasi_price)
        val image: ImageView? =
            view.findViewById(R.id.product_image) ?: view.findViewById(R.id.rekomendasi_image)

        val stock: TextView? = view.findViewById(R.id.product_stock)

//        tombol untuk seller
        val btnEdit: ImageButton? = view.findViewById(R.id.btn_edit)
        val btnDelete: ImageButton? = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val layout = when {
            isRekomendasi -> R.layout.item_rekomendasi
            isSeller -> R.layout.item_product_seller
            else -> R.layout.item_product
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {

        val product = items[position]

        holder.title?.text = product.name
        holder.price?.text = "Rp ${product.price}"

        // tampilin stok untuk produk
        if (!isRekomendasi) {
            holder.stock?.text = "Stok: ${product.stock}"
        } else {
            holder.stock?.visibility = View.GONE
        }

        // gambar produk
        if (!product.imageUri.isNullOrEmpty()) {
//            try {
//
//                val imageView = holder.image
//
//                if (!product.imageUri.isNullOrEmpty()) {
//
//                    val imageId = product.imageUri!!
//                    val url = "http://10.0.2.2:8000/image/$imageId"
//
//                    // Cache key = imageId
//                    val cached = ImageCache.get(imageId)
//                    if (cached != null) {
//                        val bitmap = BitmapFactory.decodeByteArray(cached, 0, cached.size)
//                        imageView?.setImageBitmap(bitmap)
//                        return
//                    }
//
//                    val request = okhttp3.Request.Builder()
//                        .url(url)
//                        .build()
//
//                    OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
//                        override fun onFailure(call: okhttp3.Call, e: IOException) {
//                            e.printStackTrace()
//                            imageView?.post {
//                                imageView.setImageResource(R.drawable.ic_product_placeholder)
//                            }
//                        }
//
//                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
//                            val bytes = response.body?.bytes()
//                            if (bytes != null) {
//
//                                // Save in cache
//                                ImageCache.put(imageId, bytes)
//
//                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//                                imageView?.post {
//                                    imageView.setImageBitmap(bitmap)
//                                }
//                            } else {
//                                imageView?.post {
//                                    imageView.setImageResource(R.drawable.ic_product_placeholder)
//                                }
//                            }
//                        }
//                    })
//
//                } else {
//                    imageView?.setImageResource(R.drawable.ic_product_placeholder)
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                // placeholder klo exc
//                holder.image?.setImageResource(R.drawable.ic_product_placeholder)
//            }

            // gambar produk
            if (!product.imageUri.isNullOrEmpty()) {
                val imageId = product.imageUri!!

                // Show placeholder first
                holder.image?.setImageResource(R.drawable.ic_product_placeholder)

                ImageHandler.getImage(holder.itemView.context, imageId) { bytes ->
                    if (bytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        (holder.itemView.context as Activity).runOnUiThread {
                            holder.image?.setImageBitmap(bitmap)
                        }
                    } else {
                        (holder.itemView.context as Activity).runOnUiThread {
                            holder.image?.setImageResource(R.drawable.ic_product_placeholder)
                        }
                    }
                }
            }
        } else {
            holder.image?.setImageResource(R.drawable.ic_product_placeholder)
        }

        val context = holder.itemView.context
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentSellerEmail = prefs.getString("active_email", "")

        val isOwner = product.sellerEmail == currentSellerEmail

        // tampilan produk untuk seller
        if (isSeller && isOwner) {
            holder.btnEdit?.visibility = View.VISIBLE
            holder.btnDelete?.visibility = View.VISIBLE

            // tombol Edit
            holder.btnEdit?.setOnClickListener {

                if (!isOwner) {
                    Toast.makeText(
                        context,
                        "Anda tidak berhak mengedit produk ini",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                Log.d("ProductAdapter", "Edit produk: ${product.name}")
                val bundle = Bundle().apply {
                    putString("product_id", product.id)
                    putString("edit_mode", "true")
                    putString("product_name", product.name)
                    putInt("product_price", product.price)
                    putInt("product_stock", product.stock)
                    putString("product_description", product.description)
                    putString("product_category", product.category)
                    putString("product_image_uri", product.imageUri)
                }
                it.findNavController().navigate(R.id.jualFragment, bundle)
            }


            // tombol delete produk
            holder.btnDelete?.setOnClickListener {

                val context = holder.itemView.context

                val dialogView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_delete_product, null)

                val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
                val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
                val btnDelete = dialogView.findViewById<Button>(R.id.btn_delete)

                tvMessage.text = "Hapus produk \"${product.name}\"?"

                val dialog = AlertDialog.Builder(context)
                    .setView(dialogView)
                    .create()

                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                btnDelete.setOnClickListener {
                    if (!isOwner) {
                        Toast.makeText(
                            context,
                            "Anda tidak berhak menghapus produk ini",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                        return@setOnClickListener
                    }

                    ProductRepository.deleteProduct(
                        product.name,
                        onComplete = {
                            val pos = holder.adapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                items.removeAt(pos)
                                notifyItemRemoved(pos)
                            }
                            Toast.makeText(context, "Produk dihapus", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        },
                        onError = {
                            Toast.makeText(context, "Gagal menghapus produk", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                dialog.show()
            }


        } else {
            // tampilan pembeli
            holder.btnEdit?.visibility = View.GONE
            holder.btnDelete?.visibility = View.GONE

            holder.card?.setOnClickListener {
                Log.d("ProductAdapter", "Klik produk: ${product.name}")
                try {
                    val b = Bundle().apply {
                        putString("product_name", product.name)
                        putInt("product_price", product.price)
                        putString("product_description", product.description)
                        putInt("product_stock", product.stock)
                        putString("product_category", product.category)
                        putString("product_image_uri", "server://${product.imageUri}")
                    }
                    it.findNavController().navigate(R.id.productDetailFragment, b)
                } catch (e: Exception) {
                    Log.e("ProductAdapter", "Error saat navigate", e)
                }
            }
        }
        holder.itemView.findViewById<TextView>(R.id.product_rating)?.text =
            "⭐ ${String.format("%.1f", product.rating)}"

    }

    //counter jumlah produk
    override fun getItemCount(): Int = items.size
    fun updateData(newList: List<Product>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    object ImageCache {
        private val cache = HashMap<String, ByteArray>()

        fun get(id: String): ByteArray? = cache[id]
        fun put(id: String, data: ByteArray) { cache[id] = data }
    }

}
