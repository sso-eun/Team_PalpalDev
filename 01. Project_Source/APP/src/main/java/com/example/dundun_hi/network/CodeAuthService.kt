package com.example.dundun_hi.network

import com.example.dundun_hi.data.CodeAuthSendRequest
import com.example.dundun_hi.data.CodeAuthSendResponse
import com.example.dundun_hi.data.CodeAuthVerifyRequest
import com.example.dundun_hi.data.CodeAuthVerifyResponse
import retrofit2.http.Body
import retrofit2.http.POST

// SMS 인증번호 발송 API
interface CodeAuthService {
    @POST("code_auth/send")
    suspend fun sendCode(
        @Body request: CodeAuthSendRequest
    ): CodeAuthSendResponse
// 인증번호 확인 api
    @POST("code_auth/verifyCode")
    suspend fun verifyCode(
        @Body request: CodeAuthVerifyRequest
    ): CodeAuthVerifyResponse
}
