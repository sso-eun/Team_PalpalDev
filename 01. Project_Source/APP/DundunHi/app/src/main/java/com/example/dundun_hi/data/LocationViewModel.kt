package com.example.dundun_hi.data

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _location = MutableStateFlow<Pair<Double, Double>?>(null)
    val location: StateFlow<Pair<Double, Double>?> = _location

    fun fetchLocation() {
        val context = getApplication<Application>()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return // 권한 없음 → UI에서 요청해야 함
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let {
                _location.value = Pair(it.latitude, it.longitude)
            }
        }
    }
}
