package com.example.miniprojectv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class AccountFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var prefs: android.content.SharedPreferences

    private var userUID: String = ""
    private var isSeller: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_account, container, false)

        val tvUsername: TextView = v.findViewById(R.id.tv_username)
        val tvEmail: TextView = v.findViewById(R.id.tv_email)
        val btnLogout: Button = v.findViewById(R.id.btn_logout)
        val btnEdit: Button = v.findViewById(R.id.btn_edit)
        val btnAddExpedition: Button = v.findViewById(R.id.btn_add_expedition)

        prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        userUID = prefs.getString("active_uid", "") ?: ""
        isSeller = prefs.getBoolean("isSeller", false)

        if (userUID.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada user aktif!", Toast.LENGTH_SHORT).show()
            return v
        }

        // Load user dari Firestore
        loadUserData(tvUsername, tvEmail)

        // Edit profil
        btnEdit.setOnClickListener {
            showEditDialog(tvUsername, tvEmail)
        }

        // Logout
        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Ya") { _, _ ->
                    prefs.edit().clear().apply()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        // Tombol tambah ekspedisi khusus seller
        if (isSeller) {
            btnAddExpedition.visibility = View.VISIBLE
            btnAddExpedition.setOnClickListener {
                showAddExpeditionDialog()
            }
        } else {
            btnAddExpedition.visibility = View.GONE
        }

        return v
    }

    // ============== LOAD DATA USER DARI FIRESTORE ==============
    private fun loadUserData(tvName: TextView, tvEmail: TextView) {
        db.collection("users")
            .document(userUID)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(requireContext(), "User tidak ditemukan!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val nama = doc.getString("nama") ?: ""
                val email = doc.getString("email") ?: ""

                tvName.text = nama
                tvEmail.text = email

                // update header drawer jika ada
                updateHeader(nama, email)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat profil: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ============== UPDATE HEADER NAV DRAWER ==============
    private fun updateHeader(nama: String, email: String) {
        val navViewBuyer =
            requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(
                R.id.nav_view
            )
        val navViewSeller =
            requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(
                R.id.nav_view_seller
            )

        val navView = navViewBuyer ?: navViewSeller ?: return

        val headerView = navView.getHeaderView(0)
        val headerTitle = headerView.findViewById<TextView>(R.id.header_title)
        val headerSubtitle = headerView.findViewById<TextView>(R.id.header_subtitle)

        headerTitle.text = nama
        headerSubtitle.text = email
    }

    // ============== DIALOG EDIT NAMA + EMAIL ==============
    private fun showEditDialog(tvName: TextView, tvEmail: TextView) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_account, null)

        val etName = dialogView.findViewById<EditText>(R.id.et_edit_name)
        val etEmail = dialogView.findViewById<EditText>(R.id.et_edit_email)

        etName.setText(tvName.text)
        etEmail.setText(tvEmail.text)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Akun")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val newName = etName.text.toString().trim()
                val newEmail = etEmail.text.toString().trim()

                if (newName.isEmpty() || newEmail.isEmpty()) {
                    Toast.makeText(requireContext(), "Nama & email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                db.collection("users")
                    .document(userUID)
                    .update(
                        mapOf(
                            "nama" to newName,
                            "email" to newEmail
                        )
                    )
                    .addOnSuccessListener {
                        tvName.text = newName
                        tvEmail.text = newEmail
                        updateHeader(newName, newEmail)

                        // sync dengan SharedPreferences
                        prefs.edit()
                            .putString("active_username", newName)
                            .putString("active_email", newEmail)
                            .apply()

                        Toast.makeText(requireContext(), "Profil diperbarui", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Gagal update profil: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ============== DIALOG TAMBAH EKSPEDISI (SELLER) ==============
    private fun showAddExpeditionDialog() {
        val input = EditText(requireContext())
        input.hint = "Nama ekspedisi baru"

        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Ekspedisi")
            .setView(input)
            .setPositiveButton("Tambah") { _, _ ->
                val newExpedition = input.text.toString().trim()
                if (newExpedition.isEmpty()) {
                    Toast.makeText(requireContext(), "Nama ekspedisi tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else {
                    (activity as? SellerActivity)?.addDeliveryExpedition(newExpedition)
                    Toast.makeText(requireContext(), "Ekspedisi '$newExpedition' ditambahkan", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}