// 파일 경로: app/src/main/java/com/example/dundun_hi/data/UserRepository.kt
package com.example.dundun_hi.data

/**
 * 회원 정보를 가져오는 추상화된 인터페이스
 */
interface UserRepository {
    suspend fun getUserByNum(userNum: Int): MemberResponse

    suspend fun updateUserProfile(userNum: Int, request:UpdateProfileRequest): UpdateProfileResponse
}
