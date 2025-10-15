package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BeliFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_beli, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup rekomendasi (horizontal)
        val rekomendasiRecycler = view.findViewById<RecyclerView>(R.id.rekomendasi_recycler)
        rekomendasiRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rekomendasiRecycler.adapter = ProductAdapter(
            ProductRepository.rekomendasiProduk.toMutableList(),
            isRekomendasi = true,
            isSeller = false
        )

        // Setup produk utama (vertical)
        val productRecycler = view.findViewById<RecyclerView>(R.id.product_recycler)
        productRecycler.layoutManager = LinearLayoutManager(requireContext())
        productRecycler.adapter = ProductAdapter(
            ProductRepository.produkUtama.toMutableList(),
            isSeller = false
        )
    }
}
