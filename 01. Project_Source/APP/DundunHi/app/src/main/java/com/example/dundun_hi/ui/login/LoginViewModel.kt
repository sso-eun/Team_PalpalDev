package com.example.dundun_hi.ui.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.LoginRequest
import com.example.dundun_hi.data.LoginResponse
import com.example.dundun_hi.network.RetrofitClient
import kotlinx.coroutines.launch
import com.google.firebase.messaging.FirebaseMessaging
import com.example.dundun_hi.data.FcmTokenRequest

class LoginViewModel : ViewModel() {

    // 로그인 성공 시 API에서 내려주는 전체 LoginResponse 객체
    private val _loginState = mutableStateOf<LoginResponse?>(null)
    val loginState: State<LoginResponse?> = _loginState

    // 에러 메시지
    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun login(userId: String, userPw: String) {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.memberService.login(
                    LoginRequest(userId, userPw)
                )
                if (resp.isSuccessful) {
                    // 성공하면 LoginResponse 전체를 상태로 바꿔준다.
                    _loginState.value = resp.body()

                    // 로그인 성공 시 FCM 토큰을 받아 서버에 전송
                    val loginResponse = resp.body()
                    val userNumInt = loginResponse?.userNum?.toIntOrNull()
                    if (userNumInt != null) {
                        android.util.Log.d("FCM", "로그인 성공: userNumInt=$userNumInt, FCM 토큰 요청 시작")
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                android.util.Log.d("FCM", "FCM 토큰 획득 성공: $token")
                                viewModelScope.launch {
                                    try {
                                        android.util.Log.d("FCM", "서버로 FCM 토큰 전송 시도: userNum=$userNumInt, token=$token")
                                        val req = FcmTokenRequest(user_num = userNumInt, fcm_token = token)
                                        val res = com.example.dundun_hi.network.RetrofitClient.memberService.sendFcmToken(req)
                                        if (res.isSuccessful) {
                                            android.util.Log.d("FCM", "토큰 서버 전송 성공: code=${res.code()}")
                                        } else {
                                            android.util.Log.e("FCM", "토큰 서버 전송 실패: code=${res.code()}")
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("FCM", "토큰 서버 전송 예외: ${e.message}")
                                    }
                                }
                            } else {
                                android.util.Log.w("FCM", "FCM 토큰 가져오기 실패", task.exception)
                            }
                        }
                    }
                } else {
                    _error.value = "HTTP ${resp.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * 상태를 초기화하여, Compose 측에서 LaunchedEffect가
     * 중복 실행되지 않도록 방지한다.
     */
    fun clearLoginState() {
        _loginState.value = null
    }
}

