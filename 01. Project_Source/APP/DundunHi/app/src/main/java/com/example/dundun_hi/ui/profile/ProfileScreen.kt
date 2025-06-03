// app/src/main/java/com/example/dundun_hi/ui/profile/ProfileScreen.kt

package com.example.dundun_hi.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dundun_hi.R

/**
 * ProfileScreen: 보기 전용 프로필 화면
 *
 * @param viewModel ProfileViewModel을 주입받아 userNum:Int로 서버 데이터를 조회
 * @param userId    로그인된 사용자 ID(또는 이름) → 화면 상단에 표시
 * @param onUpdateProfileClick 수정하기 버튼 클릭 시 호출되는 콜백
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    userId: String,
    onUpdateProfileClick: () -> Unit
) {
    // ViewModel에서 내려오는 사용자 정보
    val userTel by remember { derivedStateOf { viewModel.userTel } }
    val userProfileImg by remember { derivedStateOf { viewModel.userProfileImg } }
    val userCondition by remember { derivedStateOf { viewModel.userCondition } }
    // (필요하다면 위치(lat/lon)도 읽어올 수 있습니다: viewModel.userHomeLat, viewModel.userHomeLon)

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
                .height(280.dp)
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
                    Text("수정하기", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 프로필 이미지 (URL이 비어 있으면 placeholder 아이콘, 아니면 AsyncImage로 로드)
                if (userProfileImg.isNotEmpty()) {
                    AsyncImage(
                        model = userProfileImg,
                        contentDescription = "프로필 사진",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                } else {
                    // placeholder 회색 원 + 카메라 아이콘
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCCCCCC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_camera),
                            contentDescription = "프로필 사진 없음",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 실제 로그인된 userId(이름)를 여기서 표시
                Text(
                    text = userId,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                Spacer(modifier = Modifier.height(8.dp))

                // 사용자 전화번호 (ViewModel에서 받아온 userTel)
                Text(
                    text = userTel.ifEmpty { "전화번호가 설정되지 않음" },
                    fontSize = 16.sp
                )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "위치", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    // (+) 버튼 공간은 추후 추가 가능
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 실제 외출 상태는 서버에서 받아온 userCondition 으로 분기
                    Text(
                        text = if (userCondition) "외출 중 입니다." else "집에 있습니다.",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
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
                    // (+) 버튼 공간은 추후 추가 가능
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
