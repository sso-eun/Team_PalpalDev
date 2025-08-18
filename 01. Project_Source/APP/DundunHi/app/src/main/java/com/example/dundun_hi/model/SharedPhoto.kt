package com.example.dundun_hi.model

import android.net.Uri
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class SharedPhoto(
    val id: String = UUID.randomUUID().toString(),
    val fromMe: Boolean,
    val localUri: Uri? = null,
    val remoteUrl: String? = null,
    val resId: Int? = null,
    val authorName: String = "",
    val sendAt: LocalDateTime? = null, // 날짜 필드

) {
    // 날짜를 사용자 친화적인 형태로 포맷팅하는 헬퍼 함수
    fun getFormattedDate(): String {
        return sendAt?.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")) ?: ""
    }

    // 상대적 시간 표시 (예: "방금 전", "1시간 전", "어제" 등)
    fun getRelativeTime(): String {
        if (sendAt == null) return ""

        val now = LocalDateTime.now()
        val duration = java.time.Duration.between(sendAt, now)

        return when {
            duration.toMinutes() < 1 -> "방금 전"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}분 전"
            duration.toHours() < 24 -> "${duration.toHours()}시간 전"
            duration.toDays() < 7 -> "${duration.toDays()}일 전"
            else -> getFormattedDate()
        }
    }
}