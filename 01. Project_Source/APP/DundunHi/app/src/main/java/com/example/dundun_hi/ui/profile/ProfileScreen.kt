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
//    val today = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date()) }

//    // ✅ 수정: AlertRepository의 상태를 관찰하고 화면 갱신시 새로고침
//    val alerts = alertRepository.alertList
    val isAlertLoading = alertRepository.isLoading
//

    //소은 수정. remember 제거
    val alerts = alertRepository.alertList          // SnapshotStateList 그대로 읽기
    val today = remember { SimpleDateFormat("yyyy/MM/dd", Locale.KOREA).format(Date()) }

    val todayAlerts = alerts
        .filter { it.date == today }
        .sortedBy { it.time }



    // ✅ 디버깅을 위한 로그 추가
    LaunchedEffect(alerts) {
        Log.d("ProfileScreen", "=== 알림 데이터 상태 ===")
        Log.d("ProfileScreen", "전체 알림 개수: ${alerts.size}")
        Log.d("ProfileScreen", "오늘 날짜: $today")
//        Log.d("ProfileScreen", "로딩 상태: $isAlertLoading")

        alerts.forEachIndexed { index, alert ->
            Log.d("ProfileScreen", "[$index] ${alert.date} ${alert.time} - ${alert.content}")
        }
    }
    
//    val todayAlerts = remember(alerts, today) {
//        val filtered = alerts.filter { it.date == today }.sortedBy { it.time }
//        Log.d("ProfileScreen", "오늘 알림 필터링 결과: ${filtered.size}개")
//        filtered.forEachIndexed { index, alert ->
//            Log.d("ProfileScreen", "오늘 알림 [$index]: ${alert.date} ${alert.time} - ${alert.content}")
//        }
//        filtered
//    }


    // ✅ 강화된 데이터 로딩 로직
    var isDataLoaded by remember { mutableStateOf(false) }
    var dataLoadAttempts by remember { mutableStateOf(0) }
    
    // ✅ AlertRepository 콜백 등록
    DisposableEffect(Unit) {
        val callback = {
            Log.d("ProfileScreen", "AlertRepository 데이터 로딩 완료 콜백 실행")
            isDataLoaded = true
            dataLoadAttempts += 1
        }
        
        alertRepository.onDataLoaded(callback)
        
        // 화면이 사라질 때 콜백 제거
        onDispose {
            alertRepository.removeDataLoadedCallback(callback)
        }
    }
    
    // ✅ 화면 진입시마다 데이터 새로고침
    LaunchedEffect(Unit) {
        Log.d("ProfileScreen", "=== 화면 진입 - 데이터 로딩 시작 ===")
        
        // SharedPreferences에서 userNum 확인
        val sharedPrefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val userNum = sharedPrefs.getString("user_num", "null")
        val userType = sharedPrefs.getString("user_type", "null")
        val userId = sharedPrefs.getString("user_id", "null")
        
        Log.d("ProfileScreen", "SharedPreferences 확인:")
        Log.d("ProfileScreen", "user_num: $userNum")
        Log.d("ProfileScreen", "user_type: $userType")
        Log.d("ProfileScreen", "user_id: $userId")
        
        viewModel.fetchUserFromServer()
        alertRepository.forceRefreshFromServer() // 강제 새로고침 사용
    }

    // ✅ 화면이 다시 포커스를 받을 때 갱신
    LaunchedEffect(Unit) {
//        navController.currentBackStackEntry?.lifecycle?.addObserver(object : LifecycleEventObserver {
//            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//                if (event == Lifecycle.Event.ON_RESUME) {
//                    Log.d("ProfileScreen", "화면 재진입 - 데이터 새로고침")
//                    viewModel.fetchUserFromServer()
//                    alertRepository.forceRefreshFromServer() // 강제 새로고침 사용
//                }
//            }
//        })
    }

