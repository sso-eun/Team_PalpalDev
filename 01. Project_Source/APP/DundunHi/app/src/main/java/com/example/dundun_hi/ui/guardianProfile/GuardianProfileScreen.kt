package com.example.dundun_hi.ui.guardianProfile

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.dundun_hi.R
import com.example.dundun_hi.data.AlertItem
import com.example.dundun_hi.data.AlertRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GuardianProfileScreen(
    viewModel: GuardianProfileViewModel,
    onEditSeniorClick: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val guardianId by remember { derivedStateOf { viewModel.guardianId } }
    val guardianTel by remember { derivedStateOf { viewModel.guardianTel } }
    val guardianProfileImg by remember { derivedStateOf { viewModel.guardianProfileImg } }
    val seniorId by remember { derivedStateOf { viewModel.seniorId } }
    val seniorTel by remember { derivedStateOf { viewModel.seniorTel } }
    val seniorProfileImg by remember { derivedStateOf { viewModel.seniorProfileImg } }
    val seniorAddress by remember { derivedStateOf { viewModel.seniorAddress } }
    val seniorCondition by remember { derivedStateOf { viewModel.seniorCondition } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }

    val alertRepository = remember { AlertRepository.getInstance(context) }

//    val today = remember {
//        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
//    }
//    val alerts by remember { derivedStateOf { alertRepository.alertList } }
//    val todayAlerts = remember(alerts) {
//        alerts.filter { it.date == today }.sortedBy { it.time }
//    }

    //소은 수정. remember 제거
    val alerts = alertRepository.alertList          // SnapshotStateList 그대로 읽기
    val today = remember { SimpleDateFormat("yyyy/MM/dd", Locale.KOREA).format(Date()) }

    val todayAlerts = alerts
        .filter { it.date == today }
        .sortedBy { it.time }

    // imageVersion 관찰 추가
    val imageVersion by remember { derivedStateOf { viewModel.imageVersion } }

    // imageModel 생성
    val guardianImageModel = remember(guardianProfileImg, imageVersion) {
        if (guardianProfileImg.isNullOrEmpty()) {
            null
        } else {
            ImageRequest.Builder(context)
                .data(guardianProfileImg)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.WRITE_ONLY)
                .build()
        }
    }

    val seniorImageModel = remember(seniorProfileImg, imageVersion) {
        if (seniorProfileImg.isNullOrEmpty()) {
            null
        } else {
            ImageRequest.Builder(context)
                .data(seniorProfileImg)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.WRITE_ONLY)
                .build()
        }
    }

    LaunchedEffect(Unit) {
        alertRepository.forceRefreshFromServer()
        viewModel.loadProfileData()
    }

    // MainScreen으로 이동하는 공통 함수
    val navigateToMain = {
        val sharedPreferences = context.getSharedPreferences("user_prefs", 0)
        val userNum = sharedPreferences.getString("user_num", "0") ?: "0"
        val userId = sharedPreferences.getString("user_id", "") ?: ""

        if (userId.isNotEmpty() && userNum != "0") {
            navController.navigate("main/$userNum/${Uri.encode(userId)}") {
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F0FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "든든하이" 텍스트와 홈 아이콘을 묶은 Row
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

            Text("마이페이지", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            TextButton(
                onClick = {
                    // 로그아웃 처리 - home으로 돌아가기
                    navController.navigate("home") {
                        popUpTo("guardian_profile/{userNum}") { inclusive = true }
                    }
                }
            ) {
                Text("로그아웃", fontSize = 16.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF1AB277))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("정보를 불러오는 중...", fontSize = 16.sp, color = Color.Gray)
                }
            }
        } else if (errorMessage != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("오류가 발생했습니다", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.loadProfileData() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
                    ) {
                        Text("다시 시도", color = Color.White)
                    }
                }
            }
        } else {
            GuardianProfileCard(
                guardianId = guardianId,
                guardianTel = guardianTel,
                guardianImageModel = guardianImageModel,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeniorProfileCard(
                seniorId = seniorId,
                seniorTel = seniorTel,
                seniorImageModel = seniorImageModel,
                seniorCondition = seniorCondition,
                seniorAddress = seniorAddress,
                onEditSeniorClick = onEditSeniorClick,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)   // ⬅ 가운데 정렬
            )

            Spacer(modifier = Modifier.height(24.dp))

            ScheduleCard(todayAlerts = todayAlerts, navController = navController)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("guardian_update_profile/${viewModel.guardianUserNum}")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text("내 정보 수정하기", fontSize = 18.sp, color = Color.White)
        }
    }
}

@Composable
fun GuardianProfileCard(
    guardianId: String,
    guardianTel: String,
    guardianImageModel: ImageRequest?,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth() // 전체 폭
            .height(200.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // ⬅ 내부 Column도 전체 폭 차지
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // ⬅ 내용 가로 중앙 정렬
            verticalArrangement = Arrangement.Top
        ) {
            if (guardianImageModel == null) {
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
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                AsyncImage(
                    model = guardianImageModel,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Gray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(guardianId, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(guardianTel, fontSize = 20.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SeniorProfileCard(
    seniorId: String,
    seniorTel: String,
    seniorImageModel: ImageRequest?,
    seniorCondition: Boolean,
    seniorAddress: String,
    onEditSeniorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 헤더: 연결된 계정 + 편집 아이콘
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("연결된 계정", fontSize = 20.sp, color = Color(0xFF9E9E9E), fontWeight = FontWeight.SemiBold)
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit), // 연필 아이콘 리소스
                    contentDescription = "수정",
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onEditSeniorClick() }
                )
            }

            Spacer(Modifier.height(12.dp))

            // 프로필 행
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 아바타
                if (seniorImageModel == null) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                            .border(1.dp, Color.Gray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_default_profile),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = seniorImageModel,
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Gray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column {
                    Text(seniorId, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("번호: $seniorTel", fontSize = 16.sp, color = Color(0xFF424242))
                    Spacer(Modifier.height(4.dp))
                    Text("주소:  $seniorAddress", fontSize = 16.sp, color = Color(0xFF424242))
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = Color(0xFFE0E0E0))

            // 위치 섹션
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("위치", fontSize = 20.sp, color = Color(0xFF9E9E9E), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_location), // 핀 아이콘 리소스
                    contentDescription = null,
                    tint = Color(0xFF1AB277),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = if (seniorCondition) "외출 중 입니다." else "자택에 있습니다.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ScheduleCard(todayAlerts: List<AlertItem>, navController: NavController) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_alarm),
                        contentDescription = "알림 아이콘",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "일정 관리",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = "알림 추가",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier
                        // .size(24.dp)
                        .size(26.dp)
                        .clickable {
                            navController.navigate("alarm")
                        }
                )
            }

            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(scrollState)
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
                        Column(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "${alert.date} ${alert.time}",
                                // fontSize = 26.sp,
                                fontSize = 24.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = alert.content,
                                // fontSize = 28.sp,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Normal
                            )
                            if (index < todayAlerts.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}