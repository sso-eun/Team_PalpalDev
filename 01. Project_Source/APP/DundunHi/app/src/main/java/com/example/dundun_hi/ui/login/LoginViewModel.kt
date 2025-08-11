package com.example.dundun_hi.ui.login

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.FcmTokenRequest
import com.example.dundun_hi.data.LoginRequest
import com.example.dundun_hi.data.LoginResponse
import com.example.dundun_hi.data.SignupRepository
import com.example.dundun_hi.network.RetrofitClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel이 UI에게 전달할 내비게이션 상태 정의
sealed class LoginResult {
    object Idle : LoginResult()
    object Loading : LoginResult()
    data class GoToAuthLoading(val userNum: Int, val userId: String, val seniorNum: Int) : LoginResult()
    data class GoToMain(val userNum: Int, val userId: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class LoginViewModel(
    private val repo: SignupRepository = SignupRepository()
) : ViewModel() {

    private val _loginResult = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginResult = _loginResult.asStateFlow()

    /**
     * 통합 로그인 및 경로 결정 함수
     * 1. 로그인을 시도합니다.
     * 2. 성공 시 user_type을 확인합니다.
     * 3. user_type이 보호자(1)이면, 가족 인증 상태를 추가로 확인하여 경로를 결정합니다.
     * 4. 그 외에는 메인 화면으로 보냅니다.
     */
    fun performLoginAndRoute(name: String, phone: String) {
        viewModelScope.launch {
            _loginResult.value = LoginResult.Loading
            try {
                // 1. 로그인 API 호출
                val loginResponse = RetrofitClient.memberService.login(LoginRequest(name, phone))
                if (loginResponse.isSuccessful && loginResponse.body() != null) {
                    val loginData = loginResponse.body()!!
                    val userNum = loginData.userNum.toIntOrNull() ?: 0

                    // 중요: API가 user_type을 반환한다고 가정
                    val userType = loginData.userType

                    if (userNum == 0) {
                        _loginResult.value = LoginResult.Error("사용자 정보를 찾을 수 없습니다.")
                        return@launch
                    }

                    // 2. user_type에 따라 분기
                    if (userType == 1) { // 보호자인 경우
                        // 3. 이어서 가족 인증 상태 조회
                        val authStatusResponse = RetrofitClient.memberService.getAuthStatusByGuardianNo(userNum)
                        if (authStatusResponse.isSuccessful && authStatusResponse.body() != null) {
                            val authData = authStatusResponse.body()!!
                            // 대기, 승인, 반려 상태 모두 로딩화면으로 보내서 처리
                            _loginResult.value = LoginResult.GoToAuthLoading(userNum, name, authData.seniorNum)
                        } else {
                            // 인증 기록이 없는 보호자는 메인으로
                            _loginResult.value = LoginResult.GoToMain(userNum, name)
                        }
                    } else { // 어르신 또는 그 외
                        _loginResult.value = LoginResult.GoToMain(userNum, name)
                    }

                } else {
                    _loginResult.value = LoginResult.Error("이름 또는 전화번호가 일치하지 않습니다.")
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResult.Error(e.message ?: "네트워크 오류")
            }
        }
    }
}