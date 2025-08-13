package com.example.dundun_hi.data

import com.example.dundun_hi.network.CodeAuthService


class CodeAuthRepository(
    private val service: CodeAuthService
) {
    //SMS 인증번호 발송 비즈니스 로직
    suspend fun sendCode(telNum: String): CodeAuthSendResponse {
        return service.sendCode(
            CodeAuthSendRequest(telNum)
        )
    }
    // 인증번호 확인 로직
    suspend fun verifyCode(telNum: String, authCode: String): CodeAuthVerifyResponse {
        return service.verifyCode(
            CodeAuthVerifyRequest(telNum, authCode)
        )
    }
}
