package com.example.miniprojectv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController

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

        // Ambil CardView tile produk
        val productTile1: CardView = view.findViewById(R.id.product_tile_1)
        val productTile2: CardView = view.findViewById(R.id.product_tile_2)
        val productTile3: CardView = view.findViewById(R.id.product_tile_3)

        // Klik tile 1 → navigate ke detail
        productTile1.setOnClickListener {
            val b = Bundle().apply {
                putString("product_name", "Kamera Analog A")
                putInt("product_price", 1200000)
            }
            it.findNavController().navigate(R.id.action_home_to_product, b)
        }

        // Klik tile 2 → navigate ke detail
        productTile2.setOnClickListener {
            val b = Bundle().apply {
                putString("product_name", "Kamera Analog B")
                putInt("product_price", 900000)
            }
            it.findNavController().navigate(R.id.action_home_to_product, b)
        }

        // Klik tile 3 → navigate ke detail
        productTile3.setOnClickListener {
            val b = Bundle().apply {
                putString("product_name", "Kamera Analog C")
                putInt("product_price", 750000)
            }
            it.findNavController().navigate(R.id.action_home_to_product, b)
        }
    }
}
