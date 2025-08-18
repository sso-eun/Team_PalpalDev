package com.example.dundun_hi.network

import com.example.dundun_hi.data.CertListResponse
import com.example.dundun_hi.data.DateListResponse
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
import com.example.dundun_hi.network.AuthFamilyResponse        // 추가 필요한 import
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface MemberService {

    // ═══════════════════════════════════════════════════════════════════
    // 기본 회원 관리 API
    // ═══════════════════════════════════════════════════════════════════

    // 로그인
    @POST("member/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    // 회원가입
    @POST("/member/signup")
    suspend fun signup(@Body req: SignupRequest): Response<SignupResponse>

    // 아이디 찾기
    @POST("member/findid")
    suspend fun findId(@Body req: FindIdRequest): Response<FindIdResponse>

    // 회원 정보 조회
    @GET("member/getmember/{user_num}")
    suspend fun getMember(@Path("user_num") userNum: Int): MemberResponse

    // 프로필 업데이트
    @PUT("/member/profile/{user_num}")
    suspend fun updateProfile(@Path("user_num") userNum: Int, @Body request: UpdateProfileRequest): Response<UpdateProfileResponse>

    // 비밀번호 업데이트
    @PUT("/member/password/{user_num}")
    suspend fun updatePassword(@Path("user_num") userNum: String, @Body request: UpdatePasswordRequest): Response<UpdatePasswordResponse>

    // 프로필 부분 업데이트 (Map 방식)
    @PUT("member/profile/{user_num}")
    suspend fun updateProfilePartial(
        @Path("user_num") userNum: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): retrofit2.Response<UpdateProfileResponse>

    // ═══════════════════════════════════════════════════════════════════
    // 파일 업로드 API
    // ═══════════════════════════════════════════════════════════════════

    // 프로필 이미지 업로드
    @Multipart
    @POST("/upload/profile/{user_num}")
    suspend fun uploadProfileImage(
        @Path("user_num") userNum: Int,
        @Part file: okhttp3.MultipartBody.Part
    ): retrofit2.Response<com.example.dundun_hi.data.SimpleResponse>

    // 가족관계증명서 업로드 (은재 추가)
    @Multipart
    @POST("/upload/cert/{user_num}/{senior_num}")
    suspend fun uploadCertificate(
        @Path("user_num") userNum: Int,
        @Path("senior_num") seniorNum: Int,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<FileUploadResponse>

    // ═══════════════════════════════════════════════════════════════════
    // 일정 관리 API
    // ═══════════════════════════════════════════════════════════════════

    // 일정 추가
    @POST("/date/setdate")
    suspend fun setDate(@Body body: SetDateRequest): retrofit2.Response<SimpleResponse>

    // 일정 조회 - API 명세서에 맞게 쿼리 파라미터 방식으로 수정
    @GET("/date/getdate")
    suspend fun getDateList(
        @Query("user_num") userNum: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): retrofit2.Response<DateListResponse>

    // 기존 함수도 호환성을 위해 유지 (사용하지 않을 예정)
    @GET("/date/getdate/{user_num}")
    suspend fun getDateListOld(@Path("user_num") userNum: Int): retrofit2.Response<DateListResponse>

    // 24-08-17 추가
    @PUT("date/update/{user_date_no}")
    suspend fun updateAlert(
        @Path("user_date_no") alertId: String,
        @Body request: com.example.dundun_hi.data.UpdateAlertRequest
    ): retrofit2.Response<Unit> // 응답 본문이 없을 경우 Unit 사용

    @DELETE("date/delete/{user_data_no}")
    suspend fun deleteAlert(
        @Path("user_data_no") alertId: String
    ): Response<Unit> // 성공 여부만 확인하므로 응답 본문은 Unit

    // ═══════════════════════════════════════════════════════════════════
    // 인증서/가디언 관리 API
    // ═══════════════════════════════════════════════════════════════════

    // 인증서 목록 조회
    @GET("/cert/list")
    suspend fun getCertList(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<CertListResponse>

    // 가족관계증명서 조회 (은재 추가)
    @GET("cert/getlistByNum/{guardian_no}")
    suspend fun getAuthStatusByGuardianNo(
        @Path("guardian_no") guardianNo: Int
    ): Response<AuthFamilyResponse>

    // ═══════════════════════════════════════════════════════════════════
    // 회원 검색 API
    // ═══════════════════════════════════════════════════════════════════

    // 시니어 회원 검색 (은재 추가)
    @GET("searchmember")
    suspend fun searchMember(
        @Query("field") field: String,
        @Query("keyword") keyword: String
    ): Response<SearchMemberResponse>

    // ═══════════════════════════════════════════════════════════════════
    // FCM 토큰 관리 API
    // ═══════════════════════════════════════════════════════════════════

    // FCM 토큰 전송 (은재 수정 - 경로 변경)
    @POST("/member/updatetoken")
    suspend fun sendFcmToken(@Body req: FcmTokenRequest): retrofit2.Response<SimpleResponse>
}