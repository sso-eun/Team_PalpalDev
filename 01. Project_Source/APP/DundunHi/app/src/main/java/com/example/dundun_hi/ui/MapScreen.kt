// MapScreen.kt
package com.example.dundun_hi.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.util.Log
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
import com.naver.maps.map.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class PlaceInfo(
    val name: String,
    val address: String,
    val phone: String,
    val lat: Double,
    val lon: Double
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

    val defaultLocation = LatLng(36.6357, 127.4581)

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
            currentLocation?.let {
                Marker(
                    state = rememberMarkerState(position = it),
                    captionText = "내 위치"
                )
            }

            placeInfos.forEach { place ->
                Marker(
                    state = rememberMarkerState(position = LatLng(place.lat, place.lon)),
                    onClick = {
                        selectedPlace = place
                        true
                    }
                )
            }
        }

        selectedPlace?.let { place ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(place.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(place.address, fontSize = 14.sp)
                    Text("전화번호: ${place.phone}", fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                                .clickable {
                                    val gmmIntentUri = Uri.parse("geo:0,0?q=${place.lat},${place.lon}(${place.name})")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(mapIntent)
                                }
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text("길찾기", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFF81C784), RoundedCornerShape(8.dp))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${place.phone}"))
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text("전화하기", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LargeCategoryButton("병원", Color(0xFF4CAF50), "➕", selectedCategory == "hospital") {
                selectedCategory = "hospital"
            }
            LargeCategoryButton("경로당", Color(0xFF00796B), "\uD83C\uDFE0", selectedCategory == "shelter") {
                selectedCategory = "shelter"
            }
            LargeCategoryButton("쉼터", Color(0xFF039BE5), "❄", selectedCategory == "care") {
                selectedCategory = "care"
            }
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
        Text("$icon $text", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
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

        return@withContext try {
            val code = conn.responseCode
            val isStream = if (code in 200..299) conn.inputStream else conn.errorStream
            val response = isStream.bufferedReader().use { it.readText() }

            Log.d("API_HTTP_CODE", "$code")
            Log.d("API_RAW_RESPONSE", response)

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
