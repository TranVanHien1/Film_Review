package com.example.danhgiaphim.data

import java.sql.Timestamp
import java.util.Date

data class Users (val userID : String = "",
                  val username : String = "",
                  val email : String ="",
                  val passwordHash : String ="",
                  val avatarURL : String ="",
                  val avatarID : String ="",
                  val dateOfBirth : Long = Date().time,
                  val gender : String ="",
                  val role : String ="") {
}