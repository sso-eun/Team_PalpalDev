package com.example.dundun_hi.data


data class DateListResponse(
    val results: List<DateItem>
)

data class DateItem(
    val user_date_no: Int,         // 일정 고유 번호
    val user_num: Int,             // 사용자 번호
    val user_date_title: String,   // 일정 제목
    val user_date_time: String,    // 일정 시간 "2025/08/11 17:19"
    val user_date_info: String     // 일정 정보 (DATETIME 형식)
)