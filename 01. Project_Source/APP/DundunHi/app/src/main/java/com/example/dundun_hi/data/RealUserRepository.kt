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
}
