package com.example.miniprojectv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class AccountFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var prefs: android.content.SharedPreferences

    private var userUID: String = ""
    private var isSeller: Boolean = false

    private lateinit var tvIpStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_account, container, false)

        val tvUsername: TextView = v.findViewById(R.id.tv_username)
        val tvEmail: TextView = v.findViewById(R.id.tv_email)
        val btnLogout: LinearLayout = v.findViewById(R.id.btn_logout)
        val btnEdit: LinearLayout = v.findViewById(R.id.btn_edit)
        val btnAddExpedition: LinearLayout = v.findViewById(R.id.btn_add_expedition)
        val headerExpedition = v.findViewById<TextView>(R.id.header_expedition)
        val btnChangePass: LinearLayout = v.findViewById(R.id.btn_change_password)
        val btnMLModel = v.findViewById<LinearLayout>(R.id.btn_ml_model)


        prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        userUID = prefs.getString("active_uid", "") ?: ""
        isSeller = prefs.getBoolean("isSeller", false)

        if (userUID.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada user aktif!", Toast.LENGTH_SHORT).show()
            return v
        }

        //Load user dari fs
        loadUserData(tvUsername, tvEmail)

        // Edit profil
        btnEdit.setOnClickListener {
            showEditDialog(tvUsername, tvEmail)
        }

        btnChangePass.setOnClickListener {
            showChangePasswordDialog()
        }

        btnMLModel.setOnClickListener {
            try {
                // Navigasi ke MachineLearningFragment via NavController
                val navController = findNavController()
                navController.navigate(R.id.machineLearningFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal membuka Machine Learning", Toast.LENGTH_SHORT).show()
                Log.e("AccountFragment", "Navigate to ML failed", e)
            }
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
            headerExpedition.visibility = View.VISIBLE
            btnAddExpedition.visibility = View.VISIBLE

            btnAddExpedition.setOnClickListener {
                showAddExpeditionDialog()
            }
        } else {
            headerExpedition.visibility = View.GONE
            btnAddExpedition.visibility = View.GONE
        }


        return v
    }
    //load firestore
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

                // update drawer
                updateHeader(nama, email)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat profil: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateHeader(nama: String, email: String) {
        val navViewBuyer =
            requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.nav_view)
        val navViewSeller =
            requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.nav_view_seller)

        val navView = navViewBuyer ?: navViewSeller ?: return
        val headerView = navView.getHeaderView(0)
        val headerTitle = headerView.findViewById<TextView>(R.id.header_title)
        val headerSubtitle = headerView.findViewById<TextView>(R.id.header_subtitle)

        headerTitle.text = nama
        headerSubtitle.text = email
    }

    // edit acc
    private fun showEditDialog(tvName: TextView, tvEmail: TextView) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_account, null)
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

                db.collection("users").document(userUID).update(
                    mapOf("nama" to newName, "email" to newEmail)
                ).addOnSuccessListener {
                    tvName.text = newName
                    tvEmail.text = newEmail
                    updateHeader(newName, newEmail)
                    prefs.edit().putString("active_username", newName)
                        .putString("active_email", newEmail).apply()
                    Toast.makeText(requireContext(), "Profil diperbarui", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal update profil: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        val etIp = view.findViewById<EditText>(R.id.et_ip)
//        val btnSaveIp = view.findViewById<Button>(R.id.btn_save_ip)

        // Initialize EditText with current IP
//        etIp.setText(IpHelper.getBaseUrl())

//        btnSaveIp.setOnClickListener {
//            val newIp = etIp.text.toString().trim()
//            if (newIp.isEmpty()) {
//                Toast.makeText(requireContext(), "IP tidak boleh kosong", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            // Update in-memory URL
//            IpHelper.changeBaseUrl(newIp)
//            Toast.makeText(requireContext(), "Server IP diperbarui ke $newIp", Toast.LENGTH_SHORT).show()
//            Log.d("AccountFragment", "Server IP changed to: $newIp")
//        }
    }


    // ubah pw
    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)

        val etOld = dialogView.findViewById<EditText>(R.id.et_old_password)
        val etNew = dialogView.findViewById<EditText>(R.id.et_new_password)

        AlertDialog.Builder(requireContext())
            .setTitle("Ubah Password")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val oldPass = etOld.text.toString().trim()
                val newPass = etNew.text.toString().trim()

                if (oldPass.isEmpty() || newPass.isEmpty()) {
                    Toast.makeText(requireContext(), "Isi semua field!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPass.length < 4) {
                    Toast.makeText(requireContext(), "Password baru minimal 4 karakter!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                changePassword(oldPass, newPass)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun changePassword(oldPassword: String, newPassword: String) {

        db.collection("users")
            .document(userUID)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    Toast.makeText(requireContext(), "User tidak ditemukan!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val currentHashed = document.getString("password") ?: ""

                // Verifikasi password lama
                if (!PasswordBcrypt.verifyPassword(oldPassword, currentHashed)) {
                    Toast.makeText(requireContext(), "Password lama salah!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Hash password baru bcrypt
                val newHashed = PasswordBcrypt.hashPassword(newPassword)

                db.collection("users")
                    .document(userUID)
                    .update("password", newHashed)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Password berhasil diubah!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Gagal mengubah password!", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Terjadi kesalahan!", Toast.LENGTH_SHORT).show()
            }
    }


}