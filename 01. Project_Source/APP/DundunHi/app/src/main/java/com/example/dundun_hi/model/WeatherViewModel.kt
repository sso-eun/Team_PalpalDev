package com.example.dundun_hi.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * WeatherViewModel: 날씨 데이터 로드 및 UI 상태 관리
 */
class WeatherViewModel(private val repo: WeatherRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    fun load(lat: Double, lon: Double) {
        viewModelScope.launch {
            // ◀ 수정된 부분: runCatching 대신 try-catch 사용
            try {
                // 로딩 상태 설정
                _uiState.value = WeatherUiState.Loading
                // 데이터 호출
                val data = repo.getWeather(lat, lon)
                // 성공 시 상태 업데이트
                _uiState.value = WeatherUiState.Success(data)
            } catch (e: Exception) {
                // 실패 시 상태 업데이트
                _uiState.value = WeatherUiState.Error(e)
            }
        }
    }
}

/**
 * UI 상태 표현 클래스
 */
sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val data: WeatherModel) : WeatherUiState()
    data class Error(val error: Throwable) : WeatherUiState()
}
