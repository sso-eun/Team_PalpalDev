package com.example.dundun_hi.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.SignupRepository
import com.example.dundun_hi.data.SignupRequest
import com.example.dundun_hi.data.SignupResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignupViewModel(
    private val repo: SignupRepository = SignupRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<SignupResult>(SignupResult.Idle)
    val state: StateFlow<SignupResult> = _state

    fun signup(req: SignupRequest) = viewModelScope.launch {
        try {
            val resp: SignupResponse = repo.signup(req)   // ← DTO 수신
            if (resp.message == "success") {
                _state.value = SignupResult.Success(resp.userId, resp.userNum) // ← UI 이벤트로 변환
            } else {
                _state.value = SignupResult.Error(resp.message)
            }
        } catch (e: Exception) {
            _state.value = SignupResult.Error(e.message ?: "네트워크 오류")
        }
    }
}



