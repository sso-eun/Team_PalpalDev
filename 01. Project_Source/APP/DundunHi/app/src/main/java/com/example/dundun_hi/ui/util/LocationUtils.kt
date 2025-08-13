// app/src/main/java/com/example/dundun_hi/util/LocationUtils.kt

package com.example.dundun_hi.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 두 위경도 간의 거리를 계산 (단위: 미터).
 * 하버사인(Haversine) 공식을 사용.
 *
 * @param lat1 첫 번째 지점 위도 (degrees)
 * @param lon1 첫 번째 지점 경도 (degrees)
 * @param lat2 두 번째 지점 위도 (degrees)
 * @param lon2 두 번째 지점 경도 (degrees)
 * @return 두 지점 간 거리 (meters)
 */
fun distanceBetweenMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371000.0 // 지구 반지름 (meters)
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}
