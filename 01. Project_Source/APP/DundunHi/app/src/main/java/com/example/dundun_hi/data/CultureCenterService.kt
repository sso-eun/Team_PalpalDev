package com.example.dundun_hi.data

import retrofit2.http.GET
import retrofit2.http.Query

// 1. 가독성을 위해 인터페이스 이름을 Service로 변경하는 것을 추천합니다.
interface CultureCenterService {
    @GET("culture_center")
    suspend fun getCultureCenter(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
        // 2. API가 단일 객체를 반환하므로 List가 아닌 CultureCenterResponse를 반환 타입으로 지정합니다.
    ): CultureCenterResponse
}