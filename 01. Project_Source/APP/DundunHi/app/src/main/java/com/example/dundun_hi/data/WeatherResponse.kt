package com.example.dundun_hi.network

import com.example.dundun_hi.model.WeatherModel

/**
 * Retrofit이 JSON을 파싱할 때 사용할 응답용 DTO
 * {
 *   "status": 200,
 *   "data": { currentTemp: "...", sky: "...", minTemp: "...", maxTemp: "..." }
 * }
 */
data class WeatherResponse(
    val status: Int,
    val data: WeatherModel
)
