package com.example.dundun_hi.data

data class CertListResponse(
    val totalRecordCount: Int,
    val totalPages: String,
    val currentPage: Int,
    val limit: Int,
    val req_no: Int,
    val guardian_no: Int,
    val guardian_id: String,
    val senior_num: Int,
    val senior_id: String,
    val certificate_img: String,
    val status: String,
    val submitted_at: String,
    val reviewed_at: String?,
    val reviewed_admin_no: Int?,
    val review_note: String?
)