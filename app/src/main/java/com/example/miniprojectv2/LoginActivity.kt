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
        val forgotText = findViewById<TextView>(R.id.textView3) // ✅ new line

        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Load saved or default creds
        val userUsername = prefs.getString("user_username", "Ryan")
        val userPassword = prefs.getString("user_password", "Ryan123")

        val sellerUsername = prefs.getString("seller_username", "Penjual")
        val sellerPassword = prefs.getString("seller_password", "Sell123")

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            when {
                username == userUsername && password == userPassword -> {
                    Toast.makeText(this, "Login sukses sebagai User!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("isSeller", false)
                    startActivity(intent)
                    finish()
                }

                username == sellerUsername && password == sellerPassword -> {
                    Toast.makeText(this, "Login sukses sebagai Seller!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SellerActivity::class.java)
                    intent.putExtra("isSeller", true)
                    startActivity(intent)
                    finish()
                }

                else -> {
                    Toast.makeText(this, "Username atau password salah!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ✅ Both "Sign up" and "Forgot password?" go to RegisterActivity
        val goToRegister = Intent(this, RegisterActivity::class.java)
        signupText.setOnClickListener { startActivity(goToRegister) }
        forgotText.setOnClickListener { startActivity(goToRegister) }
    }
}
