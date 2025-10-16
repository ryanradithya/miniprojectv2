package com.example.miniprojectv2

import android.content.Context
import android.os.Bundle
import android.widget.TextView
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

        // Get seller flag
        val isSeller = intent.getBooleanExtra("isSeller", false)
        if (isSeller) {
            supportActionBar?.title = "Dashboard Penjual"
        }

        // NavController setup
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_seller) as NavHostFragment
        val navController = navHostFragment.navController

        // Drawer & BottomNav
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout_seller)
        val navigationView = findViewById<NavigationView>(R.id.nav_view_seller)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_seller)

        // AppBar config – make all fragments top-level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.beliFragment,
                R.id.jualFragment,
                R.id.accountFragment // bottom + drawer can navigate here
            ),
            drawerLayout
        )

        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = prefs.getString("active_username", "John Doe")
        val email = prefs.getString("active_email", "johndoe@example.com")

        val navView = findViewById<com.google.android.material.navigation.NavigationView>(R.id.nav_view_seller)
        val headerView = navView.getHeaderView(0)
        val headerTitle = headerView.findViewById<TextView>(R.id.header_title)
        val headerSubtitle = headerView.findViewById<TextView>(R.id.header_subtitle)

        headerTitle.text = username
        headerSubtitle.text = email


        // Link toolbar with nav controller
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        // Link bottom nav + drawer with nav controller
        bottomNav.setupWithNavController(navController)
        navigationView.setupWithNavController(navController)

        // Optional: highlight “Account” properly when clicked from drawer
        navigationView.setNavigationItemSelectedListener { menuItem ->
            val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
            if (handled) drawerLayout.closeDrawers()
            handled
        }

        class SellerActivity : AppCompatActivity() {

            private lateinit var appBarConfiguration: AppBarConfiguration

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_seller)

                val toolbar = findViewById<Toolbar>(R.id.seller_toolbar)
                setSupportActionBar(toolbar)

                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_seller) as NavHostFragment
                val navController = navHostFragment.navController

                val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout_seller)
                val navigationView = findViewById<NavigationView>(R.id.nav_view_seller)
                val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_seller)

                // ✅ Update header info from SharedPreferences
                val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                val username = prefs.getString("seller_username", "Penjual") ?: "Penjual"
                val email = prefs.getString("seller_email", "penjual@example.com") ?: "penjual@example.com"

                val headerView = navigationView.getHeaderView(0)
                val headerTitle = headerView.findViewById<TextView>(R.id.header_title)
                val headerSubtitle = headerView.findViewById<TextView>(R.id.header_subtitle)

                headerTitle.text = username
                headerSubtitle.text = email

                // Konfigurasi navigasi
                appBarConfiguration = AppBarConfiguration(
                    setOf(R.id.beliFragment, R.id.jualFragment, R.id.accountFragment),
                    drawerLayout
                )
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
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

    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_seller) as NavHostFragment
        val navController = navHostFragment.navController
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
