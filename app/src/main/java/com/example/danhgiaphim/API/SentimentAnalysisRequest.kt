package com.example.danhgiaphim.API

import retrofit2.http.Body
import retrofit2.http.POST


interface SentimentAnalysisApi {
    @POST("/predict")
    suspend fun analyzeSentiment(@Body request: SentimentAnalysisRequest): SentimentAnalysisResponse
}

data class SentimentAnalysisRequest(
    val text: String
)

data class SentimentAnalysisResponse(
    val prediction: Int,
    val probabilities: List<Float>,
    val labels: Map<Int, String>
)
