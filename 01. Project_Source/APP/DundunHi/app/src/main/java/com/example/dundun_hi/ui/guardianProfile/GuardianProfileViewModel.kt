package com.example.dundun_hi.ui.guardianProfile

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.UpdateProfileRequest
import com.example.dundun_hi.data.UserRepository
import com.example.dundun_hi.network.RetrofitClient
import kotlinx.coroutines.launch
import java.util.*

class GuardianProfileViewModel(
    private val repository: UserRepository,
    private val guardianUserNum: Int,
    private val context: Context? = null

) : ViewModel() {

    companion object {
        private const val TAG = "GuardianProfileViewModel"
    }

    var guardianProfileImg by mutableStateOf<String?>(null)
        private set


    /** 보호자 정보 */
    var guardianId by mutableStateOf("")
        private set
    var guardianTel by mutableStateOf("")
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

                // 1. cert_list로 어르신 고유번호 가져오기
                val certResponse = RetrofitClient.memberService.getCertList(
                    page = 1,
                    limit = 10 // 일단 10개로 제한
                )

                if (!certResponse.isSuccessful || certResponse.body() == null) {
                    throw Exception("인증서 목록을 가져올 수 없습니다.")
                }

                val certList = certResponse.body()!!.results
                // 보호자와 연결된 어르신 찾기 (guardian_no가 현재 보호자의 userNum과 일치)
                val seniorCert = certList.find { it.guardian_no == guardianUserNum }
                    ?: throw Exception("등록된 어르신이 없습니다")

                seniorUserNum = seniorCert.senior_num

                // 2. 보호자 정보 가져오기
                val guardianResponse = repository.getUserByNum(guardianUserNum)
                guardianId = guardianResponse.userId
                guardianTel = guardianResponse.userTel

                // 3. 어르신 정보 가져오기
                val seniorResponse = repository.getUserByNum(seniorUserNum!!)
                seniorId = seniorResponse.userId
                seniorTel = seniorResponse.userTel
                seniorProfileImg = seniorResponse.userProfileImg ?: ""
                seniorHomeLat = seniorResponse.userHomeLat.toDoubleOrNull() ?: 0.0
                seniorHomeLon = seniorResponse.userHomeLot.toDoubleOrNull() ?: 0.0
                seniorCondition = (seniorResponse.userCondition == 1)

                // 4. 위도/경도를 주소로 변환
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

    /**
     * 위도/경도를 주소로 변환
     */
    private suspend fun convertLatLngToAddress(lat: Double, lng: Double) {
        try {
            if (context != null && lat != 0.0 && lng != 0.0) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    seniorAddress = address.getAddressLine(0) ?: "주소를 찾을 수 없습니다"
                } else {
                    seniorAddress = "주소를 찾을 수 없습니다"
                }
            } else {
                seniorAddress = "등록된 주소가 없습니다"
            }
        } catch (e: Exception) {
            Log.e(TAG, "주소 변환 실패", e)
            seniorAddress = "주소 변환 실패"
        }
    }

    /**
     * 주소를 위도/경도로 변환
     */
    fun convertAddressToLatLng(address: String): Pair<Double, Double>? {
        return try {
            if (context != null && address.isNotBlank()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(address, 1)

                if (!addresses.isNullOrEmpty()) {
                    val location = addresses[0]
                    Pair(location.latitude, location.longitude)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "주소를 위도/경도로 변환 실패", e)
            null
        }
    }

    /**
     * 어르신 정보 업데이트
     */
    fun updateSeniorProfile(
        newName: String,
        newTel: String,
        newAddress: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userNum = seniorUserNum ?: throw Exception("어르신 정보가 없습니다")

                // 주소를 위도/경도로 변환
                val latLng = convertAddressToLatLng(newAddress)
                    ?: throw Exception("주소를 찾을 수 없습니다. 정확한 주소를 입력해주세요.")

                val request = UpdateProfileRequest(
                    userTel = newTel,
                    userProfileImg = seniorProfileImg, // 기존 프로필 이미지 유지
                    userHomeLat = latLng.first.toString(),
                    userHomeLot = latLng.second.toString(),
                    userCondition = if (seniorCondition) "1" else "0"
                )

                val response = RetrofitClient.memberService.updateProfile(userNum, request)

                if (response.isSuccessful) {
                    // 성공 시 로컬 상태 업데이트
                    seniorId = newName // 주의: userId는 업데이트할 수 없을 수 있음
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

    /**
     * 정보 새로고침
     */
    fun refresh() {
        fetchGuardianAndSeniorInfo()
    }
}