package com.example.dundun_hi.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Toast
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
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.*
import com.naver.maps.map.overlay.PathOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class PlaceInfo(
    val name: String,
    val address: String,
    val phone: String,
    val lat: Double,
    val lon: Double
)

data class RouteInfo(
    val distance: Int, // 미터 단위
    val duration: Int, // 초 단위
    val path: List<LatLng>,
    val guide: List<String> // 경로 안내 텍스트
)

@OptIn(ExperimentalNaverMapApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var placeInfos by remember { mutableStateOf<List<PlaceInfo>>(emptyList()) }
    var selectedPlace by remember { mutableStateOf<PlaceInfo?>(null) }
    var routeInfo by remember { mutableStateOf<RouteInfo?>(null) }
    var showRouteDetails by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState()
    val defaultLocation = LatLng(36.6357, 127.4581)

    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = LatLng(it.latitude, it.longitude)
                cameraPositionState.move(CameraUpdate.toCameraPosition(CameraPosition(currentLocation!!, 15.0)))
            }
        }
    }

    LaunchedEffect(selectedCategory) {
        if (selectedCategory != null && currentLocation != null) {
            placeInfos = getPlacesFromAPI(
                context = context,
                category = selectedCategory!!,
                lat = currentLocation!!.latitude,
                lon = currentLocation!!.longitude
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // 현재 위치 마커
            currentLocation?.let {
                Marker(
                    state = rememberMarkerState(position = it),
                    captionText = "내 위치"
                )
            }

            // 장소 마커들
            placeInfos.forEach { place ->
                Marker(
                    state = rememberMarkerState(position = LatLng(place.lat, place.lon)),
                    onClick = {
                        selectedPlace = place
                        true
                    }
                )
            }

            // 경로 표시
            routeInfo?.let { route ->
                PathOverlay(
                    coords = route.path,
                    width = 5.dp,
                    color = Color(0xFF1A73E8), // 네이버 지도 스타일의 파란색
                    outlineColor = Color.White,
                    outlineWidth = 2.dp
                )
            }
        }

        // 선택된 장소 정보 패널
        selectedPlace?.let { place ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(28.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(place.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(place.address, fontSize = 20.sp)
                    Text("전화번호: ${place.phone}", fontSize = 20.sp)
                    Spacer(Modifier.height(20.dp))

                    val buttonColor = Color(0xFF4CAF50)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(buttonColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    // 네이버 지도 앱으로 바로 길안내 시작
                                    val url = "nmap://route/walk?dlat=${place.lat}&dlng=${place.lon}" +
                                            "&dname=${URLEncoder.encode(place.name, "UTF-8")}" +
                                            "&appname=com.example.dundun_hi"
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // 네이버 지도 앱이 없는 경우 웹 브라우저로 열기
                                        val webUrl = "https://map.naver.com/v5/directions/-/-/-/walk?" +
                                                "c=${place.lon},${place.lat},15,0,0,0,dh"
                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                                        context.startActivity(webIntent)
                                    }
                                }
                                .padding(horizontal = 32.dp, vertical = 16.dp)
                        ) {
                            Text("길찾기", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }

                        Box(
                            modifier = Modifier
                                .background(buttonColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${place.phone}"))
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 32.dp, vertical = 16.dp)
                        ) {
                            Text("전화하기", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        // 경로 안내 정보 패널
        if (showRouteDetails && routeInfo != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "도보 ${routeInfo!!.duration / 60}분",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${routeInfo!!.distance}m | ${routeInfo!!.duration / 60}분",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1A73E8), RoundedCornerShape(8.dp))
                            .clickable {
                                selectedPlace?.let { place ->
                                    // 네이버 지도 앱으로 길안내 시작
                                    val url = "nmap://route/walk?dlat=${place.lat}&dlng=${place.lon}" +
                                            "&dname=${URLEncoder.encode(place.name, "UTF-8")}" +
                                            "&appname=com.example.dundun_hi"
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // 네이버 지도 앱이 없는 경우 웹 브라우저로 열기
                                        val webUrl = "https://map.naver.com/v5/directions/-/-/-/walk?" +
                                                "c=${place.lon},${place.lat},15,0,0,0,dh"
                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                                        context.startActivity(webIntent)
                                    }
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "길안내 시작",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 카테고리 버튼
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (showRouteDetails) 140.dp else 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LargeCategoryButton("🏨", "병원", Color(0xFF4CAF50), selectedCategory == "hospital") {
                selectedCategory = "hospital"
                showRouteDetails = false
                routeInfo = null
            }
            LargeCategoryButton("🏠", "경로당", Color(0xFF00796B), selectedCategory == "shelter") {
                selectedCategory = "shelter"
                showRouteDetails = false
                routeInfo = null
            }
            LargeCategoryButton("❄", "쉼터", Color(0xFF039BE5), selectedCategory == "care") {
                selectedCategory = "care"
                showRouteDetails = false
                routeInfo = null
            }
        }
    }
}

@Composable
fun LargeCategoryButton(
    icon: String,
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) color else Color.White
    val textColor = if (isSelected) Color.White else color

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(32.dp))
            .clickable { onClick() }
            .padding(horizontal = 28.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$icon $label",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

suspend fun getPlacesFromAPI(
    context: android.content.Context,
    category: String,
    lat: Double,
    lon: Double
): List<PlaceInfo> {
    return withContext(Dispatchers.IO) {
        val url = URL("https://dundunhi.onrender.com/places?category=$category&lat=$lat&lon=$lon&range=0.5")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Content-Type", "application/json")

        try {
            val response = conn.inputStream.bufferedReader().readText()
            val jsonArray = JSONArray(response)
            List(jsonArray.length()) { i ->
                val item = jsonArray.getJSONObject(i)
                PlaceInfo(
                    name = item.getString("name"),
                    address = item.getString("address"),
                    phone = item.getString("phone"),
                    lat = item.getDouble("lat"),
                    lon = item.getDouble("lon")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "장소 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
            emptyList()
        } finally {
            conn.disconnect()
        }
    }
}

fun getNaverDirections(
    context: android.content.Context,
    start: LatLng,
    goal: LatLng,
    onResult: (RouteInfo?) -> Unit
) {
    // 직접 API 호출 대신 네이버 지도 앱이나 웹으로 이동
    val url = "nmap://route/walk?dlat=${goal.latitude}&dlng=${goal.longitude}" +
            "&appname=com.example.dundun_hi"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // 네이버 지도 앱이 없는 경우 웹 브라우저로 열기
        val webUrl = "https://map.naver.com/v5/directions/-/-/-/walk?" +
                "c=${goal.longitude},${goal.latitude},15,0,0,0,dh"
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
        context.startActivity(webIntent)
    }
    onResult(null)
}