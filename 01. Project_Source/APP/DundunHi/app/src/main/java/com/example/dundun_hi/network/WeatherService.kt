package com.example.dundun_hi.network

import android.util.Log
import com.example.dundun_hi.model.WeatherModel
import com.example.dundun_hi.util.GetBaseTimeUtil

class WeatherService(private val api: WeatherApi) {
    suspend fun fetch(lat: Double, lon: Double): WeatherModel {
        val (baseDate, baseTime) = GetBaseTimeUtil.getBase()
        val resp = api.getWeather(lat.toString(), lon.toString(), baseDate, baseTime)
   //     val resp = api.getWeather("37.5665", "126.9780", "20250530","1100" )
   //     val resp = api.getWeather(lat.toString(), lon.toString(), "20250530","1100" )

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
