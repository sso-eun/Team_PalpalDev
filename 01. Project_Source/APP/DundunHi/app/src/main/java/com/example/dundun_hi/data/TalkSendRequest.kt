package com.example.dundun_hi.data

data class TalkSendRequest(
    val senderType: Int,
    val senderId: Int,
    val receiverId: Int,
    val imageUrl: String
) 