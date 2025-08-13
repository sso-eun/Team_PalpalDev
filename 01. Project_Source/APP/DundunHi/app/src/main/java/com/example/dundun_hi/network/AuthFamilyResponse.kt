package com.example.dundun_hi.network
import com.google.gson.annotations.SerializedName

// 25-08-05 은재 추가
// 로딩화면에서 필요한 데이터
// guardian_auth_upload 조회해서  seniorNum, status 사용
data class AuthFamilyResponse(
    @SerializedName("req_no") val reqNo: Int,
    @SerializedName("guardian_no") val guardianNo: Int,
    @SerializedName("senior_num") val seniorNum: Int,
    @SerializedName("certificate_img") val certificateImg: String,
    @SerializedName("status") val status: Int, // 0: 대기, 1: 승인, 2: 반려
    @SerializedName("submitted_at") val submittedAt: String,
    @SerializedName("reviewed_at") val reviewedAt: String?,
    @SerializedName("reviewer_admin_no") val reviewerAdminNo: Int?,
    @SerializedName("reviewer_note") val reviewerNote: String?
)