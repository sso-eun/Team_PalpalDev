package com.example.dundun_hi.network

import com.example.dundun_hi.model.WeatherModel
import com.example.dundun_hi.util.GetBaseTimeUtil

class WeatherService(private val api: WeatherApi) {
    suspend fun fetch(lat: Double, lon: Double): WeatherModel {
        val (baseDate, baseTime) = GetBaseTimeUtil.getBase()
        val resp = api.getWeather(lat, lon, baseDate, baseTime)
        val body = resp.body()!!
        return WeatherModel(
            currentTemp = body.data.currentTemp,
            sky = body.data.sky,
            minTemp = body.data.minTemp,
            maxTemp = body.data.maxTemp
        )
    }
}