//    // ✅ 데이터 로딩 상태 모니터링
//    LaunchedEffect(alerts.size, isDataLoaded) {
//        Log.d("ProfileScreen", "알림 데이터 상태 변경 - 전체: ${alerts.size}, 오늘: ${todayAlerts.size}")
//        todayAlerts.forEach { alert ->
//            Log.d("ProfileScreen", "오늘 알림: ${alert.date} ${alert.time} - ${alert.content}")
//        }
//    }
//
//    // ✅ 데이터 로딩 완료 후 추가 보장
//    LaunchedEffect(isDataLoaded) {
//        if (isDataLoaded && alerts.isEmpty()) {
//            delay(100) // 데이터 로딩 완료 후에도 데이터가 없으면 추가 시도
//            Log.d("ProfileScreen", "데이터 로딩 완료 후에도 데이터가 없어 추가 시도")
//            alertRepository.forceRefreshFromServer()
//        }
//    }
//
//    // ✅ 데이터가 로드되지 않았을 때 강제 새로고침
//    LaunchedEffect(Unit) {
//        delay(1000) // 1초 후에도 데이터가 없으면 강제 새로고침
//        if (alerts.isEmpty()) {
//            Log.d("ProfileScreen", "1초 후 데이터가 없어 강제 새로고침 실행")
//            alertRepository.forceRefreshFromServer()
//        }
//    }
//
//    // ✅ 추가적인 데이터 로딩 보장
//    LaunchedEffect(Unit) {
//        delay(2000) // 2초 후에도 데이터가 없으면 다시 시도
//        if (alerts.isEmpty() && !isAlertLoading) {
//            Log.d("ProfileScreen", "2초 후에도 데이터가 없어 재시도")
//            alertRepository.forceRefreshFromServer()
//        }
//    }
//
//    // ✅ 콜백 기반 데이터 로딩 보장
//    LaunchedEffect(dataLoadAttempts) {
//        if (dataLoadAttempts == 0 && alerts.isEmpty()) {
//            delay(500) // 0.5초 후에도 콜백이 호출되지 않았으면 강제 새로고침
//            Log.d("ProfileScreen", "콜백이 호출되지 않아 강제 새로고침 실행")
//            alertRepository.forceRefreshFromServer()
//        }
//    }
//
//    // ✅ 최종 보장 로직
//    LaunchedEffect(Unit) {
//        delay(3000) // 3초 후에도 데이터가 없으면 최종 시도
//        if (alerts.isEmpty()) {
//            Log.d("ProfileScreen", "3초 후 최종 시도")
//            alertRepository.forceRefreshFromServer()
//        }
//    }

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

        // ── 알림 카드
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
                     if (isAlertLoading || !isDataLoaded) {
                         // 로딩 중 표시
                         Row(
                             modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
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
                         // ✅ 디버깅 정보 표시
                         Column(
                             modifier = Modifier.padding(vertical = 8.dp),
                             horizontalAlignment = Alignment.CenterHorizontally
                         ) {
                             Text(
                                 text = "오늘의 알림이 없습니다.",
                                 fontSize = 22.sp,
                                 color = Color.Gray
                             )
                             
                             // 디버깅용 정보 (개발 중에만 표시)
                             Text(
                                 text = "전체 알림: ${alerts.size}개",
                                 fontSize = 14.sp,
                                 color = Color.LightGray
                             )
                             Text(
                                 text = "오늘 날짜: $today",
                                 fontSize = 14.sp,
                                 color = Color.LightGray
                             )
                             
                             // 수동 새로고침 버튼
                             TextButton(
                                 onClick = {
                                     Log.d("ProfileScreen", "수동 새로고침 버튼 클릭")
                                     alertRepository.forceRefreshFromServer()
                                 }
                             ) {
                                 Text("다시 시도", fontSize = 14.sp, color = Color(0xFF2196F3))
                             }
                         }
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

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ 새로고침 버튼 (테스트용)
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