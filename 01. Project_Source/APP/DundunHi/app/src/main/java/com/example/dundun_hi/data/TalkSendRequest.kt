package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

data class TalkSendRequest(
    @SerializedName("sender_type")
    val senderType: Int,

    @SerializedName("sender_id")
    val senderId: Int,

    @SerializedName("receiver_id")
    val receiverId: Int,

    @SerializedName("image_url")
    val imageUrl: String
)