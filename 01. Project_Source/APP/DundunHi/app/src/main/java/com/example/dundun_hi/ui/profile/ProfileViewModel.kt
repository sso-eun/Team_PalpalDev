// app/src/main/java/com/example/dundun_hi/ui/profile/ProfileViewModel.kt

package com.example.dundun_hi.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.MemberResponse
import com.example.dundun_hi.data.UpdateProfileRequest
import com.example.dundun_hi.data.UserRepository
import com.example.dundun_hi.network.RetrofitClient
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
}
