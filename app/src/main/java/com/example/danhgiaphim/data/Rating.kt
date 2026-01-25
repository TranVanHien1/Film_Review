package com.example.danhgiaphim.data

data class Rating(
    val movieID : String ="",
    val contentRating: Float = 0F,
    val effectRating: Float = 0F,
    val castRating: Float = 0F,
    val rating: Float = 0F,
    val reviewCount: Long = 0L,
    val ratePoint: Float = 0F,
    val rateAI: Float = 0F){
}
