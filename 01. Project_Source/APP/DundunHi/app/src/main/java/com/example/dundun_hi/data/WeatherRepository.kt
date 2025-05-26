package com.example.dundun_hi.data

import com.example.dundun_hi.network.WeatherService

class WeatherRepository(private val service: WeatherService) {
    suspend fun getWeather(lat: Double, lon: Double) =
        service.fetch(lat, lon)
}
//djdjdjdjdj