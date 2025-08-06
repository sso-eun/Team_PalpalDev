package com.example.dundun_hi.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://dundunhi.onrender.com/"

    // 1) 로깅 인터셉터 설정
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2) 모든 요청에 적용할 OkHttpClient
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)  // 연결 타임아웃 30초
        .readTimeout(30, TimeUnit.SECONDS)     // 읽기 타임아웃 30초
        .writeTimeout(30, TimeUnit.SECONDS)    // 쓰기 타임아웃 30초
        .build()

    // 3) null도 직렬화하도록 하는 Gson
    private val gson = GsonBuilder()
        .serializeNulls()
        .create()

    // 4) 공통 Retrofit 인스턴스
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)                                  // <-- 로깅 클라이언트
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()


    // 5) 서비스 인터페이스들
    val memberService: MemberService =
        retrofit.create(MemberService::class.java)

    val weatherApi: WeatherApi =
        retrofit.create(WeatherApi::class.java)

    val codeAuthService: CodeAuthService =
        retrofit.create(CodeAuthService::class.java)

    val talkApi: TalkApi by lazy { retrofit.create(TalkApi::class.java) }


}
