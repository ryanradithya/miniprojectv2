package com.example.miniprojectv2

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🟢 Set custom status bar color
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_custom_status_bar)

        // 🟢 Setup Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.navigationIcon?.setTint(getColor(android.R.color.white)) // tint icons if needed

        // 🟢 Setup Drawer and NavHost
        val drawerLayout = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 🟢 Define which fragments are top-level (show hamburger)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.jualFragment,
                R.id.beliFragment,
                R.id.transactionsFragment,
                R.id.accountFragment
            ),
            drawerLayout
        )

        // 🟢 Set up Toolbar with Navigation
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 🟢 Connect NavigationView (drawer menu)
        navView.setupWithNavController(navController)

        // 🟢 Connect Bottom Navigation
        bottomNav.setupWithNavController(navController)

        // 🟢 Drawer header info from SharedPreferences
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = prefs.getString("active_username", "John Doe")
        val email = prefs.getString("active_email", "johndoe@example.com")

        val headerView = navView.getHeaderView(0)
        val headerTitle = headerView.findViewById<TextView>(R.id.header_title)
        val headerSubtitle = headerView.findViewById<TextView>(R.id.header_subtitle)

        headerTitle.text = username
        headerSubtitle.text = email

        // 🟢 Hide bottom nav on specific fragments
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.cartFragment,
                R.id.checkoutFragment,
                R.id.transactionsFragment,
                R.id.productDetailFragment -> bottomNav.visibility = View.GONE
                else -> bottomNav.visibility = View.VISIBLE
            }
        }
    }

    // 🟢 Handle Up Navigation (back arrow)
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // 🟢 Inflate Toolbar Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_appbar_menu, menu)
        return true
    }

    // 🟢 Handle Toolbar Menu clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment))
                || super.onOptionsItemSelected(item)
    }
}

// OLD CODE!!
//package com.example.miniprojectv2
//
//import android.content.Context
//import android.os.Bundle
//import android.view.Menu
//import android.view.MenuItem
//import android.util.Log
//import android.view.View
//import android.view.WindowManager
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import androidx.navigation.findNavController
//import androidx.navigation.fragment.NavHostFragment
//import androidx.navigation.ui.*
//import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.google.android.material.navigation.NavigationView
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var appBarConfiguration: AppBarConfiguration
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        val window = window
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//        window.statusBarColor = ContextCompat.getColor(this, R.color.my_custom_status_bar)
//
//// Pakai toolbar dari layout
//        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//
//// Set icon burger (menu)
//        toolbar.setNavigationIcon(R.drawable.ic_menu)
//
//// Ubah warnanya jadi putih (kalau pakai vector bisa di-tint)
//        toolbar.navigationIcon?.setTint(getColor(android.R.color.white))
//
//// Biar bisa buka navigation drawer
//        val drawerLayout = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
//        toolbar.setNavigationOnClickListener {
//            drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
//        }
//
//
//        // Ambil navHostFragment dan navController
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.navController
//
//        // Top level destinations (bottom nav items)
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.homeFragment,
//                R.id.jualFragment,
//                R.id.beliFragment,
//                R.id.transactionsFragment,
//                R.id.accountFragment
//            ),
//            findViewById(R.id.drawer_layout) // hubungkan dengan drawer
//        )
//
//        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
//        val username = prefs.getString("active_username", "John Doe")
//        val email = prefs.getString("active_email", "johndoe@example.com")
//
//        val navView = findViewById<com.google.android.material.navigation.NavigationView>(R.id.nav_view)
//        val headerView = navView.getHeaderView(0)
//        val headerTitle = headerView.findViewById<TextView>(R.id.header_title)
//        val headerSubtitle = headerView.findViewById<TextView>(R.id.header_subtitle)
//
//        headerTitle.text = username
//        headerSubtitle.text = email
//
//
//        // Hubungkan toolbar dengan navController
//        setupActionBarWithNavController(navController, appBarConfiguration)
//
//        // Hubungkan drawer navigation dengan navController
//        navView.setupWithNavController(navController)
//
//        // Hubungkan bottom navigation dengan navController
//        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)
//        bottomNav.setupWithNavController(navController)
//
//        //tambahin disini, buat hilangin navbar di 1 halaman
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            when (destination.id) {
//                R.id.cartFragment,
//                R.id.checkoutFragment,
//                R.id.transactionsFragment,
//                R.id.productDetailFragment -> bottomNav.visibility = View.GONE
//                else -> bottomNav.visibility = View.VISIBLE
//            }
//        }
//    }
//
//    // supaya tombol back/up jalan
//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }
//
//    // tampilkan menu (search + cart)
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.top_appbar_menu, menu)
//        return true
//    }
//
//    // handle klik icon top appbar
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
////            R.id.action_cart -> {
////                Log.d("MainActivity", "Cart icon clicked")
////                findNavController(R.id.nav_host_fragment).navigate(R.id.cartFragment)
////                true
////            }
////            R.id.action_search -> {
////                Log.d("MainActivity", "Search icon clicked")
////                true
////            }
//            else -> item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment))
//                    || super.onOptionsItemSelected(item)
//        }
//    }
//}

