package com.example.dundun_hi.ui // 혹은 viewmodel 패키지

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.CultureCenterResponse
import com.example.dundun_hi.network.RetrofitClient
import kotlinx.coroutines.launch

// UI에 표시될 데이터와 상태를 나타내는 클래스
data class CultureCenterUiState(
    val cultureCenter: CultureCenterResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CultureCenterViewModel : ViewModel() {

    private val _uiState = mutableStateOf(CultureCenterUiState())
    val uiState: State<CultureCenterUiState> = _uiState

    // 위도(latitude)와 경도(longitude)를 받아 API를 호출하는 함수
    fun fetchCultureCenter(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = CultureCenterUiState(isLoading = true) // 로딩 상태 시작
            try {
                // (수정) RetrofitInstance.api -> RetrofitClient.cultureCenterService 로 변경
                val response = RetrofitClient.cultureCenterService.getCultureCenter(latitude = lat, longitude = lon)
                _uiState.value = CultureCenterUiState(cultureCenter = response) // 성공
            } catch (e: Exception) {
                _uiState.value = CultureCenterUiState(error = "데이터를 불러오는 데 실패했습니다: ${e.message}") // 실패
            }
        }
    }
}