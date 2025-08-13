package com.example.dundun_hi.data

data class CertListResponse(
    val rsCode: Int,
    val totalResults: Int,
    val totalPages: Int,
    val currentPage: Int,
    val limit: Int,
    val results: List<Certificate>
)