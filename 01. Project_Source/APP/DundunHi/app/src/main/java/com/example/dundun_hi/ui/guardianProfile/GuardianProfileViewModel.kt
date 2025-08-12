package com.example.dundun_hi.ui.guardianProfile

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.UpdateProfileRequest
import com.example.dundun_hi.data.UserRepository
import com.example.dundun_hi.network.RetrofitClient
import com.example.dundun_hi.network.RetrofitClient.memberService
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Locale

class GuardianProfileViewModel(
    private val repository: UserRepository,
    val guardianUserNum: Int,
    private val context: Context? = null
) : ViewModel() {

    companion object {
        private const val TAG = "GuardianProfileViewModel"
    }

    /** 보호자 정보 */
    var guardianId by mutableStateOf("")
        private set
    var guardianTel by mutableStateOf("")
        private set
    var guardianProfileImg by mutableStateOf<String?>(null) // 화면에서는 항상 절대 URL로 유지
        private set

    /** 어르신 정보 */
    var seniorUserNum by mutableStateOf<Int?>(null)
        private set
    var seniorId by mutableStateOf("")
        private set
    var seniorTel by mutableStateOf("")
        private set
    var seniorProfileImg by mutableStateOf("")
        private set
    var seniorHomeLat by mutableStateOf(0.0)
        private set
    var seniorHomeLon by mutableStateOf(0.0)
        private set
    var seniorCondition by mutableStateOf(false)
        private set
    var seniorAddress by mutableStateOf("")
        private set

    /** 상태 관리 */
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // 추가
    var imageVersion by mutableStateOf(0L)
        private set

    private fun bumpImageVersion() {
        imageVersion = System.currentTimeMillis()
    }

    // URL 생성 시 캐시버스트 토큰 포함
    private fun createProfileImageUrl(userNum: Int): String {
        val baseUrl = RetrofitClient.apiBaseUrl.let { if (it.endsWith("/")) it else "$it/" }
        return "${baseUrl}down/profile/$userNum?v=$imageVersion"
    }

    fun onProfileImageSelected(uri: String) {
        // 미리보기: 로컬 URI 그대로 보여주기 (AsyncImage가 content:// 도 표시 가능)
        guardianProfileImg = uri
    }

    init {
        fetchGuardianAndSeniorInfo()
    }

    /**
     * 1. cert_list API로 어르신 고유번호 가져오기
     * 2. 보호자 정보 가져오기
     * 3. 어르신 정보 가져오기
     * 4. 어르신 집 주소를 위도/경도에서 주소로 변환
     */
    private fun fetchGuardianAndSeniorInfo() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                // 1) cert_list
                val certResponse = RetrofitClient.memberService.getCertList(
                    page = 1,
                    limit = 10
                )
                if (!certResponse.isSuccessful || certResponse.body() == null) {
                    throw Exception("인증서 목록을 가져올 수 없습니다.")
                }
                val certList = certResponse.body()!!.results
                val seniorCert = certList.find { it.guardian_no == guardianUserNum }
                    ?: throw Exception("등록된 어르신이 없습니다")
                seniorUserNum = seniorCert.senior_num

                // 2) 보호자 정보
                val guardianResponse = repository.getUserByNum(guardianUserNum)
                guardianId = guardianResponse.userId
                guardianTel = guardianResponse.userTel
                guardianProfileImg = "${RetrofitClient.apiBaseUrl}down/profile/${guardianUserNum}"

                // 3) 어르신 정보 (화면에 안 쓸 수도 있지만 기존 로직 유지)
                val seniorResponse = repository.getUserByNum(seniorUserNum!!)
                seniorId = seniorResponse.userId
                seniorTel = seniorResponse.userTel
                // 필요 시 절대 URL 보정
                seniorProfileImg = "${RetrofitClient.apiBaseUrl}down/profile/${seniorUserNum!!}"

                seniorHomeLat = seniorResponse.userHomeLat.toDoubleOrNull() ?: 0.0
                seniorHomeLon = seniorResponse.userHomeLot.toDoubleOrNull() ?: 0.0
                seniorCondition = (seniorResponse.userCondition == 1)

                // 4) 위/경도 → 주소
                convertLatLngToAddress(seniorHomeLat, seniorHomeLon)

                Log.d(TAG, "정보 로드 완료 - 보호자: $guardianId, 어르신: $seniorId ($seniorUserNum)")
            } catch (e: Exception) {
                Log.e(TAG, "정보 로드 실패", e)
                errorMessage = e.message ?: "알 수 없는 오류"
            } finally {
                isLoading = false
            }
        }
    }

    /** 위/경도 → 주소 */
    private suspend fun convertLatLngToAddress(lat: Double, lng: Double) {
        try {
            if (context != null && lat != 0.0 && lng != 0.0) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                seniorAddress = if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: "주소를 찾을 수 없습니다"
                } else {
                    "주소를 찾을 수 없습니다"
                }
            } else {
                seniorAddress = "등록된 주소가 없습니다"
            }
        } catch (e: Exception) {
            Log.e(TAG, "주소 변환 실패", e)
            seniorAddress = "주소 변환 실패"
        }
    }

    /** 주소 → 위/경도 */
    private fun convertAddressToLatLng(address: String): Pair<Double, Double>? {
        return try {
            if (context != null && address.isNotBlank()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(address, 1)
                if (!addresses.isNullOrEmpty()) {
                    val loc = addresses[0]
                    Pair(loc.latitude, loc.longitude)
                } else null
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "주소를 위도/경도로 변환 실패", e)
            null
        }
    }

    /** 어르신 정보 업데이트 */
    fun updateSeniorProfile(
        newName: String? = null,
        newTel: String,
        newAddress: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userNum = seniorUserNum ?: throw Exception("어르신 정보가 없습니다")

                val latLng = convertAddressToLatLng(newAddress)
                    ?: throw Exception("주소를 찾을 수 없습니다. 정확한 주소를 입력해주세요.")

                val request = UpdateProfileRequest(
                    userTel = newTel,
                    userProfileImg = seniorProfileImg, // 기존 유지
                    userHomeLat = latLng.first.toString(),
                    userHomeLot = latLng.second.toString(),
                    userCondition = if (seniorCondition) "1" else "0"
                )

                val response = RetrofitClient.memberService.updateProfile(userNum, request)
                if (response.isSuccessful) {
                    if (newName != null) seniorId = newName
                    seniorTel = newTel
                    seniorHomeLat = latLng.first
                    seniorHomeLon = latLng.second
                    seniorAddress = newAddress
                    onSuccess()
                    Log.d(TAG, "어르신 정보 업데이트 성공")
                } else {
                    throw Exception("서버 오류: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "어르신 정보 업데이트 실패", e)
                onError(e.message ?: "업데이트 실패")
            }
        }
    }

    /** 정보 새로고침 */
    fun refresh() = fetchGuardianAndSeniorInfo()

    /** UI 호출용 */
    fun loadProfileData() = fetchGuardianAndSeniorInfo()

    /** 보호자 프로필 수정 (이미지 업로드 + PUT) */
    fun updateProfile(
        newTel: String,
        newProfileImg: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                var finalProfileImgPath = newProfileImg // 서버에 보낼 값(상대 path or 기존 값)

                // 로컬 URI라면 먼저 업로드
                if (newProfileImg.startsWith("content://") || newProfileImg.startsWith("file://")) {
                    val uri = Uri.parse(newProfileImg)
                    val uploadedPath = uploadImageToServer(uri, context, guardianUserNum)
                    if (uploadedPath.isNullOrBlank()) {
                        errorMessage = "이미지 업로드 실패로 저장을 중단했습니다. 잠시 후 다시 시도해주세요."
                        isLoading = false
                        return@launch // PUT 중단
                    }
                    finalProfileImgPath = uploadedPath // ex) "profile/xxx.jpg"
                }

                val request = UpdateProfileRequest(
                    userTel = newTel,
                    userProfileImg = finalProfileImgPath, // 서버는 path를 기대
                    userHomeLat = "0",
                    userHomeLot = "0",
                    userCondition = "0"
                )

                val response = RetrofitClient.memberService.updateProfile(guardianUserNum, request)
                if (response.isSuccessful) {
                    guardianTel = newTel
                    // 화면 표시용 절대 URL로 보정
                    guardianProfileImg = "${RetrofitClient.apiBaseUrl}/down/profile/${guardianUserNum}"

                    onSuccess()
                } else {
                    errorMessage = "프로필 업데이트 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "네트워크 오류: ${e.message}"
                Log.e(TAG, "프로필 업데이트 실패", e)
            } finally {
                isLoading = false
            }
        }
    }

    /** 이미지 업로드: 서버가 반환하는 path("profile/xxx.jpg")를 리턴 */
    private suspend fun uploadImageToServer(uri: Uri, context: Context, userNum: Int): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input -> file.outputStream().use { output -> input.copyTo(output) } }

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = memberService.uploadProfileImage(userNum, imagePart)
            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "Upload rsCode=${body?.rsCode}, message=${body?.message}, path=${body?.path}")
                if (body?.rsCode == 200) {
                    body.path // <- "profile/xxx.jpg"
                } else {
                    Log.e(TAG, "이미지 업로드 실패(rsCode): ${body?.rsCode} / ${body?.message}")
                    null
                }
            } else {
                Log.e(TAG, "이미지 업로드 실패(http): ${response.code()} / ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "이미지 업로드 실패", e)
            null
        }
    }

    /** path: "profile/xxx.jpg" → "https://.../profile/xxx.jpg" */
    fun toAbsoluteUrl(baseUrl: String, path: String?): String? {
        if (path.isNullOrBlank()) return null
        return if (path.startsWith("http", ignoreCase = true)) {
            path
        } else {
            val b = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            b + path.removePrefix("/")
        }
    }
}
