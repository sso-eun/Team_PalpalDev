package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_pw") val userPw: String
)
//로그인 api로 보낼 json바디를 정의

