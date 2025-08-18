// app/src/main/java/com/example/dundun_hi/ui/profile/ProfileScreen.kt
package com.example.dundun_hi.ui.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.dundun_hi.R
import com.example.dundun_hi.data.AlertRepository
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    userId: String,
    onUpdateProfileClick: () -> Unit,
    onUpdatePasswordClick: () -> Unit,
    navController: NavController,
    navigateToMain: () -> Unit

) {
    val context = LocalContext.current
    val alertRepository = remember { AlertRepository.getInstance(context) }

    val userTel = viewModel.userTel
    val userProfileImg = viewModel.userProfileImg
    val imageVersion by remember { derivedStateOf { viewModel.imageVersion } }
    val userCondition = viewModel.userCondition
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    val imageModel = remember(userProfileImg, imageVersion) {
        if (userProfileImg.isNullOrEmpty()) {
            null
        } else {
            ImageRequest.Builder(context)
                .data(userProfileImg)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.WRITE_ONLY)
                .networkCachePolicy(CachePolicy.ENABLED)
                .crossfade(true)
                .build()
        }
    }

    val today = remember {
        val koreaTimeZone = java.util.TimeZone.getTimeZone("Asia/Seoul")
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        dateFormat.timeZone = koreaTimeZone
        dateFormat.format(Date())
    }

    val alerts = alertRepository.alertList
    val isAlertLoading = alertRepository.isLoading

    val todayAlerts = alerts
        .filter { it.date == today }
        .sortedBy { it.time }

    var isDataLoaded by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val callback = {
            isDataLoaded = true
        }
        alertRepository.onDataLoaded(callback)
        onDispose {
            alertRepository.removeDataLoadedCallback(callback)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchUserFromServer()
        alertRepository.forceRefreshFromServer()
    }

    // << 수정됨 1: 화면 전체 스크롤을 위한 scrollState 추가
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F0FA))
            .padding(16.dp)
            .verticalScroll(scrollState) // << 수정됨 2: 최상위 Column에 verticalScroll 적용
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { navigateToMain() }
        ) {
            Text(
                text = "든든하이",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "홈으로 이동",
                tint = Color(0xFF000000),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 프로필 카드
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth() // << 수정됨 3: 고정 높이 제거 (내용에 따라 유연하게 조절)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxWidth() // << 수정됨: fillMaxWidth() 추가하여 내부 정렬 개선
                    .padding(16.dp)
            ) {
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "프로필 사진",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Gray, CircleShape),
                        contentScale = ContentScale.Crop,
                        onSuccess = {
                            Log.d("ProfileScreen", "프로필 이미지 로드 성공: version $imageVersion")
                        },
                        onError = { error ->
                            Log.w("ProfileScreen", "프로필 이미지 로드 실패: ${error.result.throwable}")
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCCCCCC))
                            .border(1.dp, Color.Gray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_default_profile),
                            contentDescription = "기본 프로필",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = userId, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = userTel,
                    fontSize = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(9.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 위치 카드
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth() // << 수정됨 4: 고정 높이 제거
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location),
                            contentDescription = "위치 아이콘",
                            tint = Color(0xFF1AB277),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "위치", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "현재 상태:", fontSize = 24.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (userCondition) "외출중이에요" else "집에 있어요",
                        fontSize = 22.sp,
                        color = if (userCondition) Color(0xFF2196F3) else Color(0xFF1AB277),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = if (userCondition) R.drawable.ic_walk else R.drawable.ic_home),
                        contentDescription = null,
                        tint = if (userCondition) Color(0xFF2196F3) else Color(0xFF1AB277),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 알림 카드
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth() // << 수정됨 5: 고정 높이 제거
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_alarm),
                            contentDescription = "알림 아이콘",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "일정 관리", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "알림 추가",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { navController.navigate("alarm") }
                    )
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // << 수정됨 6: 내부 Column에서 verticalScroll 제거
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (isAlertLoading || !isDataLoaded) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF2196F3)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAlertLoading) "일정을 불러오는 중..." else "일정을 준비하는 중...",
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                        }
                    } else if (todayAlerts.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth() // << 수정됨: 너비 채우기
                                .padding(vertical = 16.dp), // << 수정됨: 패딩 추가
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "오늘의 알림이 없습니다.",
                                fontSize = 22.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        todayAlerts.forEachIndexed { index, alert ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) { // << 수정됨: 패딩값 조정
                                Text(text = "${alert.date} ${alert.time}", fontSize = 26.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = alert.content, fontSize = 28.sp)
                                if (index < todayAlerts.size - 1) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    viewModel.fetchUserFromServer()
                    alertRepository.forceRefreshFromServer()
                }
            ) {
                Text("새로고침", fontSize = 16.sp)
            }
        }

        // << 수정됨: 프로필 수정 버튼과 로딩/에러 메시지 사이 간격 추가
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onUpdateProfileClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277)),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Text("프로필 수정하기", fontSize = 22.sp, color = Color.White)
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "로딩 중...", fontSize = 22.sp, color = Color.Gray)
        }
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, fontSize = 22.sp, color = Color.Red)
        }

        // << 수정됨: 화면 하단에 충분한 공간 확보
        Spacer(modifier = Modifier.height(16.dp))
    }
}