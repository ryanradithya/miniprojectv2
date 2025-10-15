package com.example.miniprojectv2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class SellerActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller)

        // Ambil NavController dari NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_seller) as NavHostFragment
        navController = navHostFragment.navController

        // Ambil referensi BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_seller)

        // BottomNavigationView listener
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_list_produk -> {
                    navController.navigate(R.id.beliFragment)
                    true
                }
                R.id.nav_tambah_produk -> {
                    navController.navigate(R.id.jualFragment)
                    true
                }
                else -> false
            }
        }
    }
}
