package com.example.dundun_hi.data

import com.example.dundun_hi.network.MemberService
import com.example.dundun_hi.network.RetrofitClient

/**
 * RealUserRepository는 실제 서버에서 데이터를 받아오는 구현체.
 * RetrofitClient.memberService를 통해 HTTP 호출을 수행한다.
 */
class RealUserRepository : UserRepository {
    // RetrofitClient 안에 미리 만들어 둔 memberService 인스턴스를 바로 사용
    private val memberService: MemberService = RetrofitClient.memberService

    override suspend fun getUserByNum(userNum: Int): MemberResponse {
        return memberService.getMember(userNum)
    }

    override suspend fun updateUserProfile(
        userNum: Int,
        request: UpdateProfileRequest
    ): UpdateProfileResponse {
        val response = memberService.updateProfile(userNum, request)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("HTTP ${response.code()} : 프로필 수정 실패")
        }
    }


    suspend fun updateUserProfilePartial(
        userNum: Int,
        body: Map<String, Any?>
    ): UpdateProfileResponse {
        val response = RetrofitClient.memberService.updateProfilePartial(userNum, body)
        if (response.isSuccessful) return response.body()!!
        throw Exception("HTTP ${response.code()} : 프로필 수정 실패")
    }
}

