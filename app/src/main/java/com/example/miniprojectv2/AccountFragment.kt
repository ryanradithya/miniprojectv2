package com.example.miniprojectv2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class AccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_account, container, false)

        val tvUsername: TextView = v.findViewById(R.id.tv_username)
        val tvEmail: TextView = v.findViewById(R.id.tv_email)
        val btnLogout: Button = v.findViewById(R.id.btn_logout)
        val btnEdit: Button = v.findViewById(R.id.btn_edit)

        // Ambil header drawer (kalau ada)
        val navView =
            requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(
                R.id.nav_view
            )
        val headerView = navView.getHeaderView(0)
        val headerTitle = headerView.findViewById<TextView>(R.id.header_title)
        val headerSubtitle = headerView.findViewById<TextView>(R.id.header_subtitle)

        // Data dummy akun awal
        tvUsername.text = "John Doe"
        tvEmail.text = "johndoe@example.com"

        // Tombol Edit
        btnEdit.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_account, null)

            val etName = dialogView.findViewById<EditText>(R.id.et_edit_name)
            val etEmail = dialogView.findViewById<EditText>(R.id.et_edit_email)

            etName.setText(tvUsername.text)
            etEmail.setText(tvEmail.text)

            AlertDialog.Builder(requireContext())
                .setTitle("Edit Account")
                .setView(dialogView)
                .setPositiveButton("Simpan") { _, _ ->
                    val newName = etName.text.toString()
                    val newEmail = etEmail.text.toString()

                    if (newName.isNotBlank() && newEmail.isNotBlank()) {
                        tvUsername.text = newName
                        tvEmail.text = newEmail
                        headerTitle.text = newName
                        headerSubtitle.text = newEmail
                        Toast.makeText(
                            requireContext(),
                            "Akun berhasil diperbarui",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Nama & Email tidak boleh kosong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        // Tombol Logout
        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Ya") { _, _ ->
                    Toast.makeText(requireContext(), "Logout berhasil!", Toast.LENGTH_SHORT).show()

                    // Arahkan ke LoginActivity
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        return v
    }
}
