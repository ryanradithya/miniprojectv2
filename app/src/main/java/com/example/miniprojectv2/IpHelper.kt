package com.example.miniprojectv2

import android.content.Context

object IpHelper {

    /**
     * Returns the base URL of the server.
     * - Emulator: returns 10.0.2.2
     * - Real device: returns saved IP if available
     * - If not saved, returns null so auto-scan can trigger
     */
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



    /**
     * Save the detected server base URL
     */
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
