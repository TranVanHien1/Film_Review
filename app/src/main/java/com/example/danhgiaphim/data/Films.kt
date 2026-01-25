package com.example.danhgiaphim.data

import org.w3c.dom.Comment

data class Films(val movieID : String ="",
                 val title:String = "",
                 val releaseYear:String = "",
                 val director :String = "",
                 var genre : Map<String, Genre>? = null,
                 val synopsis :String = "",
                 val posterURL :String = "",
                 val trailerURL :String = "",
                 val createdAt :String = "",
                 var actor: Map<String, Actors>? = null,)    {
}