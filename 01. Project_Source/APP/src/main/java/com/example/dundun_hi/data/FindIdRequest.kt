// app/src/main/java/com/example/dundun_hi/data/SignupRequest.kt
package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

data class FindIdRequest(
    @SerializedName("user_tel")         val user_tel: String,
)
