package com.example.dundun_hi.ui.profile

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.MemberResponse
import com.example.dundun_hi.data.UpdatePasswordRequest
import com.example.dundun_hi.data.UpdatePasswordResponse
import com.example.dundun_hi.data.UpdateProfileRequest
import com.example.dundun_hi.data.UserRepository
import com.example.dundun_hi.network.RetrofitClient
import com.example.dundun_hi.network.RetrofitClient.memberService
import com.example.dundun_hi.util.distanceBetweenMeters
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
class ProfileViewModel(
    private val repository: UserRepository,
    private val userNum: Int,
    private val context: Context? = null
) : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private fun profileUrlOf(userNum: Int) =
        "${RetrofitClient.apiBaseUrl}profile/$userNum"

    private val appPrefs: SharedPreferences? =
        context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    val userNumber: Int get() = userNum
    val userConditionString: String get() = if (userCondition) "1" else "0"

    var userId by mutableStateOf("")
        private set
    var userTel by mutableStateOf("")
        private set
    var userProfileImg by mutableStateOf("")
        private set
    var userHomeLat by mutableStateOf(0.0)
        private set
    var userHomeLon by mutableStateOf(0.0)
        private set
    var userCondition by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var userType by mutableStateOf<Int?>(null)
        private set

    init {
        Log.d(TAG, "ProfileViewModel 초기화: userNum=$userNum, context=${context != null}")
        fetchUserFromServer()
    }

    fun fetchUserFromServer() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val response: MemberResponse = repository.getUserByNum(userNum)
                userId        = response.userId
                userTel       = response.userTel
                userHomeLat   = response.userHomeLat.toDoubleOrNull() ?: 0.0
                userHomeLon   = response.userHomeLot.toDoubleOrNull() ?: 0.0
                userCondition = (response.userCondition == 1)
                userType      = response.userType

                // 화면 표시는 고정 규칙
                userProfileImg = profileUrlOf(userNum)
            } catch (e: Exception) {
                Log.e(TAG, "fetchUserFromServer 오류", e)
                errorMessage = e.message ?: "알 수 없는 오류"
            } finally {
                isLoading = false
            }
        }
    }

    // ---- 팝업 억제 관련 (SharedPreferences 보존) ----
    fun setSuppressedDate(date: String) {
        appPrefs?.edit()?.putString("suppressed_date", date)?.apply()
    }

    fun getSuppressedDate(): String? {
        return appPrefs?.getString("suppressed_date", null)
    }

    // ---- 이미지 클리어 (기본 이미지로) ----
    fun clearSeniorProfileImage(onResult: (Boolean, String?) -> Unit) = viewModelScope.launch {
        try {
            Log.d(TAG, "이미지 클리어 시작: userNumber=$userNumber")
            val res = memberService.updateProfilePartial(
                userNumber,
                mapOf("user_profile_img" to "")
            )

            if (res.isSuccessful) {
                Log.d(TAG, "이미지 클리어 성공")
                userProfileImg = profileUrlOf(userNumber)
                onResult(true, null)
            } else {
                Log.e(TAG, "이미지 클리어 실패: ${res.code()} - ${res.message()}")
                val errorBody = res.errorBody()?.string()
                Log.e(TAG, "Error body: $errorBody")
                onResult(false, "이미지 클리어 실패: ${res.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "이미지 클리어 예외", e)
            onResult(false, "이미지 클리어 오류: ${e.message}")
        }
    }

    // ---- 이미지 업로드 전용 ----
    fun uploadSeniorProfileImage(
        uri: Uri,
        onResult: (Boolean, String?) -> Unit
    ) {
        val ctx = context ?: return onResult(false, "Context is null")
        viewModelScope.launch {
            try {
                Log.d(TAG, "이미지 업로드 시작: uri=$uri")

                // 1) 파일 준비 (cache에 복사)
                val input = ctx.contentResolver.openInputStream(uri)
                    ?: return@launch onResult(false, "파일을 열 수 없습니다.")

                val temp = File(ctx.cacheDir, "senior_${System.currentTimeMillis()}.jpg")
                temp.outputStream().use { output -> input.copyTo(output) }

                val reqFile = temp.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", temp.name, reqFile)

                // 2) 업로드
                Log.d(TAG, "파일 업로드 요청: userNum=$userNum")
                val upRes = memberService.uploadProfileImage(userNum, part)
                val body = upRes.body()

                Log.d(TAG, "업로드 응답: ${upRes.code()}, body=$body")

                if (!upRes.isSuccessful || body?.rsCode != 200 || body.path.isNullOrBlank()) {
                    val errorMsg = "업로드 실패: ${body?.message ?: upRes.message()}"
                    Log.e(TAG, errorMsg)
                    return@launch onResult(false, errorMsg)
                }

                val uploadedUrl = body.path!!
                Log.d(TAG, "업로드 성공, 받은 path: $uploadedUrl")

                // 3) DB에 업로드된 경로 저장
                Log.d(TAG, "프로필 업데이트 시작: userNum=$userNum, path=$uploadedUrl")
                val updateData = mapOf("user_profile_img" to uploadedUrl)
                val putRes = memberService.updateProfilePartial(userNum, updateData)

                if (!putRes.isSuccessful) {
                    val errorBody = putRes.errorBody()?.string()
                    val errorMsg = "프로필 업데이트 실패: ${putRes.code()} - ${putRes.message()}"
                    Log.e(TAG, "$errorMsg, errorBody: $errorBody")
                    return@launch onResult(false, errorMsg)
                }

                Log.d(TAG, "프로필 업데이트 성공")

                // 4) 화면 표시는 고정 규칙 (또는 실제 업로드된 URL 사용)
                userProfileImg = profileUrlOf(userNum)
                onResult(true, null)

            } catch (e: Exception) {
                Log.e(TAG, "이미지 업로드 예외", e)
                onResult(false, "이미지 업로드 오류: ${e.message}")
            }
        }
    }

    // ---- 이미지 제외 Partial 업데이트 (전화번호/위치/상태만) ----
    fun updateProfileWithoutImage(
        newTel: String,
        newHomeLat: Double,
        newHomeLon: Double,
        isOuting: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "프로필 업데이트 시작 (이미지 제외): userNum=$userNum")
                Log.d(TAG, "업데이트 데이터: tel=$newTel, lat=$newHomeLat, lon=$newHomeLon, condition=$isOuting")

                val updateData = mapOf(
                    "user_tel" to newTel,
                    "user_home_lat" to newHomeLat.toString(),
                    "user_home_lot" to newHomeLon.toString(),
                    "user_condition" to (if (isOuting) "1" else "0")
                )

                val resp = memberService.updateProfilePartial(userNum, updateData)

                if (resp.isSuccessful) {
                    Log.d(TAG, "프로필 업데이트 성공")
                    // 로컬 상태 갱신
                    userTel = newTel
                    userHomeLat = newHomeLat
                    userHomeLon = newHomeLon
                    userCondition = isOuting
                    errorMessage = null
                    onSuccess()
                } else {
                    val errorBody = resp.errorBody()?.string()
                    val errorMsg = "프로필 업데이트 실패: ${resp.code()} - ${resp.message()}"
                    Log.e(TAG, "$errorMsg, errorBody: $errorBody")
                    errorMessage = errorMsg
                }
            } catch (e: Exception) {
                Log.e(TAG, "프로필 업데이트 예외", e)
                errorMessage = "프로필 업데이트 오류: ${e.message}"
            }
        }
    }

    // ---- 과거 호출 호환: 항상 이미지 제외로 위임 ----
    fun updateProfile(
        newTel: String,
        newProfileImg: String, // 무시됨
        newHomeLat: Double,
        newHomeLon: Double,
        isOuting: Boolean,
        onSuccess: () -> Unit
    ) = updateProfileWithoutImage(newTel, newHomeLat, newHomeLon, isOuting, onSuccess)

    // ---- 위치 기반 자동 상태 업데이트 ----
    fun autoUpdateConditionBasedOnLocation(
        currentLat: Double,
        currentLon: Double,
        thresholdInMeters: Double = 100.0,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val dist = distanceBetweenMeters(userHomeLat, userHomeLon, currentLat, currentLon)
            val shouldBeOuting = (dist > thresholdInMeters)
            if (shouldBeOuting == userCondition) {
                onResult(false)
                return@launch
            }
            updateProfileWithoutImage(
                newTel = userTel,
                newHomeLat = userHomeLat,
                newHomeLon = userHomeLon,
                isOuting = shouldBeOuting
            ) { onResult(true) }
        }
    }

    fun updatePassword(
        userNum: String,
        currentPw: String,
        newPw: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val requestBody = UpdatePasswordRequest(current_pw = currentPw, new_pw = newPw)
                val response = memberService.updatePassword(userNum, requestBody)
                if (response.isSuccessful) {
                    val body: UpdatePasswordResponse? = response.body()
                    if (body != null) onResult(true, body.message)
                    else onResult(false, "서버 응답이 비어 있습니다.")
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "알 수 없는 오류가 발생했습니다."
                    onResult(false, errorMsg)
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun isHomeLocationEmpty(): Boolean {
        return userHomeLat == 0.0 || userHomeLon == 0.0
    }

    // 미리보기용 - 실제로는 아무것도 하지 않음
    fun onProfileImageSelected(uriString: String) {
        // no-op - 실제 업로드는 uploadSeniorProfileImage()로만 처리
    }

    fun setHomeLocation(
        userNum: Int,
        userTel: String,
        userProfileImg: String,
        userCondition: String,
        newLat: String,
        newLot: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = UpdateProfileRequest(
                    userTel = userTel,
                    userProfileImg = userProfileImg,
                    userHomeLat = newLat,
                    userHomeLot = newLot,
                    userCondition = userCondition
                )
                val response = memberService.updateProfile(userNum, request)
                callback(response.isSuccessful)
            } catch (e: Exception) {
                Log.e(TAG, "setHomeLocation 오류", e)
                callback(false)
            }
        }
    }
}