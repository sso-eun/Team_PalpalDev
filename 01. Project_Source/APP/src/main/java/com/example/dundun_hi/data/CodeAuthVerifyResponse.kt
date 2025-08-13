package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

/**
 * POST /code_auth/verifyCode 응답
 */
data class CodeAuthVerifyResponse(
    @SerializedName("rsCode")
    val rsCode: Int,

    @SerializedName("message")
    val message: String
)
