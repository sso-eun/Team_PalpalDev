package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

data class SimpleResponse(
    @SerializedName("rsCode")  val rsCode: Int,
    @SerializedName("message") val message: String
)
