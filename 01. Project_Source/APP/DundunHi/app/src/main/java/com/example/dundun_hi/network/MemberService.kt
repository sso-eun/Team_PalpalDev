package com.example.dundun_hi.network
//newnewnewn
import com.example.dundun_hi.data.LoginRequest
import com.example.dundun_hi.data.LoginResponse
import com.example.dundun_hi.data.SignupRequest
import com.example.dundun_hi.data.SignupResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface MemberService {
    // /member/login 엔드포인트
    @POST("member/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    //signup
    @POST("/member/signup")
    suspend fun signup(@Body req: SignupRequest): Response<SignupResponse>

}
