package com.example.miniprojectv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class CheckoutItem(val name: String, val qty: Int, val price: Int)

class CheckoutAdapter(private val items: List<CheckoutItem>) :
    RecyclerView.Adapter<CheckoutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.checkout_item_name)
        val tvPrice: TextView = view.findViewById(R.id.checkout_item_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checkout, parent, false)
        return ViewHolder(view)
    }

    // Isi ViewHolder dengan data (pakai bind)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = "${item.name} x${item.qty}"
        holder.tvPrice.text = "Rp ${item.price * item.qty}"
    }

    override fun getItemCount(): Int = items.size
}
