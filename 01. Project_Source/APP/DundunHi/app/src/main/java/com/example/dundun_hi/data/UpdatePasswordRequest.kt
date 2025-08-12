package com.example.dundun_hi.data

data class UpdatePasswordRequest(
    val current_pw: String,  // 현재 비밀번호
    val new_pw: String       // 새 비밀번호
)
