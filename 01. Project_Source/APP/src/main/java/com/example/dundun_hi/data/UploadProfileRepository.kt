package com.example.dundun_hi.data

import android.content.Context
import android.net.Uri
import com.example.dundun_hi.network.MemberService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import kotlin.io.copyTo
import kotlin.io.use
import kotlin.onFailure
import kotlin.onSuccess

class UploadProfileRepository(private val api: MemberService) {

    // 1. 서버에 이미지 업로드만 (파일 자체 업로드)
    suspend fun uploadImageFileOnly(
        context: Context,
        uri: Uri,
        userNum: Int
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)!!
                val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { inputStream.copyTo(it) }

                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

                val response = api.uploadProfileImage(userNum, part)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("업로드 실패: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // 2. 업로드 후 DB 업데이트까지 같이 진행
    suspend fun uploadAndUpdateProfile(
        context: Context,
        uri: Uri,
        userNum: Int,
        currentTel: String,
        currentLat: Double,
        currentLon: Double,
        currentCondition: Boolean,
        memberService: MemberService,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uploadResult = uploadImageFileOnly(context, uri, userNum)
        uploadResult.onSuccess {
            // 🔥 TalkApi처럼 예상 경로 사용
            val imageUrl = "https://dundunhi.onrender.com/upload/profile/${userNum}.jpg"

            val request = UpdateProfileRequest(
                userTel = currentTel,
                userProfileImg = imageUrl,
                userHomeLat = currentLat.toString(),
                userHomeLot = currentLon.toString(),
                userCondition = if (currentCondition) "1" else "0"
            )

            val response = memberService.updateProfile(userNum, request)
            if (response.isSuccessful) {
                onSuccess()
            } else {
                onError("DB 업데이트 실패: ${response.code()}")
            }
        }.onFailure {
            onError(it.message ?: "업로드 실패")
        }
    }
}
