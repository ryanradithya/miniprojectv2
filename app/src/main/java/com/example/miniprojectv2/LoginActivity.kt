package com.example.miniprojectv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameInput = findViewById<EditText>(R.id.username_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.login_button)
        val signupText = findViewById<TextView>(R.id.textView5)
        val forgotText = findViewById<TextView>(R.id.textView3)

        // ✅ Use same name as AccountFragment
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Load saved/default credentials
        val userUsername = prefs.getString("user_username", "Ryan")
        val userPassword = prefs.getString("user_password", "Ryan123")
        val userEmail = prefs.getString("user_email", "ryan@example.com")

        val sellerUsername = prefs.getString("seller_username", "Penjual")
        val sellerPassword = prefs.getString("seller_password", "Sell123")
        val sellerEmail = prefs.getString("seller_email", "penjual@example.com")

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            when {
                // ✅ User Login
                username == userUsername && password == userPassword -> {
                    Toast.makeText(this, "Login sukses sebagai User!", Toast.LENGTH_SHORT).show()

                    // Save session info
                    prefs.edit()
                        .putBoolean("isSeller", false)
                        .putString("active_username", userUsername)
                        .putString("active_email", userEmail)
                        .apply()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                // ✅ Seller Login
                username == sellerUsername && password == sellerPassword -> {
                    Toast.makeText(this, "Login sukses sebagai Seller!", Toast.LENGTH_SHORT).show()

                    // Save session info
                    prefs.edit()
                        .putBoolean("isSeller", true)
                        .putString("active_username", sellerUsername)
                        .putString("active_email", sellerEmail)
                        .apply()

                    val intent = Intent(this, SellerActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                else -> {
                    Toast.makeText(this, "Username atau password salah!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Go to Register
        val goToRegister = Intent(this, RegisterActivity::class.java)
        signupText.setOnClickListener { startActivity(goToRegister) }
        forgotText.setOnClickListener { startActivity(goToRegister) }
    }
}
