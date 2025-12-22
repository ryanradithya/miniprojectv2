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


    //endpoint api
    private var baseUrl: String = "https://umkmmanager.online"

    fun getBaseUrl(): String {
        return baseUrl

    }
}
