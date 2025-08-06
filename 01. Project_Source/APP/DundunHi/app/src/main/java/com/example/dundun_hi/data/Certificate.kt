package com.example.dundun_hi.data

data class Certificate(
    val req_no: Int,
    val guardian_no: Int,
    val guardian_id: String,
    val senior_num: Int,
    val senior_id: String,
    val certificate_img: String,
    val status: Int,
    val submitted_at: String,
    val reviewed_at: String?,
    val reviewer_admin_no: Int?,
    val reviewer_note: String?
)
