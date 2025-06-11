package com.example.dundun_hi.ui.screen

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.dundun_hi.R
import com.example.dundun_hi.model.CallShortcut

@Composable
fun CallScreen(
    contacts: List<CallShortcut>,
    onAddShortcut: (Int) -> Unit
) {
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ── 타이틀 (왼쪽 고정)
        Text(
            text = "든든하이",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            modifier = Modifier.padding(start = 8.dp)
        )

        // ── 연락처 슬롯 (항상 3개)
        contacts.plus(List(3 - contacts.size) { null })
            .take(3)
            .forEachIndexed { idx, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp), // 높이를 더 키움
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F0FA)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 28.dp), // 좌우 패딩 증가
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 왼쪽: 전화 아이콘
                        Icon(
                            painter = painterResource(R.drawable.ic_call),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp) // 아이콘 크기 증가
                                .clickable {
                                    item?.let {
                                        ctx.startActivity(
                                            Intent(Intent.ACTION_DIAL, "tel:${it.phoneNumber}".toUri())
                                        )
                                    }
                                },
                            tint = if (item != null) Color(0xFF4CAF50) else Color(0xFFDDDDDD)
                        )

                        // 중앙: 이름
                        Text(
                            text = item?.label ?: "단축키 등록",
                            fontSize = 36.sp, // 글자 크기 조정
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 32.dp), // 좌우 여백 증가
                            maxLines = 1 // 한 줄로 제한
                        )

                        // 오른쪽: 수정/등록 버튼
                        Icon(
                            painter = painterResource(R.drawable.ic_plus),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp) // 아이콘 크기 증가
                                .clickable { onAddShortcut(idx) },
                            tint = Color(0xFF666666)
                        )
                    }
                }
            }

        Spacer(modifier = Modifier.height(32.dp))

        // ── 긴급 신고 버튼들
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
        ) {
            EmergencyButton(
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp), // 높이 고정으로 변경
                label = "119신고",
                iconRes = R.drawable.ic_call_119,
                borderColor = Color(0xFFF27A54)
            ) {
                ctx.startActivity(Intent(Intent.ACTION_DIAL, "tel:119".toUri()))
            }
            EmergencyButton(
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp), // 높이 고정으로 변경
                label = "112신고",
                iconRes = R.drawable.ic_call_112,
                borderColor = Color(0xFF3B6FE0)
            ) {
                ctx.startActivity(Intent(Intent.ACTION_DIAL, "tel:112".toUri()))
            }
        }
    }
}

@Composable
private fun EmergencyButton(
    modifier: Modifier = Modifier,
    label: String,
    iconRes: Int,
    borderColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // 패딩 증가
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = label,
                fontSize = 45.sp, // 글자 크기 증가
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(72.dp) // 아이콘 크기 증가
            )
        }
    }
} 