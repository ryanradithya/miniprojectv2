package com.example.miniprojectv2.API

import com.example.miniprojectv2.model.LoanRequest
import com.example.miniprojectv2.model.PredictionResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MLApiService {
    @Headers("Authorization: Bearer diggiggidawgdawg")
    @POST("/predict")
    suspend fun predict(@Body request: LoanRequest): PredictionResponse
}
