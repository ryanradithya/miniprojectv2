package com.example.miniprojectv2

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var transactionListener: ListenerRegistration? = null

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("Permission", "Notification permission granted!")
            } else {
                Log.e("Permission", "Notification permission denied.")
            }
        }


    // Tab enum sederhana
    private enum class BottomTab { HOME, TRANSACTIONS, ACCOUNT }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Status bar
        askNotificationPermission()
        startTransactionRealtimeListener()

        // 🟢 Set custom status bar color
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_custom_status_bar)

        // Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.navigationIcon?.setTint(getColor(android.R.color.white))

        // Drawer & Nav
        val drawerLayout = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val bottomNavContainer = findViewById<View>(R.id.bottom_nav)

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

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // --------------------------
        // Custom bottom navigation
        // --------------------------
        val homeTab = findViewById<View>(R.id.nav_home)
        val transTab = findViewById<View>(R.id.nav_transactions)
        val accountTab = findViewById<View>(R.id.nav_account)

        val homeCircle = findViewById<View>(R.id.nav_home_circle)
        val transCircle = findViewById<View>(R.id.nav_transactions_circle)
        val accountCircle = findViewById<View>(R.id.nav_account_circle)

        val homeIcon = findViewById<ImageView>(R.id.nav_home_icon)
        val transIcon = findViewById<ImageView>(R.id.nav_transactions_icon)
        val accountIcon = findViewById<ImageView>(R.id.nav_account_icon)

        val homeLabel = findViewById<TextView>(R.id.nav_home_label)
        val transLabel = findViewById<TextView>(R.id.nav_transactions_label)
        val accountLabel = findViewById<TextView>(R.id.nav_account_label)

        fun setSelectedTab(tab: BottomTab) {
            // reset semua
            fun apply(
                circle: View,
                icon: ImageView,
                label: TextView,
                selected: Boolean
            ) {
                circle.visibility = if (selected) View.VISIBLE else View.GONE
                label.visibility = if (selected) View.VISIBLE else View.GONE
                label.isSelected = selected
                icon.isSelected = selected

                val iconColor = if (selected)
                    ContextCompat.getColor(this, android.R.color.white)
                else
                    ContextCompat.getColor(this, R.color.gray)

                val textColor = if (selected)
                    ContextCompat.getColor(this, R.color.green_primary)
                else
                    ContextCompat.getColor(this, R.color.gray)

                icon.setColorFilter(iconColor)
                label.setTextColor(textColor)
            }

            apply(homeCircle, homeIcon, homeLabel, tab == BottomTab.HOME)
            apply(transCircle, transIcon, transLabel, tab == BottomTab.TRANSACTIONS)
            apply(accountCircle, accountIcon, accountLabel, tab == BottomTab.ACCOUNT)
        }

        // Klik tab -> navigate
        homeTab.setOnClickListener {
            if (navController.currentDestination?.id != R.id.homeFragment) {
                navController.navigate(R.id.homeFragment)
            }
            setSelectedTab(BottomTab.HOME)
        }

        transTab.setOnClickListener {
            if (navController.currentDestination?.id != R.id.transactionsFragment) {
                navController.navigate(R.id.transactionsFragment)
            }
            setSelectedTab(BottomTab.TRANSACTIONS)
        }

        accountTab.setOnClickListener {
            if (navController.currentDestination?.id != R.id.accountFragment) {
                navController.navigate(R.id.accountFragment)
            }
            setSelectedTab(BottomTab.ACCOUNT)
        }

        // Sinkronkan tab ketika destination berubah (misal dari checkout balik ke home)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment,
                R.id.jualFragment,
                R.id.beliFragment -> {
                    bottomNavContainer.visibility = View.VISIBLE
                    setSelectedTab(BottomTab.HOME)
                }
                R.id.transactionsFragment -> {
                    bottomNavContainer.visibility = View.VISIBLE
                    setSelectedTab(BottomTab.TRANSACTIONS)
                }
                R.id.accountFragment -> {
                    bottomNavContainer.visibility = View.VISIBLE
                    setSelectedTab(BottomTab.ACCOUNT)
                }
                R.id.cartFragment,
                R.id.checkoutFragment,
                R.id.productDetailFragment -> {
                    bottomNavContainer.visibility = View.GONE
                }
                else -> {
                    bottomNavContainer.visibility = View.VISIBLE
                }
            }
        }

        // Set tab awal
        setSelectedTab(BottomTab.HOME)

        // Drawer header
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = prefs.getString("active_username", "John Doe")
        val email = prefs.getString("active_email", "johndoe@example.com")

        val headerView = navView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.header_title).text = username
        headerView.findViewById<TextView>(R.id.header_subtitle).text = email
    }

    private fun askNotificationPermission() {
        // Notification permission only exists on API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS

            when {
                ContextCompat.checkSelfPermission(this, permission) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    Log.d("Permission", "Notification permission already granted")
                }

                shouldShowRequestPermissionRationale(permission) -> {
                    AlertDialog.Builder(this)
                        .setTitle("Izin Notifikasi Dibutuhkan")
                        .setMessage("Aplikasi membutuhkan izin untuk mengirim notifikasi pembaruan.")
                        .setPositiveButton("OK") { _, _ ->
                            requestNotificationPermissionLauncher.launch(permission)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

                else -> {
                    requestNotificationPermissionLauncher.launch(permission)
                }
            }
        }
    }


    private fun startTransactionRealtimeListener() {
        val db = FirebaseFirestore.getInstance()

        transactionListener = db.collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Realtime", "Listener error: $error")
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                for (dc in snapshot.documentChanges) {
                    when (dc.type) {

                        DocumentChange.Type.ADDED -> {
                            showTransactionNotification("Transaksi baru masuk!")
                        }

                        DocumentChange.Type.MODIFIED -> {
                            showTransactionNotification("Transaksi diperbarui!")
                        }

                        DocumentChange.Type.REMOVED -> {
                            showTransactionNotification("Transaksi dihapus!")
                        }
                        else -> {}
                    }
                }
            }
    }

    private fun showTransactionNotification(msg: String) {
        val channelId = "transaction_updates"

        val manager = getSystemService(NotificationManager::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Transaction Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Update Transaksi")
            .setContentText(msg)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notif)
    }

    // 🟢 Handle Up Navigation (back arrow)
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_appbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment))
                || super.onOptionsItemSelected(item)
    }

    fun setSelectedTabFromFragment(tab: String) {
        when (tab) {
            "home" -> findViewById<View>(R.id.nav_home).performClick()
            "trans" -> findViewById<View>(R.id.nav_transactions).performClick()
            "account" -> findViewById<View>(R.id.nav_account).performClick()
        }
    }

}
