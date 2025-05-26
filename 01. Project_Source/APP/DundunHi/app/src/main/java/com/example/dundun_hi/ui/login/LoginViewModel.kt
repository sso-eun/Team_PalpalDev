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

    private val _loginState = mutableStateOf<LoginResponse?>(null)
    val loginState: State<LoginResponse?> = _loginState

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun login(userId: String, userPw: String) {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.memberService.login(
                    LoginRequest(userId, userPw)
                )
                if (resp.isSuccessful) {
                    _loginState.value = resp.body()
                } else {
                    _error.value = "HTTP ${resp.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
// Compose 에 맞춘 State 기반 로그인 상태 관리