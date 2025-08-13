package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

//POST /code_auth/send 요청

data class CodeAuthSendRequest(
    @SerializedName("tel_num") val telNum: String
)
