package com.example.miniprojectv2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // masuk ke menu utama setelah beberapa saat
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
