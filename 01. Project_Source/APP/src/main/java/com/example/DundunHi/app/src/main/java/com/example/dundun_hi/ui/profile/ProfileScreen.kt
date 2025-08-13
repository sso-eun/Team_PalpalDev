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

// ProfileScreen.kt 수정 버전

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    userId: String,
    onUpdateProfileClick: () -> Unit,
    onUpdatePasswordClick: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val alertRepository = remember { AlertRepository.getInstance(context) }

    // ✅ 수정: imageVersion도 함께 관찰
    val userTel = viewModel.userTel
    val userProfileImg = viewModel.userProfileImg
    val imageVersion by remember { derivedStateOf { viewModel.imageVersion } }
    val userCondition = viewModel.userCondition
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    // ✅ 수정: 이미지 모델을 imageVersion과 함께 생성
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

    // 오늘 날짜
    val today = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date()) }

    // 알림 목록
    val alerts = alertRepository.alertList
    val todayAlerts = remember(alerts, today) {
        alerts.filter { it.date == today }.sortedBy { it.time }
    }

    // ✅ 수정: 화면 진입 시마다 데이터 새로고침
    LaunchedEffect(Unit) {
        viewModel.fetchUserFromServer()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F0FA))
            .padding(16.dp)
    ) {
        Text(text = "든든하이", fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // ── 프로필 카드
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth().height(230.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.padding(16.dp)
            ) {
                // ✅ 수정: imageModel 사용하여 캐싱 문제 해결
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
                    // 기본 프로필 이미지
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
                    textAlign = TextAlign.Center,       // ✅ 가운데 정렬
                    modifier = Modifier.fillMaxWidth()  // ✅ 폭 전체 사용
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
            modifier = Modifier.fillMaxWidth().height(140.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
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

        // ── 알림 카드 (기존 코드 그대로)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth().height(280.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
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
                        modifier = Modifier.size(24.dp).clickable { navController.navigate("alarm") }
                    )
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).verticalScroll(scrollState)
                ) {
                    if (todayAlerts.isEmpty()) {
                        Text(
                            text = "오늘의 알림이 없습니다.",
                            fontSize = 22.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        todayAlerts.forEachIndexed { index, alert ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
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

        // 기존 Row 전체를 삭제하고 위 코드로 교체
        Spacer(modifier = Modifier.height(16.dp)) // 버튼 위 여백
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
    }
}

/*
주요 변경사항:

1. ✅ imageVersion 관찰 추가
   - val imageVersion by remember { derivedStateOf { viewModel.imageVersion } }

2. ✅ imageModel 생성 로직 추가
   - remember(userProfileImg, imageVersion)로 둘 중 하나라도 변경되면 새로운 ImageRequest 생성
   - 캐시 정책: DISABLED/WRITE_ONLY로 설정

3. ✅ AsyncImage에서 imageModel 사용
   - 기존의 복잡한 캐시 설정 대신 imageModel 사용
   - onSuccess/onError 콜백으로 로그 추가

4. ✅ Log import 추가 필요
   - import android.util.Log 추가해야 함

이제 UpdateProfileScreen에서 수정 완료 후 ProfileScreen으로 돌아가면
즉시 새로운 프로필 이미지가 반영됩니다!
*/