// app/src/main/java/com/example/dundun_hi/model/SharedPhoto.kt
package com.example.dundun_hi.model

import android.net.Uri
import java.util.UUID

data class SharedPhoto(
    val id: String = UUID.randomUUID().toString(),
    val fromMe: Boolean,
    val localUri: Uri? = null,     // 갤러리에서 고른 사진
    val remoteUrl: String? = null, // 서버 업로드 완료 URL
    val resId: Int? = null         // drawable 리소스(더미)
)
