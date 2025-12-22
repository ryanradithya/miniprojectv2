package com.example.miniprojectv2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class NotificationService : Service() {

    companion object {
        const val CHANNEL_ID = "update_channel"
        const val CHANNEL_NAME = "Store Updates"
        const val EXTRA_ID = "notification_type"
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val type = intent?.getStringExtra(EXTRA_ID) ?: "update"

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Marketplace Update")
            .setContentText(
                when (type) {
                    "product-update" -> "A product was added or updated."
                    else -> "There's a new update in the store."
                }
            )
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(1, notification)

        stopSelf()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
