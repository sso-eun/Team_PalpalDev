package com.example.dundun_hi.ui.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.LoginRequest
import com.example.dundun_hi.data.LoginResponse
import com.example.dundun_hi.network.RetrofitClient
import kotlinx.coroutines.launch

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
