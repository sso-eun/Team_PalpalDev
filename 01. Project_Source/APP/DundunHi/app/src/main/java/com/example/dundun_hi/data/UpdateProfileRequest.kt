// app/src/main/java/com/example/dundun_hi/data/UpdateProfileRequest.kt

package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("user_tel")
    val userTel: String,

    @SerializedName("user_profile_img")
    val userProfileImg: String,

    @SerializedName("user_home_lat")
    val userHomeLat: String,

    @SerializedName("user_home_lot")
    val userHomeLot: String,

    @SerializedName("user_condition")
    val userCondition: String
)
