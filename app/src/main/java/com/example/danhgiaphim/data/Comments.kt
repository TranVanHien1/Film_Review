package com.example.danhgiaphim.data

data class Comments(val filmID : String = "",
                    val reviewID : String = "",
                    val userID : String = "",
                    val contentRating : Float = 0F,
                    val effectRating : Float = 0F,
                    val castRating : Float = 0F,
                    val comment : String = "",
                    var like : Long = 0L,
                    val reviewDate : String = "",
                    val sentimentLabel : String? = null){

}
