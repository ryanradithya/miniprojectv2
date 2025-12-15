package com.example.miniprojectv2

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient

    private val db = FirebaseFirestore.getInstance()
    private val RC_GOOGLE_SIGN_UP = 9002

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

        // ===== Google Sign-In Config =====
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(this, gso)

        val loginText = findViewById<TextView>(R.id.textView3)
        val roleSpinner = findViewById<Spinner>(R.id.role_spinner)
        val usernameInput = findViewById<EditText>(R.id.reg_username_input)
        val emailInput = findViewById<EditText>(R.id.reg_email_input)
        val passwordInput = findViewById<EditText>(R.id.reg_password_input)
        val saveButton = findViewById<Button>(R.id.save_button)
        val googleButton = findViewById<Button>(R.id.btn_google_signup)

        // ===== Kembali ke Login =====
        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // ===== REGISTER EMAIL =====
        saveButton.setOnClickListener {

            val roleDisplay = roleSpinner.selectedItem.toString()
            val nama = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (!validateInputs(nama, email, password)) return@setOnClickListener

            val roleKey = if (roleDisplay == "User") "buyer" else "seller"

            registerWithEmail(nama, email, password, roleKey)
        }

        // ===== REGISTER GOOGLE =====
        googleButton.setOnClickListener {
            googleClient.signOut() // hindari akun cached
            startActivityForResult(
                googleClient.signInIntent,
                RC_GOOGLE_SIGN_UP
            )
        }
    }

    // ================= GOOGLE RESULT =================
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_UP) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                Toast.makeText(this, "Google Sign-Up dibatalkan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                auth.currentUser?.let { handleGoogleUser(it) }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Google Sign-Up gagal", Toast.LENGTH_SHORT).show()
            }
    }

    // ================= GOOGLE AUTO REGISTER =================
    private fun handleGoogleUser(user: com.google.firebase.auth.FirebaseUser) {

        val uid = user.uid
        val email = user.email ?: ""
        val name = user.displayName ?: "User"

        val userRef = db.collection("users").document(uid)

        userRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {

                val userData = hashMapOf(
                    "nama" to name,
                    "email" to email,
                    "role" to "buyer", // default
                    "authProvider" to "google",
                    "createdAt" to System.currentTimeMillis()
                )

                userRef.set(userData)
            }

            Toast.makeText(this, "Registrasi Google berhasil!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // ================= EMAIL REGISTER =================
    private fun registerWithEmail(
        nama: String,
        email: String,
        password: String,
        roleKey: String
    ) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid

                val userData = hashMapOf(
                    "nama" to nama,
                    "email" to email,
                    "role" to roleKey,
                    "authProvider" to "email",
                    "createdAt" to System.currentTimeMillis()
                )

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
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    // ================= VALIDASI =================
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

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
