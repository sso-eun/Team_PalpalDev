package com.example.dundun_hi.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

//d요청 인터페이스
interface ClovaSpeechApiService {

    /**
     * @Multipart: 파일과 같은 여러 종류의 데이터를 함께 보낼 때 사용
     * suspend: 이 함수는 작업이 오래 걸릴 수 있으니, 비동기(백그라운드)로 처리해야 한다는 표시
     */
    @Multipart
    @POST // <-- API Endpoint의 뒷부분은 Client에서 지정할 것이므로 여기서는 비워둠
    suspend fun recognizeSpeech(
        @Url fullUrl: String,                               // <-- 전체 URL을 직접 전달받도록 변경
        @Header("X-CLOVASPEECH-API-KEY") secretKey: String, // <-- 이렇게 헤더 하나만 받도록 수정! - 로컬이랑 스토리지 버전 다름!!!!!!!!!!!
        @Part params: MultipartBody.Part,
        @Part media: MultipartBody.Part
    ): Response<ClovaSpeechResponse>
}