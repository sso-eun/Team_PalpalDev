package com.example.dundun_hi.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String
    ): Response<WeatherResponse>
}
