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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val BASE_URL = "https://dundunhi.onrender.com/"

class TalkRepository(
    private val api: TalkApi = RetrofitClient.talkApi
) {

    suspend fun uploadAndSend(
        context: Context,
        senderId: Int,      // 현재 사용자 (발신자)
        receiverId: Int,    // 상대방 (수신자)
        localUri: Uri,
        senderType: Int = 0 // 기본값: 시니어, 필요시 외부에서 전달
    ): String = withContext(Dispatchers.IO) {

        Log.d("TalkRepository", "🚀 업로드 시작: senderId=$senderId, receiverId=$receiverId, senderType=$senderType")

        try {
            /* 1) Multipart 파일 생성 */
            Log.d("TalkRepository", "📁 파일 준비 중...")
            val bytes = context.contentResolver.openInputStream(localUri)!!.use { it.readBytes() }
            Log.d("TalkRepository", "📏 파일 크기: ${bytes.size} bytes")

            val part = MultipartBody.Part.createFormData(
                name = "file",
                filename = "talk_${System.currentTimeMillis()}.jpg",
                body = bytes.toRequestBody("image/*".toMediaType())
            )

            /* 2) 업로드 */
            Log.d("TalkRepository", "📤 이미지 업로드 요청 시작...")
            val uploadRes = api.uploadTalkPhoto(senderId, part)
            Log.d("TalkRepository", "📤 업로드 응답: rsCode=${uploadRes.rsCode}, message=${uploadRes.message}")
            Log.d("TalkRepository", "📤 업로드된 파일 경로: ${uploadRes.filePath}")

            require(uploadRes.rsCode == 200) { "Upload failed: ${uploadRes.message}" }

            // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
            //           ★★★ 바로 이 코드 한 줄! ★★★
            // filePath가 null이거나 비어있으면 여기서 에러를 발생시키고 중단합니다.
            require(!uploadRes.filePath.isNullOrBlank()) { "Server returned null or empty file path" }
            // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

            /* 3) talk/send API 호출 */
            val sendReq = TalkSendRequest(
                senderType = senderType,
                senderId = senderId,
                receiverId = receiverId,
                imageUrl = uploadRes.filePath // 이제 filePath는 null이 아님이 보장됩니다.
            )

            Log.d("TalkRepository", "💬 talk/send 요청 시작...")
            Log.d("TalkRepository", "💬 요청 데이터: $sendReq")
            val sendRes = api.sendTalk(sendReq)
            Log.d("TalkRepository", "💬 talk/send 응답: rsCode=${sendRes.rsCode}, message=${sendRes.message}")

            require(sendRes.rsCode == 200) { "Send failed: ${sendRes.message}" }

            Log.d("TalkRepository", "🎉 전체 프로세스 완료! 최종 URL: ${uploadRes.filePath}")

            /* 4) 최종 URL 반환 */
            uploadRes.filePath // 여기서도 filePath는 null이 아님이 보장됩니다.

        } catch (e: Exception) {
            Log.e("TalkRepository", "💥 에러 발생: ${e.message}", e)

            when (e) {
                is java.net.ConnectException -> Log.e("TalkRepository", "🌐 네트워크 연결 실패")
                is java.net.SocketTimeoutException -> Log.e("TalkRepository", "⏰ 네트워크 타임아웃")
                is IllegalArgumentException -> Log.e("TalkRepository", "📝 잘못된 파라미터: ${e.message}")
                is java.io.IOException -> Log.e("TalkRepository", "📁 파일 I/O 에러: ${e.message}")
                else -> Log.e("TalkRepository", "🔥 알 수 없는 에러: ${e.javaClass.simpleName}")
            }
            throw e
        }
    }

    suspend fun fetchTalkList(userNum: Int): List<SharedPhoto> = withContext(Dispatchers.IO) {
        try {
            Log.d("TalkRepository", "📋 대화 목록 조회 시작: userNum=$userNum")
            val res = api.getTalkList(mapOf("user_num" to userNum))
            Log.d("TalkRepository", "📋 대화 목록 응답: rsCode=${res.rsCode}, 데이터 수=${res.data.size}")

            require(res.rsCode == 200) { res.message }

            val photos = res.data.map { dto ->
                Log.d("TalkRepository", "📷 사진 데이터: talkId=${dto.talkId}, senderId=${dto.senderId}, senderType=${dto.senderType}, sendAt=${dto.sendAt}")

                // 작성자 이름을 동적으로 결정
                val authorName = when {
                    dto.senderId == userNum -> "나"
                    else -> "상대방"
                }

                // send_at 문자열을 LocalDateTime으로 파싱
                val sendDateTime = try {
                    val dateString = dto.sendAt
                    Log.d("TalkRepository", "📅 날짜 파싱 시도: $dateString")

                    when {
                        // ISO 8601 형식 (예: 2025-01-15T14:30:00Z 또는 2025-01-15T14:30:00)
                        dateString.contains('T') -> {
                            if (dateString.endsWith('Z')) {
                                LocalDateTime.parse(dateString.removeSuffix("Z"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            } else {
                                LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            }
                        }
                        // MySQL DATETIME 형식 (예: 2025-01-15 14:30:00)
                        dateString.contains(' ') -> {
                            LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        }
                        // 기타 형식들 시도
                        else -> {
                            LocalDateTime.parse(dateString)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("TalkRepository", "날짜 파싱 실패: ${dto.sendAt}, 에러: ${e.message}")
                    Log.w("TalkRepository", "현재 시간으로 대체합니다.")
                    LocalDateTime.now()
                }

                SharedPhoto(
                    fromMe = (dto.senderId == userNum),
                    remoteUrl = "https://port-0-dundunhi-manmbjl26e1dbc28.sel4.cloudtype.app/down/talk/${dto.talkId}",
                    localUri = null,
                    resId = null,
                    authorName = authorName,
                    sendAt = sendDateTime
                )
            }

            Log.d("TalkRepository", "📋 대화 목록 조회 완료: ${photos.size}개 사진")

            // 🎯 기본적으로 최신순 정렬하여 반환 (디폴트 보장)
            val sortedPhotos = photos.sortedByDescending { it.sendAt }
            Log.d("TalkRepository", "📅 최신순 정렬 완료")

            sortedPhotos

        } catch (e: Exception) {
            Log.e("TalkRepository", "📋 대화 목록 조회 실패: ${e.message}", e)

            // 에러 타입별 상세 로그
            when (e) {
                is java.net.ConnectException -> Log.e("TalkRepository", "🌐 네트워크 연결 실패 (목록 조회)")
                is java.net.SocketTimeoutException -> Log.e("TalkRepository", "⏰ 네트워크 타임아웃 (목록 조회)")
                else -> Log.e("TalkRepository", "🔥 알 수 없는 에러 (목록 조회): ${e.javaClass.simpleName}")
            }

            emptyList()
        }
    }
}