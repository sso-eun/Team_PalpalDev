package com.example.dundun_hi.model

import android.net.Uri
import java.util.UUID

data class SharedPhoto(
    val id: String = UUID.randomUUID().toString(),
    val fromMe: Boolean,
    val localUri: Uri? = null,
    val remoteUrl: String? = null,
    val resId: Int? = null
)