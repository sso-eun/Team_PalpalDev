// CultureCenterScreen.kt
package com.example.dundun_hi.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dundun_hi.data.CultureCenterResponse
import com.example.dundun_hi.data.LocationViewModel
import com.example.dundun_hi.ui.CultureCenterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultureCenterScreen(
    cultureCenterViewModel: CultureCenterViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel()
) {
    val uiState by cultureCenterViewModel.uiState
    val location by locationViewModel.location.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            locationViewModel.fetchLocation()
        }
    }

    LaunchedEffect(location) {
        val (lat, lon) = location ?: return@LaunchedEffect
        cultureCenterViewModel.fetchCultureCenter(lat, lon)
    }

    LaunchedEffect(Unit) {
        if (location == null) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "내 주변 문화센터",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        // (수정) 글씨를 굵게 변경
                        fontWeight = FontWeight.Bold
                    )
                },
                // (수정) 배경색을 흰색으로 변경하고 그림자 효과 추가
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                // (추가) 상단 바와 컨텐츠를 구분하기 위한 그림자 효과
                modifier = Modifier.background(Color.White)
            )
        }
    ) { paddingValues ->
        // (수정) 전체 배경색을 흰색으로 변경
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> LoadingState()
                uiState.error != null -> ErrorState(message = uiState.error)
                uiState.cultureCenter != null -> CultureCenterInfoCard(data = uiState.cultureCenter!!)
                location == null -> InitialState()
            }
        }
    }
}


@Composable
fun CultureCenterInfoCard(data: CultureCenterResponse) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), // 카드는 약간의 회색으로 구분감 부여
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = data.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "거리",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "거리: ${data.distance}m",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(data.link))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("CultureCenterInfoCard", "링크 열기 실패: ${data.link}", e)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
            ) {
                Text(
                    "웹사이트 방문하기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LoadingState() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("주변 문화센터를 찾고 있어요...", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ErrorState(message: String?) {
    Text(
        text = message ?: "오류가 발생했습니다.",
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun InitialState() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("현재 위치를 확인하고 있습니다.", style = MaterialTheme.typography.bodyLarge)
        Text("위치 권한을 허용해주세요.", style = MaterialTheme.typography.bodyMedium)
    }
}