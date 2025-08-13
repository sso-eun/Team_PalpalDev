package com.example.dundun_hi.network

// (수정) 우리가 만든 서비스 인터페이스를 임포트합니다.
import com.example.dundun_hi.data.CultureCenterService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://port-0-dundunhi-manmbjl26e1dbc28.sel4.cloudtype.app/"

    // 1) 로깅 인터셉터
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2) OkHttpClient
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // 3) Gson
    private val gson = GsonBuilder()
        .serializeNulls()
        .create()

    // 4) Retrofit 인스턴스
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // 5) API 서비스들
    val memberService: MemberService =
        retrofit.create(MemberService::class.java)

    val weatherApi: WeatherApi =
        retrofit.create(WeatherApi::class.java)

    val codeAuthService: CodeAuthService =
        retrofit.create(CodeAuthService::class.java)

    val talkApi: TalkApi =
        retrofit.create(TalkApi::class.java)

    // (수정) CultureCenterRequest -> CultureCenterService 로 변경
    val cultureCenterService: CultureCenterService =
        retrofit.create(CultureCenterService::class.java)

    // 절대 경로 변환용
    private fun toAbsoluteUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return if (path.startsWith("http")) path
        else apiBaseUrl + path.removePrefix("/")
    }

    val apiBaseUrl: String
        get() = BASE_URL
}