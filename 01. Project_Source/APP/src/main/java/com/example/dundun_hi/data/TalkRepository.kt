package com.example.dundun_hi.data

import android.content.Context
import android.net.Uri
import android.util.Log
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
        senderId: Int,    // userNum → senderId로 변경
        receiverId: Int,  // guardianId → receiverId로 변경
        localUri: Uri
    ): String = withContext(Dispatchers.IO) {

        Log.d("TalkRepository", "업로드 시작: senderId=$senderId, receiverId=$receiverId")

        /* 1) Multipart 파일 생성 */
        val bytes = context.contentResolver.openInputStream(localUri)!!.use { it.readBytes() }
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = "talk_${System.currentTimeMillis()}.jpg",
            body = bytes.toRequestBody("image/*".toMediaType())
        )

        /* 2) 업로드 */
        Log.d("TalkRepository", "파일 업로드 중...")
        val uploadRes = api.uploadTalkPhoto(senderId, part)
        Log.d("TalkRepository", "업로드 응답: rsCode=${uploadRes.rsCode}, filePath=${uploadRes.filePath}")
        require(uploadRes.rsCode == 200) { "Upload failed: ${uploadRes.message}" }

        /* 3) 대화 전송 */
        val currentTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())

        val sendReq = TalkSendRequest(
            senderType = 0,
            senderId = senderId,
            receiverId = receiverId,
            imageUrl = uploadRes.filePath
        )
        Log.d("TalkRepository", "대화 전송 중: $sendReq")
        val sendRes = api.sendTalk(sendReq)
        Log.d("TalkRepository", "전송 응답: rsCode=${sendRes.rsCode}, message=${sendRes.message}")
        require(sendRes.rsCode == 200) { "Send failed: ${sendRes.message}" }

        /* 4) 최종 URL 반환 */
        uploadRes.filePath
    }

    suspend fun fetchTalkList(userNum: Int): List<SharedPhoto> = withContext(Dispatchers.IO) {
        Log.d("TalkRepository", "대화 목록 조회: userNum=$userNum")
        val res = api.getTalkList(mapOf("user_num" to userNum))
        Log.d("TalkRepository", "조회 응답: rsCode=${res.rsCode}, 데이터 개수=${res.data.size}")

        require(res.rsCode == 200) { res.message }

        res.data.map { dto ->
            Log.d("TalkRepository", "DTO: talkId=${dto.talkId}, senderId=${dto.senderId}, imageUrl=${dto.imageUrl}")

            // 상대 경로를 완전한 URL로 변환
            val fullImageUrl = if (dto.imageUrl.startsWith("http")) {
                dto.imageUrl
            } else {
                "${BASE_URL}${dto.imageUrl}"
            }

            Log.d("TalkRepository", "변환된 URL: $fullImageUrl")

            SharedPhoto(
                fromMe = (dto.senderId == userNum), // Int 비교로 변경
                senderUserId = dto.senderId.toString(),  // String으로 변환
                senderName = dto.senderId.toString(),    // 필요에 따라 이름값으로 수정
                remoteUrl = fullImageUrl,
                localUri = null,
                resId = null,
                sendAt = dto.sendAt
            )



        }
    }
}