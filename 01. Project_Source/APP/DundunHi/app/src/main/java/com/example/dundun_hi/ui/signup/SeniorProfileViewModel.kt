package com.example.dundun_hi.ui.signup

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.MemberResponse
import com.example.dundun_hi.network.MemberService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

class SeniorProfileViewModel(private val apiService: MemberService) : ViewModel() {

    private val _seniorProfile = MutableStateFlow<MemberResponse?>(null)
    val seniorProfile = _seniorProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    // 추가: 주소/좌표 상태
    private val _roadAddress = MutableStateFlow<String?>(null)
    val roadAddress = _roadAddress.asStateFlow()

    private val _coords = MutableStateFlow<Pair<Double, Double>?>(null)
    val coords = _coords.asStateFlow()

    fun fetchSeniorProfile(seniorNum: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getMember(seniorNum)
                _seniorProfile.value = response

                // 서버 값에서 좌표/주소 초기화 (필드명 형태 맞춰서 파싱)
                val lat = response.userHomeLat.toDoubleOrNull()
                val lon = response.userHomeLot.toDoubleOrNull()

                if (lat != null && lon != null) {
                    _coords.value = lat to lon
                }
                // 프로필에 주소 문자열이 없다면 이후 필요 시 lat→addr로 채우면 됨
            } catch (e: Exception) {
                _seniorProfile.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** 위도·경도 → 도로명 주소 */
    fun setAddressFromLatLng(context: Context, lat: Double, lon: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val addr = geocodeLatLng(context, lat, lon)
            _roadAddress.value = addr
            _coords.value = lat to lon
            _isLoading.value = false
        }
    }

    /** 도로명 주소 → 위도·경도 */
    fun setLatLngFromAddress(context: Context, address: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val pair = geocodeAddress(context, address)
            _coords.value = pair
            _roadAddress.value = address.takeIf { !it.isNullOrBlank() }
            _isLoading.value = false
        }
    }

    // ---- Geocoder helpers (API 33+ 비동기 대응) ----

    private suspend fun geocodeLatLng(
        context: Context,
        lat: Double,
        lon: Double
    ): String? {
        val geocoder = Geocoder(context, Locale.KOREA)
        val result: List<Address>? = withTimeoutOrNull(4_000) {
            if (Build.VERSION.SDK_INT >= 33) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (!cont.isCompleted) cont.resume(addresses)
                        }
                        override fun onError(errorMessage: String?) {
                            if (!cont.isCompleted) cont.resume(emptyList())
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lon, 1)
            }
        }

        val a = result?.firstOrNull() ?: return null
        // 전체 도로명 주소 한 줄
        return a.getAddressLine(0)
    }

    private suspend fun geocodeAddress(
        context: Context,
        address: String
    ): Pair<Double, Double>? {
        val geocoder = Geocoder(context, Locale.KOREA)
        val result: List<Address>? = withTimeoutOrNull(4_000) {
            if (Build.VERSION.SDK_INT >= 33) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocationName(address, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (!cont.isCompleted) cont.resume(addresses)
                        }
                        override fun onError(errorMessage: String?) {
                            if (!cont.isCompleted) cont.resume(emptyList())
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(address, 1)
            }
        }

        val a = result?.firstOrNull() ?: return null
        return a.latitude to a.longitude
    }
}

// ViewModel Factory
class SeniorProfileViewModelFactory(private val apiService: MemberService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SeniorProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SeniorProfileViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
