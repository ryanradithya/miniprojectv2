package com.example.miniprojectv2

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

class MachineLearningFragment : Fragment(R.layout.fragment_machine_learning) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // =============================
        // TEMPORARY PLACEHOLDER LOGIC
        // =============================
        // Ini cuma penanda bahwa fragment berhasil dibuka
        Toast.makeText(
            requireContext(),
            "Machine Learning Settings dibuka",
            Toast.LENGTH_SHORT
        ).show()

        // Nanti di sini kamu akan:
        // - init switch ML
        // - load saved config
        // - setup spinner / radio button model
        // - connect ke ViewModel / DataStore
    }
}
