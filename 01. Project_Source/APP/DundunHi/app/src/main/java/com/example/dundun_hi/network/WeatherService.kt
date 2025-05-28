package com.example.dundun_hi.network

import android.util.Log
import com.example.dundun_hi.model.WeatherModel
import com.example.dundun_hi.util.GetBaseTimeUtil

class WeatherService(private val api: WeatherApi) {
    suspend fun fetch(lat: Double, lon: Double): WeatherModel {
        val (baseDate, baseTime) = GetBaseTimeUtil.getBase()
//        val resp = api.getWeather(lat.toString(), lon.toString(), baseDate, baseTime)
        val resp = api.getWeather("37.5665", "126.9780", "20250527","0500" )

       // val body = resp.body()!!

        val body = resp.body()
            ?: throw IllegalStateException("서버 응답이 비어 있습니다")

        // ⑤ 로그는 return 전에 찍어야 도달
        Log.d("WeatherService", "body = $body")


        return WeatherModel(
            currentTemp = body.data.currentTemp,
            sky = body.data.sky,
            minTemp = body.data.minTemp,
            maxTemp = body.data.maxTemp
        )
        Log.e("WeatherService",resp.body().toString())
    }
}
//class WeatherService(private val api: WeatherApi) {
//    suspend fun fetch(lat: Double, lon: Double): WeatherModel {
//        val baseDate = "20250527"    // 원하는 발표 날짜 (yyyyMMdd)
//        val baseTime = "1420"        // 원하는 발표 시각 (HHmm)
//        val testLat  = 37    // 원하는 위도 예시 (서울 중심)
//        val testLon  = 126     // 원하는 경도 예시
//
//        // 실제 호출: 파라미터를 모두 하드코딩 값으로 넘김
//        val resp = api.getWeather(
//            testLat,
//            testLon,
//            baseDate,
//            baseTime
//        )
//
//        if (!resp.isSuccessful) throw HttpException(resp)
//        val body = resp.body() ?: throw IllegalStateException("body is null")
//        return body.data
//    }
//}
