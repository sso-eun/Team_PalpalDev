package com.example.dundun_hi.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.MemberResponse
import com.example.dundun_hi.network.MemberService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SeniorProfileViewModel(private val apiService: MemberService) : ViewModel() {

    private val _seniorProfile = MutableStateFlow<MemberResponse?>(null)
    val seniorProfile = _seniorProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    fun fetchSeniorProfile(seniorNum: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // MemberService에 이미 정의된 getMember 함수 사용
                val response = apiService.getMember(seniorNum)
                _seniorProfile.value = response
            } catch (e: Exception) {
                // 오류 처리
                _seniorProfile.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// ViewModel에 apiService를 주입하기 위한 Factory
class SeniorProfileViewModelFactory(private val apiService: MemberService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SeniorProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SeniorProfileViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}