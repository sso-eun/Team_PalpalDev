package com.example.dundun_hi.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.MemberResponse
import com.example.dundun_hi.data.UserRepository
import kotlinx.coroutines.launch

/**
 * ProfileViewModel: userNum을 받아 서버에서 MemberResponse를 조회하고,
 * 화면에서 관찰할 수 있는 상태로 바꿔 둔다.
 */
class ProfileViewModel(
    private val repository: UserRepository,
    private val userNum: Int
) : ViewModel() {

    var userId by mutableStateOf("")           // 서버에서 내려온 user_id(로그인 ID 혹은 실제 이름)
        private set

    var userTel by mutableStateOf("")          // user_tel
        private set

    var userProfileImg by mutableStateOf("")   // user_profile_img (URL 혹은 빈 문자열)
        private set

    var userHomeLat by mutableStateOf(0.0)     // user_home_lat
        private set

    var userHomeLon by mutableStateOf(0.0)     // user_home_lot
        private set

    var userCondition by mutableStateOf(false) // user_condition: true=외출 중, false=집
        private set

    var isLoading by mutableStateOf(false)     // 로딩 상태
        private set

    var errorMessage by mutableStateOf<String?>(null) // 오류 메시지
        private set

    init {
        fetchUserFromServer()
    }

    private fun fetchUserFromServer() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                val response: MemberResponse = repository.getUserByNum(userNum)
                userId = response.userId
                userTel = response.userTel
                userProfileImg = response.userProfileImg
                userHomeLat = response.userHomeLat.toDoubleOrNull() ?: 0.0
                userHomeLon = response.userHomeLot.toDoubleOrNull() ?: 0.0
                userCondition = (response.userCondition == 1)  // 1=외출 중
            } catch (e: Exception) {
                errorMessage = e.message ?: "알 수 없는 오류"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateTel(newTel: String) {
        userTel = newTel
        // TODO: 서버에 user_tel 수정 API 호출
    }

    fun updateProfileImg(newImgPath: String) {
        userProfileImg = newImgPath
        // TODO: 서버에 프로필 이미지 업로드 후 반환 URL 갱신
    }

    fun updateHomeLocation(lat: Double, lon: Double) {
        userHomeLat = lat
        userHomeLon = lon
        // TODO: 서버에 user_home_lat, user_home_lot 수정 API 호출
    }

    fun updateCondition(isOuting: Boolean) {
        userCondition = isOuting
        // TODO: 서버에 user_condition 수정 API 호출
    }
}
