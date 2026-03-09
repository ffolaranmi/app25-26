package com.example.smartvoice.network

data class PredictResponse(
    val pathology: Boolean,
    val p_healthy: Double,
    val p_pathology: Double
)