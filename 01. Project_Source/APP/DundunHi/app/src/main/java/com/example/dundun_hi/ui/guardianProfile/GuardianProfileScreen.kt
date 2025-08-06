// app/src/main/java/com/example/dundun_hi/ui/guardian/GuardianProfileScreen.kt

package com.example.dundun_hi.ui.guardianProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dundun_hi.R

/**
 * GuardianProfileScreen: 보호자 프로필 화면 (어르신 정보 포함)
 */
@Composable
fun GuardianProfileScreen(
    viewModel: GuardianProfileViewModel,
    onEditSeniorClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // ViewModel의 상태들을 observe
    val guardianId by remember { derivedStateOf { viewModel.guardianId } }
    val guardianTel by remember { derivedStateOf { viewModel.guardianTel } }
    val seniorId by remember { derivedStateOf { viewModel.seniorId } }
    val seniorTel by remember { derivedStateOf { viewModel.seniorTel } }
    val seniorProfileImg by remember { derivedStateOf { viewModel.seniorProfileImg } }
    val seniorAddress by remember { derivedStateOf { viewModel.seniorAddress } }
    val seniorCondition by remember { derivedStateOf { viewModel.seniorCondition } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F0FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── 상단 타이틀 ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "든든하이 보호자",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            // 로그아웃 버튼
            TextButton(onClick = onLogoutClick) {
                Text(
                    text = "로그아웃",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            // 로딩 상태
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF1AB277))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "정보를 불러오는 중...",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        } else if (errorMessage != null) {
            // 오류 상태
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "오류가 발생했습니다",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.refresh() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
                    ) {
                        Text("다시 시도", color = Color.White)
                    }
                }
            }
        } else {
            // ── 보호자 정보 카드 ─────────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "보호자 정보",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1AB277)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = guardianId,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = guardianTel,
                        fontSize = 20.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 어르신 정보 카드 ─────────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "어르신 정보",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )

                        Button(
                            onClick = onEditSeniorClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "정보 수정",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 프로필 이미지와 기본 정보
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 프로필 이미지
                        if (seniorProfileImg.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFCCCCCC))
                                    .border(1.dp, Color.Gray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_default_profile),
                                    contentDescription = "기본 프로필",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        } else {
                            AsyncImage(
                                model = seniorProfileImg,
                                contentDescription = "어르신 프로필",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.Gray, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = seniorId,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = seniorTel,
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 현재 상태
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (seniorCondition) R.drawable.ic_walk else R.drawable.ic_home
                            ),
                            contentDescription = null,
                            tint = if (seniorCondition) Color(0xFF2196F3) else Color(0xFF1AB277),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (seniorCondition) "외출중이에요" else "집에 있어요",
                            fontSize = 20.sp,
                            color = if (seniorCondition) Color(0xFF2196F3) else Color(0xFF1AB277),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 집 주소
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_location),
                                contentDescription = null,
                                tint = Color(0xFF1AB277),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "집 주소",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = seniorAddress,
                            fontSize = 16.sp,
                            color = Color.Black,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}