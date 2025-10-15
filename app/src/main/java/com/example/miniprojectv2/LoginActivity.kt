package com.example.miniprojectv2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameInput = findViewById<EditText>(R.id.username_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // hardcoded auth
            when {
                username == "Ryan" && password == "Ryan123" -> {
                    Toast.makeText(this, "Login sukses! Selamat datang, Ryan", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                username == "Penjual" && password == "Sell123" -> {
                    Toast.makeText(this, "Login Penjual berhasil!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SellerActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                else -> {
                    Toast.makeText(this, "Username atau password salah!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
