package com.example.miniprojectv2

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Status bar
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_custom_status_bar)

        val loginText = findViewById<TextView>(R.id.textView3)
        val roleSpinner = findViewById<Spinner>(R.id.role_spinner)
        val usernameInput = findViewById<EditText>(R.id.reg_username_input)
        val emailInput = findViewById<EditText>(R.id.reg_email_input)
        val passwordInput = findViewById<EditText>(R.id.reg_password_input)
        val saveButton = findViewById<Button>(R.id.save_button)

        // Pindah ke login
        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        saveButton.setOnClickListener {
            val roleDisplay = roleSpinner.selectedItem.toString()   // "User" / "Seller"
            val nama = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Validasi dasar
            if (!validateInputs(nama, email, password)) return@setOnClickListener

            // Role untuk Firestore
            val roleKey = if (roleDisplay == "User") "buyer" else "seller"

            // Cek email sudah dipakai atau belum
            checkEmailExists(email) { exists ->
                if (exists) {
                    showToast("Email sudah digunakan!")
                    return@checkEmailExists
                }

                // Generate ID custom: b1, b2, s1, s2, ...
                generateCustomUserId(roleKey) { customId ->
                    if (customId == null) {
                        showToast("Gagal membuat ID user")
                        return@generateCustomUserId
                    }

                    val userData = hashMapOf(
                        "id" to customId,
                        "nama" to nama,
                        "email" to email,
                        "password" to password,
                        "role" to roleKey
                    )

                    db.collection("users")
                        .document(customId)   // docId = "b1"/"s1"
                        .set(userData)
                        .addOnSuccessListener {
                            showToast("Registrasi berhasil!")
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            showToast("Gagal menyimpan ke database: ${it.message}")
                        }
                }
            }
        }
    }

    // =================== VALIDASI INPUT ===================
    private fun validateInputs(nama: String, email: String, password: String): Boolean {
        if (nama.isEmpty()) {
            showToast("Nama tidak boleh kosong")
            return false
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email tidak valid")
            return false
        }
        if (password.length < 4) {
            showToast("Password minimal 4 karakter")
            return false
        }
        return true
    }

    // ============ CEK EMAIL SUDAH ADA ATAU BELUM ============
    private fun checkEmailExists(email: String, callback: (Boolean) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result ->
                callback(!result.isEmpty)
            }
            .addOnFailureListener {
                showToast("Gagal cek email: ${it.message}")
                callback(false)
            }
    }

    // ============ GENERATE ID: b1, b2, s1, s2, ... ============
    private fun generateCustomUserId(roleKey: String, callback: (String?) -> Unit) {
        db.collection("users")
            .whereEqualTo("role", roleKey)
            .get()
            .addOnSuccessListener { result ->
                val count = result.size() + 1
                val prefix = if (roleKey == "buyer") "b" else "s"
                callback("$prefix$count")
            }
            .addOnFailureListener {
                showToast("Gagal menghitung user: ${it.message}")
                callback(null)
            }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}