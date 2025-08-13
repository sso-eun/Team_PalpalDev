package com.example.dundun_hi.data

/**
 * 일정 목록 조회 API 응답 데이터 클래스
 */
data class DateListResponse(
    val results: List<DateItem>
)

/**
 * 개별 일정 항목 데이터 클래스
 */
data class DateItem(
    val user_date_no: Int,         // 일정 고유 번호
    val user_num: Int,             // 사용자 번호
    val user_date_title: String,   // 일정 제목
    val user_date_time: String,    // 일정 시간 "2025/08/11 17:19"
    val user_date_info: String     // 일정 정보 (DATETIME 형식)
)