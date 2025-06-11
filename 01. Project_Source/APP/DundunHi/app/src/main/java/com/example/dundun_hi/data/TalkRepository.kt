package com.example.dundun_hi.data


import android.content.Context
import android.net.Uri
import com.example.dundun_hi.network.RetrofitClient
import com.example.dundun_hi.network.TalkApi
import com.example.dundun_hi.data.TalkSendRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class TalkRepository(
    private val api: TalkApi = RetrofitClient.talkApi
) {
    suspend fun uploadAndSend(
        context: Context,
        userNum: Int,
        guardianId: Int,
        localUri: Uri
    ): String = withContext(Dispatchers.IO) {

        /* 1) Multipart 파일 생성 */
        val bytes = context.contentResolver.openInputStream(localUri)!!.use { it.readBytes() }
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = "talk_${System.currentTimeMillis()}.jpg",
            body = bytes.toRequestBody("image/*".toMediaType())
        )

        /* 2) 업로드 */
        val uploadRes = api.uploadTalkPhoto(userNum, part)
        require(uploadRes.rsCode == 200) { "Upload failed: ${uploadRes.message}" }

        /* 3) 보호자 전송 */
        val sendReq = TalkSendRequest(
            senderType = 0,
            senderId = userNum,
            receiverId = guardianId,
            imageUrl = uploadRes.filePath
        )
        val sendRes = api.sendTalk(sendReq)
        require(sendRes.rsCode == 200) { "Send failed: ${sendRes.message}" }

        /* 4) 최종 URL 반환 */
        uploadRes.filePath
    }
}
