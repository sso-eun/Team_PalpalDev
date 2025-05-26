// app/src/main/java/com/example/dundun_hi/data/SignupRequest.kt
package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

data class SignupRequest(
    @SerializedName("user_type")        val user_type: Int,
    @SerializedName("user_id")          val user_id: String,
    @SerializedName("user_pw")          val user_pw: String,
    @SerializedName("user_tel")         val user_tel: String,
    @SerializedName("user_profile_img") val user_profile_img: String? = null,
    @SerializedName("user_home_lat")    val user_home_lat: String? = null,
    @SerializedName("user_home_lot")    val user_home_lot: String? = null,
    @SerializedName("user_condition")   val user_condition: Int,
)
