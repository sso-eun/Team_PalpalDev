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
import com.example.dundun_hi.data.SimpleResponse
import com.example.dundun_hi.data.SetDateRequest
import com.example.dundun_hi.data.FcmTokenRequest
import com.example.dundun_hi.data.FileUploadResponse        // 은재 추가 - 가족관계증명서
import com.example.dundun_hi.data.SearchMemberResponse
import com.example.dundun_hi.data.VerifyMemberRequest
import com.example.dundun_hi.data.VerifySeniorRequest
import com.example.dundun_hi.data.VerifySeniorResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query     //은재 추가


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

    // 25-08-02 은재 추가: 가족관계증명서 업로드
    @Multipart
    @POST("/upload/cert/{user_num}/{senior_num}")
    suspend fun uploadCertificate(
        @Path("user_num") userNum: Int,
        @Path("senior_num") seniorNum: Int,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<FileUploadResponse>

    // 가족관계증명서 조회
    @GET("cert/getlistByNum/{guardian_no}")
    suspend fun getAuthStatusByGuardianNo(
        @Path("guardian_no") guardianNo: Int
    ): Response<AuthFamilyResponse>

    // 시니어 회원 검색을 위한 함수 추가
    @GET("searchmember")
    suspend fun searchMember(
        @Query("field") field: String,
        @Query("keyword") keyword: String
    ): Response<SearchMemberResponse> // 방금 만든 데이터 클래스 사용

    // 회원 정보 검증 (user_num, user_tel)

    // ----------------------------------------------

    @Multipart
    @POST("/upload/profile/{user_num}")
    suspend fun uploadProfileImage(
        @Path("user_num") userNum: Int,
        @Part file: okhttp3.MultipartBody.Part
    ): retrofit2.Response<com.example.dundun_hi.data.SimpleResponse>

    // 일정 추가 (set_date)
    @POST("/date/setdate")
    suspend fun setDate(@Body body: SetDateRequest): retrofit2.Response<SimpleResponse>

    @POST("/member/fcm_token")
    suspend fun sendFcmToken(@Body req: FcmTokenRequest): retrofit2.Response<SimpleResponse>

}
