package com.example.miniprojectv2

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

object ImageHandler {

    private val client = OkHttpClient()


    fun uploadImage(context: Context, uri: Uri, callback: (String?) -> Unit) {
        Log.d("ImageHandler", "uploadImage() START uri=$uri")

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("ImageHandler", "inputStream NULL → cannot read URI")
                callback(null)
                return
            }

            val tempFile = File(context.cacheDir, "upload_temp.jpg")
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            val fileBody = tempFile.asRequestBody("image/*".toMediaType())
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "image.jpg", fileBody)
                .build()

            val baseUrl = IpHelper.getBaseUrl()
            if (baseUrl == null) {
                Log.e("ImageHandler", "No server URL found. Please scan network first.")
                callback(null)
                return
            }

            val requestUrl = "$baseUrl/upload"
            Log.d("ImageHandler", "POST → $requestUrl")

            val request = Request.Builder()
                .url(requestUrl)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {
                    Log.e("ImageHandler", "HTTP FAILED → ${e.message}")
                    callback(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    val bodyString = response.body?.string() ?: ""
                    Log.d("ImageHandler", "RESPONSE: $bodyString")

                    val id = Regex("\"imageId\":\\s*\"(.*?)\"")
                        .find(bodyString)
                        ?.groups?.get(1)?.value

                    Log.d("ImageHandler", "Parsed imageId=$id")
                    callback(id)
                }
            })

        } catch (e: Exception) {
            Log.e("ImageHandler", "EXCEPTION → ${e.message}")
            callback(null)
        }
    }


    fun getImage(context: Context, id: String, callback: (ByteArray?) -> Unit) {
        val baseUrl = IpHelper.getBaseUrl()
        if (baseUrl == null) {
            Log.e("ImageHandler", "No server URL found. Cannot fetch image.")
            callback(null)
            return
        }

        val url = "$baseUrl/image/$id"
        Log.d("ImageHandler", "GET → $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                Log.e("ImageHandler", "GET FAILED → ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("ImageHandler", "GET RESPONSE status=${response.code}")
                val bytes = response.body?.bytes()
                callback(bytes)
            }
        })
    }
}
