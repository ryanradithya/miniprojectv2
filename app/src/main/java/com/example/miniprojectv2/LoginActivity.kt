package com.example.miniprojectv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback


class LoginActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        try {
            IpHelper.autoDetectServer(this) { success ->
                runOnUiThread {
                    Log.d("LoginActivity", "Auto-detect result: $success, url=${IpHelper.getBaseUrl()}")
                }
            }
        } catch (e: Exception) {
            Log.d("LoginActivity", "Auto-detect failed")
        }

        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_custom_status_bar)

        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val usernameInput = findViewById<EditText>(R.id.username_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.login_button)
        val signupText = findViewById<TextView>(R.id.textView5)
        val forgotText = findViewById<TextView>(R.id.textView3)

        loginButton.setOnClickListener {
            val userInput = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (userInput.isEmpty() || password.isEmpty()) {
                toast("Harap isi username/email dan password!")
                return@setOnClickListener
            }

            loginUser(userInput, password)
        }

        signupText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotText.setOnClickListener {
            openForgotPasswordFragment()
        }

        //  ==============================
        //  LISTENER SAAT FRAGMENT HILANG
        //  ==============================
        supportFragmentManager.addOnBackStackChangedListener {
            val isFragmentVisible = supportFragmentManager.backStackEntryCount > 0

            val bottomHalf = findViewById<View>(R.id.bottom_half)
            val container = findViewById<FrameLayout>(R.id.login_fragment_container)

            if (!isFragmentVisible) {

                val bottomHalf = findViewById<View>(R.id.bottom_half)
                val container = findViewById<FrameLayout>(R.id.login_fragment_container)

                // Fade out fragment container
                container.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
                container.visibility = View.GONE

                // Fade in login layout
                bottomHalf.visibility = View.VISIBLE
                bottomHalf.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
            }
        }

        //  ==============================
        //  HANDLE TOMBOL BACK
        //  ==============================
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        })
    }

    fun restoreLoginLayout() {
        findViewById<View>(R.id.bottom_half).visibility = View.VISIBLE
        findViewById<FrameLayout>(R.id.login_fragment_container).visibility = View.GONE
    }
    private fun openForgotPasswordFragment() {

        val bottomHalf = findViewById<View>(R.id.bottom_half)
        val container = findViewById<FrameLayout>(R.id.login_fragment_container)

// Fade Out card login
        bottomHalf.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
        bottomHalf.visibility = View.INVISIBLE

// Tampilkan container + animasi fade in
        container.visibility = View.VISIBLE
        container.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))

        supportFragmentManager.beginTransaction()
            .replace(R.id.login_fragment_container, ForgotPasswordFragment())
            .addToBackStack("forgot_password")
            .commit()
    }




    // ================= LOGIN KE FIRESTORE =================
    private fun loginUser(userInput: String, password: String) {

        // Deteksi input sebagai email atau username
        val isEmail = Patterns.EMAIL_ADDRESS.matcher(userInput).matches()
        val field = if (isEmail) "email" else "nama"

        db.collection("users")
            .whereEqualTo(field, userInput)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    toast("Akun tidak ditemukan.")
                    return@addOnSuccessListener
                }

                val doc = result.documents.first()
                val data = doc.data ?: run {
                    toast("Data akun tidak valid.")
                    return@addOnSuccessListener
                }

                val savedPassword = data["password"] as? String ?: ""
                val role = data["role"] as? String ?: "buyer"
                val nama = data["nama"] as? String ?: ""
                val email = data["email"] as? String ?: ""
                val uid = doc.id     // sama dengan field "id" yang kita set di Register

                val isValid = PasswordBcrypt.verifyPassword(password, savedPassword)

                if (!isValid) {
                    toast("Password salah!")
                    return@addOnSuccessListener
                }


                // Simpan session
                prefs.edit()
                    .putString("active_username", nama)
                    .putString("active_email", email)
                    .putString("active_uid", uid)
                    .putBoolean("isSeller", role == "seller")
                    .apply()

                if (role == "seller") {
                    toast("Login sukses sebagai Seller!")
                    startActivity(Intent(this, SellerActivity::class.java))
                } else {
                    toast("Login sukses sebagai Buyer!")
                    startActivity(Intent(this, MainActivity::class.java))
                }

                finish()
            }
            .addOnFailureListener {
                toast("Gagal terhubung ke database: ${it.message}")
            }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}