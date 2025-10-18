package com.example.miniprojectv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LoginActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences

    // ✅ Keep these as class-level variables
    private var userUsername: String? = null
    private var userPassword: String? = null
    private var userEmail: String? = null

    private var sellerUsername: String? = null
    private var sellerPassword: String? = null
    private var sellerEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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

        // ✅ Load credentials once here
        loadCredentials()

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            when {
                // ✅ User login
                username == userUsername && password == userPassword -> {
                    prefs.edit()
                        .putBoolean("isSeller", false)
                        .putString("active_username", userUsername)
                        .putString("active_email", userEmail)
                        .apply()

                    Toast.makeText(this, "Login sukses sebagai User!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

                // ✅ Seller login
                username == sellerUsername && password == sellerPassword -> {
                    prefs.edit()
                        .putBoolean("isSeller", true)
                        .putString("active_username", sellerUsername)
                        .putString("active_email", sellerEmail)
                        .apply()

                    Toast.makeText(this, "Login sukses sebagai Seller!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SellerActivity::class.java))
                    finish()
                }

                else -> {
                    Toast.makeText(this, "Username atau password salah!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        signupText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // ✅ Whenever the LoginActivity returns to foreground, reload prefs
    override fun onResume() {
        super.onResume()
        loadCredentials()
    }

    private fun loadCredentials() {
        userUsername = prefs.getString("user_username", "Ryan")
        userPassword = prefs.getString("user_password", "Ryan123")
        userEmail = prefs.getString("user_email", "ryan@example.com")

        sellerUsername = prefs.getString("seller_username", "Penjual")
        sellerPassword = prefs.getString("seller_password", "Sell123")
        sellerEmail = prefs.getString("seller_email", "penjual@example.com")

        Log.d("LoginActivity", "Loaded User: $userUsername / $userPassword")
        Log.d("LoginActivity", "Loaded Seller: $sellerUsername / $sellerPassword")
    }
}
