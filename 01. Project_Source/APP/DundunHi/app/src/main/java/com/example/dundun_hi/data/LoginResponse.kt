package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message") val message: String,
    @SerializedName("user_num") val userNum: String
)

//서버가 반환하는 json구조를 받을 모델