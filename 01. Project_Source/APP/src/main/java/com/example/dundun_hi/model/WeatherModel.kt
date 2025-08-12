package com.example.dundun_hi.model

data class WeatherModel(
    val currentTemp: String,
    val sky: String,
    val minTemp: String,
    val maxTemp: String)

{
    // 추가하세요 ↓
    val currentTempInt: Int
        get() = currentTemp.toDoubleOrNull()?.toInt() ?: 0

    val minTempInt: Int
        get() = minTemp.toDoubleOrNull()?.toInt() ?: 0

    val maxTempInt: Int
        get() = maxTemp.toDoubleOrNull()?.toInt() ?: 0
}



