package com.example.miniprojectv2

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // ===== Status bar =====
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor =
            ContextCompat.getColor(this, R.color.my_custom_status_bar)

        val loginText = findViewById<TextView>(R.id.textView3)
        val roleSpinner = findViewById<Spinner>(R.id.role_spinner)
        val usernameInput = findViewById<EditText>(R.id.reg_username_input)
        val emailInput = findViewById<EditText>(R.id.reg_email_input)
        val passwordInput = findViewById<EditText>(R.id.reg_password_input)
        val saveButton = findViewById<Button>(R.id.save_button)

        // ===== Kembali ke Login =====
        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // ===== REGISTER =====
        saveButton.setOnClickListener {

            val roleDisplay = roleSpinner.selectedItem.toString()
            val nama = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (!validateInputs(nama, email, password)) return@setOnClickListener

            val roleKey = if (roleDisplay == "User") "buyer" else "seller"

            registerWithFirebase(nama, email, password, roleKey)
        }
    }

    /**
     * ================= REGISTER FIREBASE AUTH =================
     */
    private fun registerWithFirebase(
        nama: String,
        email: String,
        password: String,
        roleKey: String
    ) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val uid = result.user!!.uid

                // (OPSIONAL) legacy custom id: b1 / s1
                generateCustomUserId(roleKey) { customId ->

                    val userData = hashMapOf(
                        "id" to customId,              // legacy ID (FIELD SAJA)
                        "nama" to nama,
                        "email" to email,
                        "role" to roleKey,
                        "authProvider" to "email",
                        "createdAt" to System.currentTimeMillis()
                    )

                    // Firestore docId = UID Firebase
                    db.collection("users")
                        .document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Registrasi berhasil! Silakan login.",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Gagal menyimpan profil: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Registrasi gagal: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * ================= VALIDASI =================
     */
    private fun validateInputs(nama: String, email: String, password: String): Boolean {
        if (nama.isEmpty()) {
            showToast("Nama tidak boleh kosong")
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email tidak valid")
            return false
        }
        if (password.length < 6) {
            showToast("Password minimal 6 karakter")
            return false
        }
        return true
    }

    /**
     * ================= LEGACY CUSTOM ID (OPSIONAL) =================
     * Hanya dipakai sebagai FIELD, bukan autentikasi
     */
    private fun generateCustomUserId(roleKey: String, callback: (String) -> Unit) {
        db.collection("users")
            .whereEqualTo("role", roleKey)
            .get()
            .addOnSuccessListener { result ->
                val prefix = if (roleKey == "buyer") "b" else "s"
                val count = result.size() + 1
                callback("$prefix$count")
            }
            .addOnFailureListener {
                callback("") // tidak fatal
            }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
