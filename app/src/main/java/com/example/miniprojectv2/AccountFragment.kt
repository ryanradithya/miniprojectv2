package com.example.miniprojectv2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
    private val auth = FirebaseAuth.getInstance()
    private lateinit var prefs: SharedPreferences

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
        val btnChangePass: LinearLayout = v.findViewById(R.id.btn_change_password)
        val btnAddExpedition: LinearLayout = v.findViewById(R.id.btn_add_expedition)
        val btnMLModel: LinearLayout = v.findViewById(R.id.btn_ml_model)
        val headerExpedition: TextView = v.findViewById(R.id.header_expedition)

        val headerReport: TextView = v.findViewById(R.id.header_report)
        val btnReport: LinearLayout = v.findViewById(R.id.btn_report)


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
                findNavController().navigate(R.id.machineLearningFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal membuka Machine Learning", Toast.LENGTH_SHORT).show()
                Log.e("AccountFragment", "Navigation error", e)
            }
        }

        btnLogout.setOnClickListener {

            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_logout_confirm, null)

            val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
            val btnLogoutConfirm = dialogView.findViewById<Button>(R.id.btn_logout)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnLogoutConfirm.setOnClickListener {
                prefs.edit().clear().apply()
                auth.signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
                dialog.dismiss()
            }

            dialog.show()
        }


        //fun seller
        if (isSeller) {
            headerExpedition.visibility = View.VISIBLE
            btnAddExpedition.visibility = View.VISIBLE
            btnAddExpedition.setOnClickListener { showAddExpeditionDialog() }

            headerReport.visibility = View.VISIBLE
            btnReport.visibility = View.VISIBLE
            btnReport.setOnClickListener {
                try {
                    findNavController().navigate(R.id.sellerReportFragment)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Gagal membuka laporan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            headerExpedition.visibility = View.GONE
            btnAddExpedition.visibility = View.GONE

            headerReport.visibility = View.GONE
            btnReport.visibility = View.GONE
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


    //firebase userdata
    private fun loadUserData(tvName: TextView, tvEmail: TextView) {
        db.collection("users")
            .document(userUID)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                tvName.text = doc.getString("nama") ?: ""
                tvEmail.text = doc.getString("email") ?: ""

                val dob = doc.getString("dob")
                val region = doc.getString("region")
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat profil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditDialog(tvName: TextView, tvEmail: TextView) {

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_account, null)

        val etName = dialogView.findViewById<EditText>(R.id.et_edit_name)
        val etEmail = dialogView.findViewById<EditText>(R.id.et_edit_email)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)

        etName.setText(tvName.text)
        etEmail.setText(tvEmail.text)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Nama dan email tidak boleh kosong",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            updateProfile(newName, newEmail, tvName, tvEmail)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showChangePasswordDialog() {

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)

        val etOld = dialogView.findViewById<EditText>(R.id.et_old_password)
        val etNew = dialogView.findViewById<EditText>(R.id.et_new_password)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val oldPass = etOld.text.toString().trim()
            val newPass = etNew.text.toString().trim()

            if (oldPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Password tidak boleh kosong",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "Password minimal 6 karakter",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            changePassword(oldPass, newPass)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun changePassword(oldPassword: String, newPassword: String) {

        val firebaseUser = auth.currentUser ?: return

        val credential = EmailAuthProvider
            .getCredential(firebaseUser.email!!, oldPassword)

        firebaseUser.reauthenticate(credential)
            .addOnSuccessListener {
                firebaseUser.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Password berhasil diubah",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Password lama salah",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showAddExpeditionDialog() {

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_expedition, null)

        val etExpedition = dialogView.findViewById<EditText>(R.id.et_expedition_name)
        val btnAdd = dialogView.findViewById<Button>(R.id.btn_add)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnAdd.setOnClickListener {
            val expeditionName = etExpedition.text.toString().trim()

            if (expeditionName.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Nama ekspedisi tidak boleh kosong",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            saveExpeditionToFirestore(
                expeditionName = expeditionName,
                onSuccess = {
                    Toast.makeText(
                        requireContext(),
                        "Ekspedisi berhasil ditambahkan",
                        Toast.LENGTH_SHORT
                    ).show()

                    (activity as? SellerActivity)
                        ?.addDeliveryExpedition(expeditionName)

                    dialog.dismiss()
                },
                onError = {
                    Toast.makeText(
                        requireContext(),
                        "Gagal menyimpan ekspedisi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        dialog.show()
    }


    //update profile
    private fun updateProfile(
        newName: String,
        newEmail: String,
        tvName: TextView,
        tvEmail: TextView
    ) {
        val firebaseUser = auth.currentUser

        if (firebaseUser != null && firebaseUser.email != newEmail) {
            firebaseUser.updateEmail(newEmail)
                .addOnSuccessListener {
                    updateFirestoreProfile(newName, newEmail, tvName, tvEmail)
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Silakan login ulang untuk mengganti email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            updateFirestoreProfile(newName, newEmail, tvName, tvEmail)
        }
    }

    //fun firestore
    private fun updateFirestoreProfile(
        newName: String,
        newEmail: String,
        tvName: TextView,
        tvEmail: TextView
    ) {
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
                prefs.edit()
                    .putString("active_username", newName)
                    .putString("active_email", newEmail)
                    .apply()
                Toast.makeText(requireContext(), "Profil diperbarui", Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestGpsAndTagLocation() {

        tvLocation.text = "Mengambil lokasi..."

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

    //get locaiton
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

    //save expedition ke firestore
    private fun saveExpeditionToFirestore(
        expeditionName: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {

        val db = FirebaseFirestore.getInstance()

        val data = hashMapOf(
            "name" to expeditionName,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("expeditions")
            .add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun saveExpedition(expeditionName: String) {

        val expedition = Expedition(
            name = expeditionName,
            createdAt = System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("expeditions")
            .add(expedition)
    }

}
