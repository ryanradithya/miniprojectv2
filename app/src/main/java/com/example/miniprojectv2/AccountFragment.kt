package com.example.miniprojectv2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.miniprojectv2.SellerActivity

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
        val btnAddExpedition: Button = v.findViewById(R.id.btn_add_expedition) // new button

        // Coba cari nav_view (pembeli) atau nav_view_seller (penjual)
        val navView = requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(
            R.id.nav_view
        ) ?: requireActivity().findViewById(R.id.nav_view_seller)

        // Siapkan variabel untuk header (null-safe)
        var headerTitle: TextView? = null
        var headerSubtitle: TextView? = null
        navView?.let {
            val headerView = it.getHeaderView(0)
            headerTitle = headerView.findViewById(R.id.header_title)
            headerSubtitle = headerView.findViewById(R.id.header_subtitle)
        }

        // Ambil SharedPreferences
        val prefs = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)

        // Cek apakah login sebagai seller
        val isSeller = prefs.getBoolean("isSeller", false)

        // Ambil data sesuai tipe akun
        var username: String
        var email: String

        Log.d("AccountFragment", "isSeller: $isSeller")

        if (isSeller) {
            username = prefs.getString("seller_username", "Penjual") ?: "Penjual"
            email = prefs.getString("seller_email", "penjual@example.com") ?: "penjual@example.com"
        } else {
            username = prefs.getString("user_username", "Ryan") ?: "Ryan"
            email = prefs.getString("user_email", "ryan@example.com") ?: "ryan@example.com"
        }

        // Tampilkan di UI
        tvUsername.text = username
        tvEmail.text = email

        // Update header drawer bila ada
        headerTitle?.text = username
        headerSubtitle?.text = email

        // Tombol Edit
        btnEdit.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_account, null)

            val etName = dialogView.findViewById<EditText>(R.id.et_edit_name)
            val etEmail = dialogView.findViewById<EditText>(R.id.et_edit_email)

            etName.setText(tvUsername.text)
            etEmail.setText(tvEmail.text)

            AlertDialog.Builder(requireContext())
                .setTitle("Edit Akun")
                .setView(dialogView)
                .setPositiveButton("Simpan") { _, _ ->
                    val newName = etName.text.toString()
                    val newEmail = etEmail.text.toString()

                    if (newName.isNotBlank() && newEmail.isNotBlank()) {
                        // Update tampilan
                        tvUsername.text = newName
                        tvEmail.text = newEmail
                        headerTitle?.text = newName
                        headerSubtitle?.text = newEmail

                        // Simpan ke SharedPreferences
                        val editor = prefs.edit()
                        if (isSeller) {
                            editor.putString("seller_username", newName)
                            editor.putString("seller_email", newEmail)
                        } else {
                            editor.putString("user_username", newName)
                            editor.putString("user_email", newEmail)
                        }
                        editor.apply()

                        Toast.makeText(requireContext(), "Akun berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Nama & Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
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

                    // Reset status login
                    prefs.edit().remove("isSeller").apply()

                    // Arahkan ke LoginActivity
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        // ==========================
        // Button Tambah Expedisi (Seller Only)
        // ==========================
        if (isSeller) {
            btnAddExpedition.visibility = View.VISIBLE
            btnAddExpedition.setOnClickListener {
                val input = EditText(requireContext())
                input.hint = "Nama Expedisi Baru"

                AlertDialog.Builder(requireContext())
                    .setTitle("Tambah Expedisi")
                    .setView(input)
                    .setPositiveButton("Tambah") { _, _ ->
                        val newExpedition = input.text.toString().trim()
                        if (newExpedition.isNotEmpty()) {
                            (activity as? SellerActivity)?.addDeliveryExpedition(newExpedition)
                            Toast.makeText(requireContext(), "Expedisi '$newExpedition' berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Nama expedisi tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        } else {
            btnAddExpedition.visibility = View.GONE
        }

        return v
    }
}
