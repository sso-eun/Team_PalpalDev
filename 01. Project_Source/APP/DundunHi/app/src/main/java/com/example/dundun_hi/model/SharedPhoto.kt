package com.example.dundun_hi.model

import android.net.Uri
import java.util.UUID

data class SharedPhoto(
    val id: String = UUID.randomUUID().toString(),
    val fromMe: Boolean,
    val senderUserId: String,
    val senderName: String = "", // 나중을 위한 필드
    val sendAt: String = "",
    val localUri: Uri? = null,
    val remoteUrl: String? = null,
    val resId: Int? = null
)
