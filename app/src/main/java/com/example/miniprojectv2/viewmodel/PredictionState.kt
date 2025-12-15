package com.example.miniprojectv2.viewmodel

sealed class PredictionState {
    object Idle : PredictionState()
    object Loading : PredictionState()
    data class Success(
        val prediction: String,
        val description: String
    ) : PredictionState()
    data class Error(val message: String) : PredictionState()
}

