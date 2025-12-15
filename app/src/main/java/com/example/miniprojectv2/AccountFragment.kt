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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import android.location.Geocoder
import java.util.Locale


class AccountFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }


    private val db = FirebaseFirestore.getInstance()
    private lateinit var prefs: android.content.SharedPreferences
    private val auth = FirebaseAuth.getInstance()

    private var userUID: String = ""
    private var isSeller: Boolean = false
    private var addressText: String = ""

    private lateinit var tvLocation: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

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
        addressText = prefs.getString(
            "active_location",
            "Lokasi tidak tersedia"
        ).toString()

        if (userUID.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada user aktif!", Toast.LENGTH_SHORT).show()
            return v
        }

        loadUserData(tvUsername, tvEmail)

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

        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Ya") { _, _ ->
                    prefs.edit().clear().apply()
                    auth.signOut()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        if (isSeller) {
            headerExpedition.visibility = View.VISIBLE
            btnAddExpedition.visibility = View.VISIBLE
            btnAddExpedition.setOnClickListener { showAddExpeditionDialog() }
        } else {
            headerExpedition.visibility = View.GONE
            btnAddExpedition.visibility = View.GONE
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        tvLocation = view.findViewById(R.id.tv_location)
        tvLocation.text = addressText

        val btnChangeLocation = view.findViewById<LinearLayout>(R.id.btn_change_location)
        btnChangeLocation.setOnClickListener {
            requestGpsAndTagLocation()
        }
    }



    private fun loadUserData(tvName: TextView, tvEmail: TextView) {
        db.collection("users")
            .document(userUID)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener
                tvName.text = doc.getString("nama") ?: ""
                tvEmail.text = doc.getString("email") ?: ""
            }
    }

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

                val firebaseUser = auth.currentUser

                //  Update email Firebase jika user Firebase
                if (firebaseUser != null && newEmail != firebaseUser.email) {
                    firebaseUser.updateEmail(newEmail)
                }

                db.collection("users")
                    .document(userUID)
                    .update(mapOf("nama" to newName, "email" to newEmail))
                    .addOnSuccessListener {
                        tvName.text = newName
                        tvEmail.text = newEmail
                        prefs.edit()
                            .putString("active_username", newName)
                            .putString("active_email", newEmail)
                            .apply()
                        Toast.makeText(requireContext(), "Profil diperbarui", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)

        val etOld = dialogView.findViewById<EditText>(R.id.et_old_password)
        val etNew = dialogView.findViewById<EditText>(R.id.et_new_password)

        AlertDialog.Builder(requireContext())
            .setTitle("Ubah Password")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val oldPass = etOld.text.toString()
                val newPass = etNew.text.toString()
                changePassword(oldPass, newPass)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun changePassword(oldPassword: String, newPassword: String) {

        val firebaseUser = auth.currentUser

        // USER FIREBASE
        if (firebaseUser != null && firebaseUser.email != null) {

            val credential = EmailAuthProvider
                .getCredential(firebaseUser.email!!, oldPassword)

            firebaseUser.reauthenticate(credential)
                .addOnSuccessListener {
                    firebaseUser.updatePassword(newPassword)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Password lama salah", Toast.LENGTH_SHORT).show()
                }

            return
        }
    }

    private fun showAddExpeditionDialog() {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Ekspedisi")
            .setView(input)
            .setPositiveButton("Tambah") { _, _ ->
                (activity as? SellerActivity)?.addDeliveryExpedition(input.text.toString())
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun requestGpsAndTagLocation() {

        // Update UI immediately
        tvLocation.text = "📡 Mengambil lokasi..."

        Snackbar.make(
            requireView(),
            "Mengambil lokasi GPS...",
            Snackbar.LENGTH_SHORT
        ).show()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude

                    addressText = getAddressFromLocation(lat, lon)

                    tvLocation.text = addressText
                    prefs.edit()
                        .putString("active_location", addressText)
                        .apply()



                } else {
                    tvLocation.text = "Lokasi tidak tersedia"
                }
            }
            .addOnFailureListener {
                tvLocation.text = "Gagal mendapatkan lokasi"
            }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            requestGpsAndTagLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Izin lokasi ditolak",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getAddressFromLocation(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]

                val city = address.locality ?: address.subAdminArea ?: ""
                val province = address.adminArea ?: ""
                val country = address.countryName ?: ""

                listOf(city, province, country)
                    .filter { it.isNotEmpty() }
                    .joinToString(", ")
            } else {
                "Alamat tidak ditemukan"
            }
        } catch (e: Exception) {
            "Gagal mendapatkan alamat"
        }
    }
}
