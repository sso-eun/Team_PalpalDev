package com.example.dundun_hi.data


import android.content.Context
import android.net.Uri
import com.example.dundun_hi.network.RetrofitClient
import com.example.dundun_hi.network.TalkApi
import com.example.dundun_hi.data.TalkSendRequest
import com.example.dundun_hi.model.SharedPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

private const val BASE_URL = "https://dundunhi.onrender.com/"

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
    suspend fun fetchTalkList(userNum: Int): List<SharedPhoto> =
        api.getTalkList(mapOf("user_num" to userNum)).let { res ->
            require(res.rsCode == 200) { res.message }
            res.data.map { dto ->
                SharedPhoto(
                    fromMe = (dto.senderId == userNum),
                    remoteUrl = "https://port-0-dundunhi-manmbjl26e1dbc28.sel4.cloudtype.app/down/talk/${dto.talkId}",  // talk_id로 실제 이미지 URL 구성
                    localUri = null,
                    resId = null,
                    authorName = if (dto.senderType == 0) "우리딸" else "보호자"
                )
            }
        }
}
