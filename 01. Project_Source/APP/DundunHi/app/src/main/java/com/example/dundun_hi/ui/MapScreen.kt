package com.example.dundun_hi.ui

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.*

@OptIn(ExperimentalNaverMapApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val defaultLocation = LatLng(36.6357, 127.4581) // 충북대

    // 현재 위치 가져오기
    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = LatLng(it.latitude, it.longitude)
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(currentLocation ?: defaultLocation, 15.0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = rememberMarkerState(position = defaultLocation),
                captionText = "충북대학교"
            )
            currentLocation?.let {
                Marker(
                    state = rememberMarkerState(position = it),
                    captionText = "내 위치"
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LargeCategoryButton(
                text = "병원",
                color = Color(0xFF4CAF50),
                icon = "➕",
                isSelected = selectedCategory == "병원",
                onClick = { selectedCategory = "병원" }
            )
            LargeCategoryButton(
                text = "경로당",
                color = Color(0xFF00796B),
                icon = "\uD83C\uDFE0",
                isSelected = selectedCategory == "경로당",
                onClick = { selectedCategory = "경로당" }
            )
            LargeCategoryButton(
                text = "쉼터",
                color = Color(0xFF039BE5),
                icon = "\u2744",
                isSelected = selectedCategory == "쉼터",
                onClick = { selectedCategory = "쉼터" }
            )
        }
    }
}

@Composable
fun LargeCategoryButton(
    text: String,
    color: Color,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFE0E0E0) else Color.White

    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$icon $text",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
