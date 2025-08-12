package com.example.dundun_hi.data

import com.example.dundun_hi.network.MemberService
import com.example.dundun_hi.network.RetrofitClient
import retrofit2.Response

class FindIdRepository(
    private val service: MemberService = RetrofitClient.memberService
) {
    suspend fun findId(req: FindIdRequest): Response<FindIdResponse> {
        return service.findId(req)
    }
}
