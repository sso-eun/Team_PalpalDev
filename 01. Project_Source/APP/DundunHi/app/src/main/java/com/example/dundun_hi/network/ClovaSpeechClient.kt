package com.example.dundun_hi.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Clova Speech API 통신을 총괄하는 매니저 객체
 */
object ClovaSpeechClient {

    const val INVOKE_URL = "https://clovaspeech-gw.ncloud.com/external/v1/12439/e68ffa0445895a132ce42506f77f9643da499556fe36dbac235cc713e871c5a2/recognizer/upload"
    const val SECRET_KEY = "7d3045d493674932948d52e8879b5e3e"


    // 통신 과정을 로그로 확인하기 위한 인터셉터 (디버깅용)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttp 클라이언트 생성 (위의 로깅 인터셉터 포함)
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Retrofit 객체 생성
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://clovaspeech-gw.ncloud.com/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 위에서 만든 '메뉴판(ApiService)'의 구현체를 Retrofit을 통해 생성합니다.
    val apiService: ClovaSpeechApiService = retrofit.create(ClovaSpeechApiService::class.java)
}