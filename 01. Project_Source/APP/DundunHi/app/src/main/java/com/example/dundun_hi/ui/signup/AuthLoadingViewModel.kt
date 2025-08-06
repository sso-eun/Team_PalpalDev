package com.example.dundun_hi.ui.signup

// 파일 경로: app/src/main/java/com/example/dundun_hi/ui/signup/AuthLoadingViewModel.kt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.dundun_hi.network.RetrofitClient

class AuthLoadingViewModel : ViewModel() {

    // UI가 관찰할 인증 상태 (null: 초기 로딩, 0: 대기, 1: 승인, 2: 반려)
    private val _authStatus = MutableStateFlow<Int?>(null)
    val authStatus: StateFlow<Int?> = _authStatus

    // --- seniorNum을 저장할 상태 추가 ---
    private val _seniorNum = MutableStateFlow(0)
    val seniorNum: StateFlow<Int> = _seniorNum

    // TODO: 실제 유저 이름
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    fun setUserName(id: String) {
        // 기존 setUserName을 확장하여 userNum은 지금 사용하지 않더라도 받아둘 수 있음
        _userName.value = id
    }

    fun setSeniorNum(num: Int) {
        _seniorNum.value = num
    }

    // 서버에 인증 상태를 요청하는 함수
    fun checkAuthStatus(guardianNo: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.memberService.getAuthStatusByGuardianNo(guardianNo)
                if (response.isSuccessful) {
                    val authData = response.body()
                    val currentStatus = authData?.status
                    _authStatus.value = currentStatus

                    if (currentStatus == 0) {
                        delay(3000)
                        checkAuthStatus(guardianNo)
                    }
                } else {
                    Log.e("AuthLoadingViewModel", "API Error: ${response.errorBody()?.string()}")
                    _authStatus.value = -1
                }
            } catch (e: Exception) {
                Log.e("AuthLoadingViewModel", "Exception: ${e.message}")
                _authStatus.value = -1
            }
        }
    }
}