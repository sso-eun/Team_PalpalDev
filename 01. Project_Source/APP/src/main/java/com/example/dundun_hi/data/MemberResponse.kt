package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

data class MemberResponse(
    @SerializedName("user_num")
    val userNum: Int,

    @SerializedName("user_type")
    val userType: Int,          // 0: 어르신, 1: 보호자

    @SerializedName("user_id")
    val userId: String,         // 로그인 ID (아이디)

    @SerializedName("user_tel")
    val userTel: String,        // 전화번호

    @SerializedName("user_profile_img")
    val userProfileImg: String, // 프로필 이미지 URL 또는 경로 (빈 문자열이면 없음)

    @SerializedName("user_home_lat")
    val userHomeLat: String,    // 집 위도 (문자열로 올 수 있으니 String, 나중에 Double로 변환)

    @SerializedName("user_home_lot")
    val userHomeLot: String,    // 집 경도 (문자열)

    @SerializedName("user_condition")
    val userCondition: Int,     // 0: 집(귀가), 1: 외출 중

    @SerializedName("user_signup")
    val userSignup: String      // 가입일시 (예: "2025-05-31T00:00:00.000Z")
)
