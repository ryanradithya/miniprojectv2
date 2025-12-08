package com.example.miniprojectv2

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView

class SellerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private enum class SellerTab { LIST, ADD, ORDERS, ACCOUNT }

    var sellerUsername: String = "Penjual"
    var sellerEmail: String = "penjual@example.com"

    private val _deliveryExpeditions: MutableList<String> =
        mutableListOf("JNE", "Tiki", "SiCepat")
    val deliveryExpeditions: List<String>
        get() = _deliveryExpeditions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller)

        // Status bar
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_custom_status_bar)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.seller_toolbar)
        setSupportActionBar(toolbar)

        // NavHost
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_seller) as NavHostFragment
        val navController = navHostFragment.navController

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout_seller)
        val navigationView = findViewById<NavigationView>(R.id.nav_view_seller)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.beliFragment, R.id.jualFragment, R.id.ordersFragment, R.id.accountFragment),
            drawerLayout
        )

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

        // ==============================
        // CUSTOM NAVBAR
        // ==============================

        // Tabs
        val tabList = findViewById<View>(R.id.nav_list_produk)
        val tabAdd = findViewById<View>(R.id.nav_tambah_produk)
        val tabOrders = findViewById<View>(R.id.nav_pesanan)
        val tabAccount = findViewById<View>(R.id.nav_account_seller)

        // Circles
        val circleList = findViewById<View>(R.id.nav_list_produk_circle)
        val circleAdd = findViewById<View>(R.id.nav_tambah_produk_circle)
        val circleOrders = findViewById<View>(R.id.nav_pesanan_circle)
        val circleAccount = findViewById<View>(R.id.nav_account_seller_circle)

        // Icons
        val iconList = findViewById<ImageView>(R.id.nav_list_produk_icon)
        val iconAdd = findViewById<ImageView>(R.id.nav_tambah_produk_icon)
        val iconOrders = findViewById<ImageView>(R.id.nav_pesanan_icon)
        val iconAccount = findViewById<ImageView>(R.id.nav_account_seller_icon)

        // Labels
        val labelList = findViewById<TextView>(R.id.nav_list_produk_label)
        val labelAdd = findViewById<TextView>(R.id.nav_tambah_produk_label)
        val labelOrders = findViewById<TextView>(R.id.nav_pesanan_label)
        val labelAccount = findViewById<TextView>(R.id.nav_account_seller_label)

        fun setSelectedTab(tab: SellerTab) {

            fun apply(circle: View, icon: ImageView, label: TextView, selected: Boolean) {
                circle.visibility = if (selected) View.VISIBLE else View.GONE
                label.visibility = if (selected) View.VISIBLE else View.GONE

                val iconColor = if (selected)
                    ContextCompat.getColor(this, android.R.color.white)
                else
                    ContextCompat.getColor(this, R.color.gray)

                val labelColor = if (selected)
                    ContextCompat.getColor(this, R.color.green_primary)
                else
                    ContextCompat.getColor(this, R.color.gray)

                icon.setColorFilter(iconColor)
                label.setTextColor(labelColor)
            }

            apply(circleList, iconList, labelList, tab == SellerTab.LIST)
            apply(circleAdd, iconAdd, labelAdd, tab == SellerTab.ADD)
            apply(circleOrders, iconOrders, labelOrders, tab == SellerTab.ORDERS)
            apply(circleAccount, iconAccount, labelAccount, tab == SellerTab.ACCOUNT)
        }

        // On Click Listeners
        tabList.setOnClickListener {
            if (navController.currentDestination?.id != R.id.beliFragment) {
                navController.navigate(R.id.beliFragment)
            }
            setSelectedTab(SellerTab.LIST)
        }

        tabAdd.setOnClickListener {
            if (navController.currentDestination?.id != R.id.jualFragment) {
                navController.navigate(R.id.jualFragment)
            }
            setSelectedTab(SellerTab.ADD)
        }

        tabOrders.setOnClickListener {
            if (navController.currentDestination?.id != R.id.ordersFragment) {
                navController.navigate(R.id.ordersFragment)
            }
            setSelectedTab(SellerTab.ORDERS)
        }

        tabAccount.setOnClickListener {
            if (navController.currentDestination?.id != R.id.accountFragment) {
                navController.navigate(R.id.accountFragment)
            }
            setSelectedTab(SellerTab.ACCOUNT)
        }

        // Auto-sync when navigating
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.beliFragment -> setSelectedTab(SellerTab.LIST)
                R.id.jualFragment -> setSelectedTab(SellerTab.ADD)
                R.id.ordersFragment -> setSelectedTab(SellerTab.ORDERS)
                R.id.accountFragment -> setSelectedTab(SellerTab.ACCOUNT)
            }
        }

        // Default tab
        setSelectedTab(SellerTab.LIST)

        // Drawer header
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sellerUsername = prefs.getString("active_username", sellerUsername) ?: sellerUsername
        sellerEmail = prefs.getString("active_email", sellerEmail) ?: sellerEmail

        val headerView = navigationView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.header_title).text = sellerUsername
        headerView.findViewById<TextView>(R.id.header_subtitle).text = sellerEmail
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_seller) as NavHostFragment
        val navController = navHostFragment.navController
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun addDeliveryExpedition(expedition: String) {
        val trimmed = expedition.trim()
        if (trimmed.isNotEmpty() && !_deliveryExpeditions.contains(trimmed)) {
            _deliveryExpeditions.add(trimmed)
            val prefs = getSharedPreferences("ExpeditionPrefs", Context.MODE_PRIVATE)
            prefs.edit().putStringSet("expeditions_set", _deliveryExpeditions.toSet()).apply()
        }
    }
}
