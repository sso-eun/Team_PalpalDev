package com.example.dundun_hi.network

import com.example.dundun_hi.data.FindIdRequest
import com.example.dundun_hi.data.FindIdResponse
import com.example.dundun_hi.data.LoginRequest
import com.example.dundun_hi.data.LoginResponse
import com.example.dundun_hi.data.MemberResponse
import com.example.dundun_hi.data.SignupRequest
import com.example.dundun_hi.data.SignupResponse
import com.example.dundun_hi.data.UpdatePasswordRequest
import com.example.dundun_hi.data.UpdatePasswordResponse
import com.example.dundun_hi.data.UpdateProfileRequest
import com.example.dundun_hi.data.UpdateProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface MemberService {
    // /member/login 엔드포인트
    @POST("member/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    //signup
    @POST("/member/signup")
    suspend fun signup(@Body req: SignupRequest): Response<SignupResponse>

    @POST("member/findid")
    suspend fun findId(@Body req: FindIdRequest): Response<FindIdResponse>

    @GET("member/getmember/{user_num}")
    suspend fun getMember(@Path("user_num") userNum: Int): MemberResponse

    @PUT("/member/profile/{user_num}")
    suspend fun updateProfile(@Path("user_num") userNum: Int, @Body request: UpdateProfileRequest): Response<UpdateProfileResponse>

    @PUT("/member/password/{user_num}")
    suspend fun updatePassword(@Path("user_num") userNum: String, @Body request: UpdatePasswordRequest): Response<UpdatePasswordResponse>



}
