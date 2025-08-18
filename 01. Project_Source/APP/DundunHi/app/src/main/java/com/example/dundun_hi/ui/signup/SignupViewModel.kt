package com.example.dundun_hi.ui.signup

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.CodeAuthRepository
import com.example.dundun_hi.data.CodeAuthSendResponse
import com.example.dundun_hi.data.CodeAuthVerifyResponse
import com.example.dundun_hi.data.SignupRepository
import com.example.dundun_hi.data.SignupRequest
import com.example.dundun_hi.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

// --- 1. '인증 후 임시 회원가입'의 결과를 나타내는 새로운 상태 클래스 ---
sealed class UserCreationState {
    object Idle : UserCreationState()
    object Loading : UserCreationState()
    data class Success(val userNum: Int, val userId: String) : UserCreationState()
    data class Error(val message: String) : UserCreationState()
}

class SignupViewModel(
    private val repo: SignupRepository = SignupRepository()
) : ViewModel() {

    private val _userCreationState = MutableStateFlow<UserCreationState>(UserCreationState.Idle)
    val userCreationState: StateFlow<UserCreationState> = _userCreationState

    var createdUserNum: Int? = null
        private set
    var createdUserId: String? = null
        private set

    var lastTelNum: String = ""
        private set
    private val codeAuthRepository = CodeAuthRepository(RetrofitClient.codeAuthService)

    // --- 1. 누락되었던 변수들 추가 ---
    private val _sendCodeResult = MutableStateFlow<CodeAuthSendResponse?>(null)
    val sendCodeResult: StateFlow<CodeAuthSendResponse?> = _sendCodeResult

    private val _verifyCodeResult = MutableStateFlow<CodeAuthVerifyResponse?>(null)
    val verifyCodeResult: StateFlow<CodeAuthVerifyResponse?> = _verifyCodeResult
    // ------------------------------------

    fun verifyCodeAndCreateUser(name: String, phone: String, authCode: String, userType: Int) {
        viewModelScope.launch {
            _userCreationState.value = UserCreationState.Loading
            try {
                val verifyResponse = codeAuthRepository.verifyCode(phone, authCode)
                if (verifyResponse.rsCode == 200) {
                    // --- 2. 누락되었던 파라미터 추가 ---
                    val signupRequest = SignupRequest(
                        user_type = userType,
                        user_id = name,
                        user_pw = phone,
                        user_tel = phone,
                        user_profile_img = "", // 기본값
                        user_home_lat = "",    // 기본값
                        user_home_lot = "",    // 기본값
                        user_condition = 0     // 기본값
                    )
                    // ------------------------------------
                    val signupResponse = repo.signup(signupRequest)

                    if (signupResponse.message == "회원가입 성공") {
                        val userNumInt = signupResponse.userNum.toIntOrNull() ?: 0
                        createdUserNum = userNumInt
                        createdUserId = name
                        _userCreationState.value = UserCreationState.Success(userNumInt, name)

                        //fcm 토큰 전송------------------------------------
                        sendFcmTokenToServer(signupResponse.userNum)

                    }
                } else {
                    _userCreationState.value = UserCreationState.Error(verifyResponse.message)
                }
            } catch (e: Exception) {
                _userCreationState.value = UserCreationState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    // --- FCM 토큰 전송을 위한 private 함수 추가 ---S
    private fun sendFcmTokenToServer(userNumStr: String?) {
        val userNumInt = userNumStr?.toIntOrNull()
        if (userNumInt != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM", "Signup Success - FCM Token: $token")
                    viewModelScope.launch {
                        try {
                            val req = FcmTokenRequest(user_num = userNumInt, user_token = token)
                            RetrofitClient.memberService.sendFcmToken(req)
                            Log.d("FCM", "Token sent to server successfully from signup.")
                        } catch (e: Exception) {
                            Log.e("FCM", "Failed to send token from signup", e)
                        }
                    }
                } else {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                }
            }
        }
    }
    // ------------------------------------------
    fun sendVerificationCode(telNum: String) {
        lastTelNum = telNum
        viewModelScope.launch {
            try {
                _sendCodeResult.value = codeAuthRepository.sendCode(telNum)
            } catch (e: Exception) {
                _sendCodeResult.value =
                    CodeAuthSendResponse(-1, e.localizedMessage ?: "네트워크 오류")
            }
        }
    }

    fun signup(req: SignupRequest) = viewModelScope.launch {
        try {
            val resp = repo.signup(req)
            if (resp.message == "회원가입 성공") {
                lastUserId = req.user_id
                _state.value = SignupResult.Success(lastUserId, resp.userNum)
            } else {
                _state.value = SignupResult.Error(resp.message)
            }
        } catch (e: Exception) {
            _state.value = SignupResult.Error(e.message ?: "네트워크 오류")
        }
    }

    // --- 기존 상태 변수들은 시니어 가입 흐름을 위해 유지 ---
    private val _state = MutableStateFlow<SignupResult>(SignupResult.Idle)
    val state: StateFlow<SignupResult> = _state
    var lastUserId: String = ""
        private set
}


