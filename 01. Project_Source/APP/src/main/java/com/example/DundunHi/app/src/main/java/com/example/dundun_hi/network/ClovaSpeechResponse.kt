package com.example.dundun_hi.network

import com.google.gson.annotations.SerializedName

/**
 * Clova Speech API의 실제 응답 구조에 맞춘 데이터 클래스
 * 지금은 최상위에 있는 'text' 필드만 사용할 예정
 */
data class ClovaSpeechResponse(
    // 우리가 최종적으로 사용할 전체 음성인식 텍스트
    @SerializedName("text")
    val text: String,

    // 텍스트의 각 문단(segment) 정보를 담는 리스트
    // 지금 당장 사용하진 않지만, 나중을 위해 구조를 만들어 둡니다.
    @SerializedName("segments")
    val segments: List<Segment>
)

/**
 * 'segments' 리스트 안에 들어있는 개별 문단 객체의 구조
 */
data class Segment(
    @SerializedName("text")
    val text: String,

    @SerializedName("start")
    val start: Int,

    @SerializedName("end")
    val end: Int
)