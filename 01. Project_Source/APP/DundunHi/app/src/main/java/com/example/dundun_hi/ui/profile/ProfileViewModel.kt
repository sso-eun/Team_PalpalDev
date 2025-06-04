// app/src/main/java/com/example/dundun_hi/ui/profile/ProfileViewModel.kt

package com.example.dundun_hi.ui.profile

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

class ProfileViewModel(
    private val repository: UserRepository,
    private val userNum: Int
) : ViewModel() {

    /** 서버에서 받아온 로그인 ID(화면에 표시할 이름) */
    var userId by mutableStateOf("")
        private set

    /** 서버에서 받아온 전화번호 */
    var userTel by mutableStateOf("")
        private set

    /** 서버에서 받아온 프로필 이미지 URL(없으면 빈 문자열) */
    var userProfileImg by mutableStateOf("")
        private set

    /** 서버에서 받아온 집 위도 (문자열을 Double로 변환) */
    var userHomeLat by mutableStateOf(0.0)
        private set

    /** 서버에서 받아온 집 경도 (문자열을 Double로 변환) */
    var userHomeLon by mutableStateOf(0.0)
        private set

    /** 서버에서 받아온 외출 여부 (0=집, 1=외출중) */
    var userCondition by mutableStateOf(false)
        private set

    /** 로딩 상태 */
    var isLoading by mutableStateOf(false)
        private set

    /** 오류 메시지 */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        // 화면이 처음 생성될 때 한 번 자동으로 호출
        fetchUserFromServer()
    }

    /**
     * 1) 서버에서 MemberResponse를 가져와서
     *    각 State(userId, userTel, ...)에 값을 넣어 준다.
     * 2) 이 메서드를 public으로 변경했기 때문에,
     *    Compose 쪽에서 `viewModel.fetchUserFromServer()`를 직접 호출할 수 있다.
     */
    fun fetchUserFromServer() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                // 내부적으로 suspend fun getUserByNum(userNum: Int): MemberResponse 호출
                val response: MemberResponse = repository.getUserByNum(userNum)

                userId          = response.userId
                userTel         = response.userTel
                userProfileImg  = response.userProfileImg
                userHomeLat     = response.userHomeLat.toDoubleOrNull() ?: 0.0
                userHomeLon     = response.userHomeLot.toDoubleOrNull() ?: 0.0
                userCondition   = (response.userCondition == 1)

            } catch (e: Exception) {
                errorMessage = e.message ?: "알 수 없는 오류"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * UpdateProfileScreen에서 “수정 완료”를 눌렀을 때 호출될 메서드.
     * 서버에 PUT 요청을 보내고, 성공 시 다시 fetchUserFromServer()를 호출한다.
     */
    fun updateProfile(
        newTel: String,
        newProfileImg: String,
        newHomeLat: Double,
        newHomeLon: Double,
        isOuting: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 예시: UpdateProfileRequest 정의된 데이터 클래스에 맞게 작성
                val request = UpdateProfileRequest(
                    userTel        = newTel,
                    userProfileImg = newProfileImg,
                    userHomeLat    = newHomeLat.toString(),
                    userHomeLot    = newHomeLon.toString(),
                    userCondition  = if (isOuting) "1" else "0"
                )
                // 실제 RetrofitService의 updateProfile(...) 호출
                val resp = RetrofitClient.memberService.updateProfile(userNum, request)
                if (resp.isSuccessful) {
                    // 서버 수정이 성공하면, 뷰모델 내부에서 다시 fetchUserFromServer()를 호출하여
                    // 상태(userTel, userProfileImg, userCondition 등)를 최신화한다.
                    fetchUserFromServer()
                    onSuccess()
                } else {
                    // 필요에 따라 오류 처리 (resp.code() 등)
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 예외 처리
            }
        }
    }

    /**
     * 현재 위치(lat,lon)와 집 위치(userHomeLat, userHomeLon)를 비교해서,
     * threshold(meters) 이상 차이나면 “외출 중”, 이하면 “집에 있음”으로 자동 업데이트하는 흐름.
     *
     * @param currentLat  현재 GPS 위도
     * @param currentLon  현재 GPS 경도
     * @param thresholdInMeters  “집”으로 간주할 최대 거리 (meters). 이 범위를 넘으면 외출 중.
     * @param onResult    (Boolean 변경여부) → true: 상태가 바뀌어서 서버에 PUT 요청을 보냈음, false: 변화 없음
     */
    fun autoUpdateConditionBasedOnLocation(
        currentLat: Double,
        currentLon: Double,
        thresholdInMeters: Double = 100.0, // 예: 100m 이내는 집, 넘으면 외출
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            // 1) 두 좌표 간 거리 계산
            val dist = distanceBetweenMeters(
                userHomeLat, userHomeLon,
                currentLat, currentLon
            )

            // 2) 비교 후, 새로운 상태 결정
            val shouldBeOuting = (dist > thresholdInMeters)

            // 3) 만약 서버에 저장된 userCondition과 같으면 아무것도 안 함
            if (shouldBeOuting == userCondition) {
                onResult(false)
                return@launch
            }

            // 4) userCondition이 바뀌었으므로, PUT 요청을 보낸다.
            updateProfile(
                newTel = userTel,                     // 전화번호는 변경 없으므로 기존 값 전달
                newProfileImg = userProfileImg,       // 프로필 이미지 역시 그대로
                newHomeLat = userHomeLat,             // 집 위치도 그대로
                newHomeLon = userHomeLon,
                isOuting = shouldBeOuting
            ) {
                // 수정 성공 후 로컬 상태(userCondition)는 이미 updateProfile 에서 바뀜.
                onResult(true)
            }
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
                val requestBody = UpdatePasswordRequest(
                    current_pw = currentPw,
                    new_pw = newPw
                )
                val response = memberService.updatePassword(userNum, requestBody)

                if (response.isSuccessful) {
                    // 200~299 범위
                    val body: UpdatePasswordResponse? = response.body()
                    if (body != null) {
                        onResult(true, body.message)
                    } else {
                        onResult(false, "서버 응답이 비어 있습니다.")
                    }
                } else {
                    // HTTP 오류 (4xx, 5xx)
                    val errorMsg = response.errorBody()?.string() ?: "알 수 없는 오류가 발생했습니다."
                    onResult(false, errorMsg)
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "네트워크 오류가 발생했습니다.")
            }
        }
    }
}


