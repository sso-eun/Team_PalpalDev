package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

// POST /code_auth/verifyCode 요청 바디

data class CodeAuthVerifyRequest(
    @SerializedName("tel_num") val telNum: String,

    @SerializedName("auth_code") val authCode: String
)
