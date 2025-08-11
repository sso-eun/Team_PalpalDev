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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.dundun_hi.data.OnboardingManager


// ViewModel이 UI에게 전달할 내비게이션 상태 정의
sealed class LoginResult {
    object Idle : LoginResult()
    object Loading : LoginResult()
    data class GoToAuthLoading(val userNum: Int, val userId: String, val seniorNum: Int) : LoginResult()
    data class GoToMain(val userNum: Int, val userId: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

// ViewModel을 AndroidViewModel로 변경하고 Application을 받도록 수정
class LoginViewModel(
    // Application context를 받음
    application: Application,
    private val repo: SignupRepository = SignupRepository()
) : AndroidViewModel(application) {

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
                    val userType = loginData.userType                   // 중요: API가 user_type을 반환한다고 가정


                    if (userNum == 0) {
                        _loginResult.value = LoginResult.Error("사용자 정보를 찾을 수 없습니다.")
                        return@launch
                    }

                    // 2. user_type에 따라 분기
                    // 보호자인 경우
                    if (userType == 1) {
                        val authStatusResponse = RetrofitClient.memberService.getAuthStatusByGuardianNo(userNum)
                        if (authStatusResponse.isSuccessful && authStatusResponse.body() != null) {
                            val authData = authStatusResponse.body()!!

                            // --- 여기가 핵심적인 변경 부분입니다 ---
                            if (authData.status == 1) { // 인증이 승인된 상태라면
                                // 1. OnboardingManager를 통해 완료 여부를 확인
                                val hasCompleted = OnboardingManager.hasCompletedOnboarding(getApplication())

                                if (hasCompleted) {
                                    // 2. 이미 완료했다면, 바로 메인 화면으로 보냄
                                    _loginResult.value = LoginResult.GoToMain(userNum, name)
                                } else {
                                    // 3. 아직 완료하지 않았다면, 온보딩을 위해 AuthLoadingScreen으로 보냄
                                    _loginResult.value = LoginResult.GoToAuthLoading(userNum, name, authData.seniorNum)
                                }
                            } else { // 대기(0) 또는 반려(2) 상태
                                _loginResult.value = LoginResult.GoToAuthLoading(userNum, name, authData.seniorNum)
                            }
                            // ------------------------------------------
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