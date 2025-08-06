// 25-08-05 은재 추가
package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

// 1. API 응답 전체를 감싸는 데이터 클래스
// API가 사용자 정보 리스트를 반환한다고 가정
data class SearchMemberResponse(
    val results: List<SearchedMember>
)

data class SearchedMember(
    @SerializedName("user_num") val userNum: Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_tel") val userTel: String
)