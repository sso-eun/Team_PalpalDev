package com.example.dundun_hi.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.FindIdRepository
import com.example.dundun_hi.data.FindIdRequest
import com.example.dundun_hi.data.FindIdResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

// FindId 상태를 나타내는 sealed class
sealed class FindIdResult {
    object Idle : FindIdResult()
    object Loading : FindIdResult()
    data class Success(val userId: String) : FindIdResult()
    data class Error(val errorMessage: String) : FindIdResult()
}

class FindIdViewModel(
    private val repository: FindIdRepository = FindIdRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<FindIdResult>(FindIdResult.Idle)
    val state: StateFlow<FindIdResult> = _state

    fun findIdByPhone(phone: String) {
        if (phone.isBlank()) {
            _state.value = FindIdResult.Error("전화번호를 입력해주세요")
            return
        }

        viewModelScope.launch {
            _state.value = FindIdResult.Loading
            try {
                val response: Response<FindIdResponse> =
                    repository.findId(FindIdRequest(user_tel = phone.trim()))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.message == "ID 조회 성공") {
                        // 서버가 "조회 성공"으로 메시지를 준다고 가정
                        _state.value = FindIdResult.Success(body.userId)
                    } else {
                        _state.value = FindIdResult.Error(body?.message ?: "알 수 없는 오류")
                    }
                } else {
                    _state.value = FindIdResult.Error("서버 오류: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = FindIdResult.Error("네트워크 오류: ${e.localizedMessage}")
            }
        }
    }
}
