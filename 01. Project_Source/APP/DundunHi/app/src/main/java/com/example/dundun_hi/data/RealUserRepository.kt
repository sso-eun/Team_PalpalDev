package com.example.dundun_hi.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.dundun_hi.network.MemberService
import com.example.dundun_hi.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class RealUserRepository : UserRepository {

    private val memberService: MemberService = RetrofitClient.memberService

    override suspend fun getUserByNum(userNum: Int): MemberResponse {
        return memberService.getMember(userNum)
    }

    override suspend fun updateUserProfile(
        userNum: Int,
        request: UpdateProfileRequest
    ): UpdateProfileResponse {
        val response = memberService.updateProfile(userNum, request)
        if (response.isSuccessful) return response.body()!!
        else throw Exception("HTTP ${response.code()} : 프로필 수정 실패")
    }

    suspend fun updateUserProfilePartial(
        userNum: Int,
        body: Map<String, Any?>
    ): UpdateProfileResponse {
        val response = memberService.updateProfilePartial(userNum, body)
        if (response.isSuccessful) return response.body()!!
        throw Exception("HTTP ${response.code()} : 프로필 수정 실패")
    }

    // 이미지 업로드 → 서버가 message에 경로/URL 반환
    suspend fun uploadProfileImageToServer(
        context: Context,
        userNum: Int,
        uri: Uri
    ): String {
        val cr = context.contentResolver

        val fileName = queryDisplayName(cr, uri) ?: "profile_${System.currentTimeMillis()}.jpg"
        val mime = cr.getType(uri) ?: "image/jpeg"

        val bytes = cr.openInputStream(uri)!!.use { it.readBytes() }
        val reqBody = bytes.toRequestBody(mime.toMediaType())

        val part = MultipartBody.Part.createFormData("file", fileName, reqBody)

        val resp = memberService.uploadProfileImage(userNum, part) // ✅ 여기!
        if (!resp.isSuccessful) throw RuntimeException("Upload failed: HTTP ${resp.code()}")
        val body = resp.body() ?: throw RuntimeException("Upload failed: empty body")
        val p = body.path ?: throw RuntimeException("Upload failed: empty path")
        return p
    }

    private fun queryDisplayName(cr: ContentResolver, uri: Uri): String? {
        val cols = arrayOf(OpenableColumns.DISPLAY_NAME)
        return cr.query(uri, cols, null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    }
}
