package com.example.dundun_hi.network

import com.example.dundun_hi.data.CultureCenterService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://port-0-dundunhi-manmbjl26e1dbc28.sel4.cloudtype.app/"

    // 1) 로깅 인터셉터 설정
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2) 모든 요청에 적용할 OkHttpClient
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
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

    val cultureCenterService: CultureCenterService =
        retrofit.create(CultureCenterService::class.java)

    private fun toAbsoluteUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return if (path.startsWith("http")) path
        else RetrofitClient.apiBaseUrl + path.removePrefix("/")
    }


    // 외부에서 사용할 수 있도록 공개 getter
    val apiBaseUrl: String
        get() = BASE_URL

}