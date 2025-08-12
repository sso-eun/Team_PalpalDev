package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

data class FindIdResponse(
    @SerializedName("message") val message: String,
    @SerializedName("user_num") val userNum: String,
    @SerializedName("user_id")  val userId: String
)

