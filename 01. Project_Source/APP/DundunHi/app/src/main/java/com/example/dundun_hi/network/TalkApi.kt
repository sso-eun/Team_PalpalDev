package com.example.dundun_hi.network

import com.example.dundun_hi.data.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface TalkApi {

    @Multipart
    @POST("/upload/talk/{user_num}")
    suspend fun uploadTalkPhoto(
        @Path("user_num") userNum: Int,
        @Part file: MultipartBody.Part
    ): UploadTalkResponse

    @POST("/talk/send")
    suspend fun sendTalk(
        @Body body: TalkSendRequest
    ): SimpleResponse
}
