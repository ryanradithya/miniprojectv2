package com.example.miniprojectv2

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val roleSpinner = findViewById<Spinner>(R.id.role_spinner)
        val usernameInput = findViewById<EditText>(R.id.reg_username_input)
        val passwordInput = findViewById<EditText>(R.id.reg_password_input)
        val saveButton = findViewById<Button>(R.id.save_button)

        saveButton.setOnClickListener {
            val role = roleSpinner.selectedItem.toString()
            val newUsername = usernameInput.text.toString().trim()
            val newPassword = passwordInput.text.toString().trim()

            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val editor = prefs.edit()

            if (role == "User") {
                editor.putString("user_username", newUsername)
                editor.putString("user_password", newPassword)
            } else {
                editor.putString("seller_username", newUsername)
                editor.putString("seller_password", newPassword)
            }

            editor.apply()

            Toast.makeText(this, "$role credentials updated!", Toast.LENGTH_SHORT).show()
            finish() // return to login
        }
    }
}
