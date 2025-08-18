package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName


data class UpdateAlertRequest(
    @SerializedName("user_date_title")
    val title: String,

    @SerializedName("user_date_time")
    val dateTime: String,

    @SerializedName("user_date_info")
    val dateInfo: String
)