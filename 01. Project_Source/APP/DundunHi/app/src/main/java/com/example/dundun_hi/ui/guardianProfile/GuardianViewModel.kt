package com.example.dundun_hi.ui.guardianProfile

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.MemberResponse
import com.example.dundun_hi.data.UserRepository
import com.example.dundun_hi.network.RetrofitClient
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


class GuardianViewModel(
    private val repository: UserRepository,
    private val userNum: Int,
    private val context: Context? = null
) : ViewModel() {

    var seniorUserNum by mutableStateOf<Int?>(null)
        private set

    companion object {
        private const val TAG = "GuardianViewModel"
    }

    private val sharedPreferences: SharedPreferences? = context?.getSharedPreferences("guardian_prefs", Context.MODE_PRIVATE)

    val userNumber: Int
        get() = userNum

    /** 서버에서 받아온 보호자 ID(화면에 표시할 이름) */
    var guardianId by mutableStateOf("")
        private set

    /** 서버에서 받아온 보호자 전화번호 */
    var guardianTel by mutableStateOf("")
        private set

    /** 서버에서 받아온 보호자 프로필 이미지 URL(없으면 빈 문자열) */
    var guardianProfileImg by mutableStateOf("")
        private set

    /** 로딩 상태 */
    var isLoading by mutableStateOf(false)
        private set

    /** 오류 메시지 */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        // 화면이 처음 생성될 때 한 번 자동으로 호출
        Log.d(TAG, "GuardianViewModel 초기화: userNum=$userNum, context=${context != null}")
        fetchGuardianFromServer()
    }

    /**
     * 서버에서 보호자 정보를 가져와서 각 State에 값을 설정합니다.
     */
    fun fetchGuardianFromServer() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                val response: MemberResponse = repository.getUserByNum(userNum)
                guardianId = response.userId
                guardianTel = response.userTel
                guardianProfileImg = response.userProfileImg ?: ""

                Log.d(TAG, "보호자 정보 로드 완료: ID=$guardianId, Tel=$guardianTel")
            } catch (e: Exception) {
                Log.e(TAG, "보호자 정보 로드 실패", e)
                errorMessage = e.message ?: "알 수 없는 오류"
            } finally {
                isLoading = false
            }
        }

        viewModelScope.launch {
            try {
                val certList = RetrofitClient.memberService.getCertList(page = 1, limit = 10)
                val mySenior = certList.body()?.find { it.guardian_no == userNum } // ✅ 수정됨
                seniorUserNum = mySenior?.senior_num
                Log.d(TAG, "어르신 고유번호: $seniorUserNum")
            } catch (e: Exception) {
                Log.e("GuardianVM", "cert_list 에러", e)
            }
        }

    }

    /**
     * 로그인 검증을 위한 메서드 (입력된 정보와 서버 정보 비교)
     */
    fun validateLogin(inputName: String, inputPhone: String): Boolean {
        return guardianId == inputName && guardianTel == inputPhone
    }

    /**
     * 로그인 상태를 SharedPreferences에 저장
     */
    fun saveLoginState(isLoggedIn: Boolean) {
        sharedPreferences?.edit()
            ?.putBoolean("is_logged_in", isLoggedIn)
            ?.putInt("logged_user_num", userNum)
            ?.apply()
    }

    /**
     * 저장된 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences?.getBoolean("is_logged_in", false) ?: false
    }

    /**
     * 로그아웃 처리
     */
    fun logout() {
        sharedPreferences?.edit()
            ?.clear()
            ?.apply()
    }
}