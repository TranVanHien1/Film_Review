package com.example.danhgiaphim.API

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ToxicCheckApi {
    @POST("/check_toxic")
    suspend fun checkComment(@Body request: ToxicCheckRequest): ToxicCheckResponse
}

data class ToxicCheckRequest(val text: String)
data class ToxicCheckResponse(
    val is_toxic: Boolean,
    val probabilities: List<Float>,
    val labels: List<String>
)