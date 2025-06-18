// app/src/main/java/com/example/dundun_hi/ui/profile/ProfileScreen.kt

package com.example.dundun_hi.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dundun_hi.R
import com.example.dundun_hi.data.AlertRepository
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.layout.ContentScale

/**
 * ProfileScreen: 보기 전용 프로필 화면
 *
 * @param viewModel               ProfileViewModel 인스턴스
 * @param userId                  로그인된 사용자 ID (서버에서 받아온 값)
 * @param onUpdateProfileClick    "프로필 수정하기" 버튼 클릭 시 호출될 콜백
 * @param onUpdatePasswordClick   "비밀번호 수정하기" 버튼 클릭 시 호출될 콜백
 * @param navController           NavController instance for navigation
 */
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

    val userTel by remember { derivedStateOf { viewModel.userTel } }
    val userProfileImg by remember { derivedStateOf { viewModel.userProfileImg } }
    val userCondition by remember { derivedStateOf { viewModel.userCondition } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }

    // 오늘 날짜의 알림만 필터링
    val today = remember { 
        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
    }
    
    // AlertRepository의 알림 목록을 관찰
    val alerts by remember { derivedStateOf { alertRepository.alertList } }
    
    // 오늘 날짜의 알림만 필터링
    val todayAlerts = remember(alerts) {
        alerts.filter { it.date == today }
            .sortedBy { it.time }
    }

    // Compose가 처음 렌더링될 때 서버에서 사용자 정보를 가져옴
    LaunchedEffect(Unit) {
        viewModel.fetchUserFromServer()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F0FA))
            .padding(16.dp)
    ) {
        // ── 상단 타이틀 ───────────────────────────────────────────────────
        Text(
            text = "든든하이",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── 프로필 카드 ─────────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.padding(16.dp)
            ) {
                // 프로필 이미지: 테두리 추가
                if (userProfileImg.isNullOrEmpty()) {
                    // 기본 회색 원 (사진이 없을 경우)
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCCCCCC))
                            .border(
                                width = 1.dp,
                                color = Color.Gray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_default_profile),
                            contentDescription = "기본 프로필",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                    )
                    }
                } else {
                    AsyncImage(
                        model = userProfileImg,
                        contentDescription = "프로필 사진",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                color = Color.Gray,
                                shape = CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 로그인된 아이디(이름) 표시
                Text(
                    text = userId,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                Spacer(modifier = Modifier.height(4.dp))

                // 전화번호 표시
                Text(text = userTel, fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 위치 카드 ───────────────────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Column {
                // "위치" 헤더
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location),
                            contentDescription = "위치 아이콘",
                            tint = Color(0xFF1AB277),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "위치",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "위치 추가",
                        tint = Color(0xFF1AB277),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { 
                                navController.navigate("enter_exit")
                            }
                    )
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // 외출 여부 상태 텍스트 (아이콘+텍스트, 배경 없음)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "현재 상태:",
                        fontSize = 24.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (userCondition) "외출중이에요" else "집에 있어요",
                        fontSize = 22.sp,
                        color = if (userCondition) Color(0xFF2196F3) else Color(0xFF1AB277),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(
                            id = if (userCondition) R.drawable.ic_walk else R.drawable.ic_home
                        ),
                        contentDescription = null,
                        tint = if (userCondition) Color(0xFF2196F3) else Color(0xFF1AB277),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 알림 카드 ───────────────────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            Column {
                // 알림 헤더
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
                            text = "알림",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "알림 추가",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { 
                                navController.navigate("alarm")
                            }
                    )
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // 알림 내용을 스크롤 가능한 영역으로 변경
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(scrollState)
                ) {
                    if (todayAlerts.isEmpty()) {
                        // 오늘의 알림이 없을 경우
                        Text(
                            text = "오늘의 알림이 없습니다.",
                            fontSize = 22.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        // 오늘의 알림 목록 표시
                        todayAlerts.forEachIndexed { index, alert ->
                            Column(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${alert.date} ${alert.time}",
                                    fontSize = 26.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = alert.content,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Normal
                                )
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

        // ── 버튼들 ───────────────────────────────────────────────────────────
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // "프로필 수정하기" 버튼
                Button(
                    onClick = onUpdateProfileClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277)),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("프로필 수정하기", fontSize = 22.sp, color = Color.White)
                }

                // "비밀번호 수정하기" 버튼
                Button(
                    onClick = onUpdatePasswordClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277)),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("비밀번호 수정하기", fontSize = 22.sp, color = Color.White)
                }
            }
        }

        // (선택) 로딩/에러 처리 UI
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "로딩 중...", fontSize = 22.sp, color = Color.Gray)
        }
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, fontSize = 22.sp, color = Color.Red)
        }
    }
}
