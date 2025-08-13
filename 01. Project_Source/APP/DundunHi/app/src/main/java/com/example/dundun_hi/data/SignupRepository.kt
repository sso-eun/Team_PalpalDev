// app/src/main/java/com/example/dundun_hi/data/SignupRepository.kt
package com.example.dundun_hi.data

import com.example.dundun_hi.network.RetrofitClient
import retrofit2.Response

class SignupRepository {
    // Response<> 로 받아오도록 변경
    suspend fun signup(req: SignupRequest): SignupResponse {
        val response: Response<SignupResponse> =
            RetrofitClient.memberService.signup(req)

        if (response.isSuccessful) {
            // body() 가 null 이면 예외 처리
            return response.body()!!
        } else {
            throw Exception("회원가입 실패: ${response.code()} ${response.errorBody()?.string()}")
        }
    }
}
