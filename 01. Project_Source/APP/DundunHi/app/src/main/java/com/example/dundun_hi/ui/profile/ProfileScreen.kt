// app/src/main/java/com/example/dundun_hi/ui/profile/ProfileScreen.kt

package com.example.dundun_hi.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * ProfileScreen: 보기 전용 프로필 화면
 *
 * @param viewModel            ProfileViewModel 인스턴스
 * @param userId               로그인된 사용자 ID(또는 실제 이름)
 * @param onUpdateProfileClick “수정하기” 버튼 클릭 시 호출될 콜백
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    userId: String,
    onUpdateProfileClick: () -> Unit
) {
    // 1) ViewModel 상태(State) 구독
    val userTel by remember { derivedStateOf { viewModel.userTel } }
    val userProfileImg by remember { derivedStateOf { viewModel.userProfileImg } }
    val userCondition by remember { derivedStateOf { viewModel.userCondition } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }

    // 2) 화면이 Compose에 처음 렌더링될 때(fetchUserFromServer 호출)
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
            fontSize = 32.sp,
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
                .height(250.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.padding(16.dp)
            ) {
                // “수정하기” 버튼
                Button(
                    onClick = onUpdateProfileClick,
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(45.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
                ) {
                    Text("수정하기", fontSize = 20.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // (1) 프로필 이미지: 테두리 1px(=1.dp) 추가
                if (userProfileImg.isNotEmpty()) {
                    AsyncImage(
                        model = userProfileImg,
                        contentDescription = "프로필 사진",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                color = Color.Gray,
                                shape = CircleShape
                            )
                    )
                } else {
                    // 빈 사진 자리(기본 회색 원 + 테두리)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCCCCCC))
                            .border(
                                width = 1.dp,
                                color = Color.Gray,
                                shape = CircleShape
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // (2) 실제 로그인된 userId(이름) 표시
                Text(
                    text = "$userId",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                Spacer(modifier = Modifier.height(4.dp))

                // (3) 전화번호: 서버에서 받아온 userTel 표시
                Text(text = userTel, fontSize = 16.sp)
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
                .height(152.dp)
        ) {
            Column {
                // “위치” 헤더
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "위치",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // 사용자 외출 여부를 오른쪽 끝 버튼으로 표시
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "현재 상태:",
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { /* 필요시 클릭 이벤트 처리 */ },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
                    ) {
                        Text(
                            text = if (userCondition) "외출중이에요" else "집에 있어요",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
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
                .height(453.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "알림", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                Column(modifier = Modifier.padding(16.dp)) {
                    val items = listOf(
                        "04/11 오후 3시 30분\n청주 문화센터 노래 교실",
                        "04/11 오후 3시 30분\n청주 문화센터 노래 교실"
                    )
                    items.forEachIndexed { index, text ->
                        Text(text = text, fontSize = 16.sp)
                        if (index != items.lastIndex) {
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
