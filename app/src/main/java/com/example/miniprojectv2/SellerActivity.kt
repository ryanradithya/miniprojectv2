package com.example.miniprojectv2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class SellerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller)

        // Toolbar setup
        val toolbar = findViewById<Toolbar>(R.id.seller_toolbar)
        setSupportActionBar(toolbar)

        // NavController setup
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_seller) as NavHostFragment
        val navController = navHostFragment.navController

        // DrawerLayout & NavigationView
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout_seller)
        val navigationView = findViewById<NavigationView>(R.id.nav_view_seller)

        // BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_seller)

        // Konfigurasi top-level destinations (beli, jual)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.beliFragment, R.id.jualFragment),
            drawerLayout // Hubungkan drawer
        )

        // Hubungkan Toolbar dengan NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        // Hubungkan BottomNav dan DrawerNav ke NavController
        bottomNav.setupWithNavController(navController)
        navigationView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_seller) as NavHostFragment
        val navController = navHostFragment.navController
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
