package com.example.miniprojectv2

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.atomic.AtomicBoolean


object IpHelper {
    private val client = OkHttpClient()


    private var baseUrl: String = "http://192.168.1.25:8000"

    fun getBaseUrl(): String {
//                return "http://10.0.2.2:8000"
        return baseUrl

//        val isEmulator = android.os.Build.FINGERPRINT.contains("generic") ||
//                android.os.Build.MODEL.contains("google_sdk") ||
//                android.os.Build.MODEL.contains("Emulator") ||
//                android.os.Build.MANUFACTURER.contains("Genymotion")
//
//        if (isEmulator) return "http://10.0.2.2:8000"
//
//        // Get saved IP from SharedPreferences
//        val prefs = context.getSharedPreferences("server_prefs", Context.MODE_PRIVATE)
//        return prefs.getString("base_url", null)
    }

    fun changeBaseUrl(newUrl: String) {
        baseUrl = newUrl
    }

    fun autoDetectServer(context: Context, onResult: (Boolean) -> Unit) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipInt = wifiManager.connectionInfo.ipAddress

        if (ipInt == 0) {
            onResult(false)
            return
        }

        // Convert device IP
        val deviceIp = InetAddress.getByAddress(
            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()
        ).hostAddress

        val subnet = deviceIp.substringBeforeLast(".") // "192.168.1"

        Log.d("IpHelper", "Scanning subnet: $subnet.*")

        val executor = Executors.newFixedThreadPool(50)
        val found = AtomicBoolean(false)

        for (i in 1..254) {
            executor.execute {
                if (found.get()) return@execute

                val testIp = "http://$subnet.$i:8000/ping"

                try {
                    val request = Request.Builder().url(testIp).build()
                    val response = client.newCall(request).execute()
                    Log.d("IpHelper", "Server searching at $baseUrl")

                    if (response.isSuccessful) {
                        if (found.compareAndSet(false, true)) {
                            baseUrl = "http://$subnet.$i:8000"
                            Log.d("IpHelper", "Server FOUND at $baseUrl")
                            onResult(true)
                        }
                    }
                } catch (_: Exception) {
                    // ignore failed hosts
                    Log.d("IpHelper", "Server NOT found at $testIp")
                }
            }
        }

        // Timeout failsafe
        Thread {
            Thread.sleep(8000)
            if (!found.get()) {
                Log.d("IpHelper", "Server NOT found in subnet scan.")
                onResult(false)
            }
        }.start()
    }


//    fun saveBaseUrl(context: Context, url: String) {
//        context.getSharedPreferences("server_prefs", Context.MODE_PRIVATE)
//            .edit()
//            .putString("base_url", url)
//            .apply()
//    }
//
//    /**
//     * Clear the saved server IP (optional)
//     */
//    fun clearBaseUrl(context: Context) {
//        context.getSharedPreferences("server_prefs", Context.MODE_PRIVATE)
//            .edit()
//            .remove("base_url")
//            .apply()
//    }


}
