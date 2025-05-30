package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

// POST /code_auth/send 응답

data class CodeAuthSendResponse(
    @SerializedName("rsCode")
    val rsCode: Int,
    @SerializedName("message")
    val message: String
)
