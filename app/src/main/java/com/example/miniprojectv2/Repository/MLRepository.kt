package com.example.miniprojectv2.Repository

import com.example.miniprojectv2.API.RetrofitClient
import com.example.miniprojectv2.model.LoanRequest
import com.example.miniprojectv2.model.PredictionResponse

class MLRepository {

    //fun untuk melakukan prediksi (api)
    suspend fun predictLoan(request: LoanRequest): Result<PredictionResponse> {
        return try {
            val response = RetrofitClient.api.predict(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
