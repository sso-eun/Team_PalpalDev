package com.example.dundun_hi.model

//data class SharedPhoto(
//    val url: String,
//    val fromMe: Boolean   // 내가 보낸 사진이면 true
//)
data class SharedPhoto(
    val resId: Int,     // R.drawable.xxx
    val fromMe: Boolean
)