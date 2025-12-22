package com.example.miniprojectv2.API

import com.example.miniprojectv2.API.MLApiService
import com.example.miniprojectv2.IpHelper
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    //ambil url
    private var BASE_URL = IpHelper.getBaseUrl()

    val api: MLApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MLApiService::class.java)
    }
}
