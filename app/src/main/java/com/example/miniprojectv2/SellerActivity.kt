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

    // Seller info
    var sellerUsername: String = "Penjual"
    var sellerEmail: String = "penjual@example.com"

    // Delivery expeditions list (mutable internally)
    private val _deliveryExpeditions: MutableList<String> = mutableListOf("JNE", "Tiki", "SiCepat")

    // Read-only access for fragments
    val deliveryExpeditions: List<String>
        get() = _deliveryExpeditions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.seller_toolbar)
        setSupportActionBar(toolbar)

        // NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_seller) as NavHostFragment
        val navController = navHostFragment.navController

        // Drawer & BottomNav
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout_seller)
        val navigationView = findViewById<NavigationView>(R.id.nav_view_seller)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_seller)

        // AppBar config
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.beliFragment, R.id.jualFragment, R.id.ordersFragment, R.id.accountFragment),
            drawerLayout
        )
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        bottomNav.setupWithNavController(navController)
        navigationView.setupWithNavController(navController)

        // Header info from SharedPreferences
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sellerUsername = prefs.getString("active_username", sellerUsername) ?: sellerUsername
        sellerEmail = prefs.getString("active_email", sellerEmail) ?: sellerEmail

        val headerView = navigationView.getHeaderView(0)
        val headerTitle = headerView.findViewById<TextView>(R.id.header_title)
        val headerSubtitle = headerView.findViewById<TextView>(R.id.header_subtitle)

        headerTitle.text = sellerUsername
        headerSubtitle.text = sellerEmail

        // Drawer item selection
        navigationView.setNavigationItemSelectedListener { menuItem ->
            val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
            if (handled) drawerLayout.closeDrawers()
            handled
        }
    }

    // Add a new expedition
    fun addDeliveryExpedition(expedition: String) {
        val trimmed = expedition.trim()
        if (trimmed.isNotEmpty() && !_deliveryExpeditions.contains(trimmed)) {
            _deliveryExpeditions.add(trimmed)

            // Save to SharedPreferences
            val prefs = getSharedPreferences("ExpeditionPrefs", Context.MODE_PRIVATE)
            prefs.edit().putStringSet("expeditions_set", _deliveryExpeditions.toSet()).apply()
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
