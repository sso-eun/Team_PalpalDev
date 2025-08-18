package com.example.dundun_hi.ui.screen

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA)) // 부드러운 배경색
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 헤더 ---
        Header()

        Spacer(modifier = Modifier.height(16.dp))

        // --- 연락처 단축키 슬롯 ---
        contacts.plus(List(3 - contacts.size) { null })
            .take(3)
            .forEachIndexed { index, item ->
                if (item != null) {
                    CallShortcutCard(
                        item = item,
                        onCall = {
                            ctx.startActivity(Intent(Intent.ACTION_DIAL, "tel:${it.phoneNumber}".toUri()))
                        },
                        onEdit = { onAddShortcut(index) }
                    )
                } else {
                    EmptyShortcutCard(
                        onClick = { onAddShortcut(index) }
                    )
                }
            }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 긴급 신고 버튼 ---
        // CallScreen Composable 내부의 긴급 신고 버튼 Row 부분입니다.

        // CallScreen Composable 내부의 긴급 신고 버튼 Row 부분입니다.

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EmergencyButton(
                modifier = Modifier.weight(1f),
                label = "119 긴급신고",
                iconResId = R.drawable.ic_call_119,
                mainColor = Color(0xFFD32F2F),
                // [변경] backgroundColor 파라미터를 삭제했습니다.
                onClick = {
                    ctx.startActivity(Intent(Intent.ACTION_DIAL, "tel:119".toUri()))
                }
            )
            EmergencyButton(
                modifier = Modifier.weight(1f),
                label = "112 경찰신고",
                iconResId = R.drawable.ic_call_112,
                mainColor = Color(0xFF1976D2),
                // [변경] backgroundColor 파라미터를 삭제했습니다.
                onClick = {
                    ctx.startActivity(Intent(Intent.ACTION_DIAL, "tel:112".toUri()))
                }
            )
        }
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // 기존 '든든하이' 로고나 이미지가 있다면 Image() 사용을 추천합니다.
        Text(
            text = "든든하이",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
        Text(
            text = "오늘도 든든한 하루 보내세요!",
            fontSize = 18.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun CallShortcutCard(
    item: CallShortcut,
    onCall: (CallShortcut) -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이름 및 번호 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = item.phoneNumber,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            // 수정 버튼
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "단축키 수정",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 전화 버튼
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)) // 부드러운 녹색
                    .clickable { onCall(item) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "전화 걸기",
                    tint = Color(0xFF4CAF50), // 진한 녹색
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyShortcutCard(
    onClick: () -> Unit
) {
    val stroke = Stroke(
        width = 6f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick)
            .then(
                Modifier.border(
                    width = 2.dp,
                    color = Color(0xFFD0D8E2),
                    shape = RoundedCornerShape(24.dp)
                )
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "단축키 추가",
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "단축키 추가",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }
}


@Composable
private fun EmergencyButton(
    modifier: Modifier = Modifier,
    label: String,
    @DrawableRes iconResId: Int,
    mainColor: Color,
    // [변경] backgroundColor 파라미터가 필요 없으므로 삭제했습니다.
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        // [변경] 배경색을 흰색으로 고정합니다.
        colors = CardDefaults.cardColors(containerColor = Color.White),
        // [변경] 그림자 효과를 추가합니다. (기본값 0.dp -> 6.dp)
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        // [변경] 테두리 색상을 투명도 없이 mainColor 그대로 사용합니다.
        border = BorderStroke(2.dp, mainColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp) // 내부 여백
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp),
                // [변경] 아이콘 색상을 투명도 없이 mainColor 그대로 사용합니다.
                tint = mainColor
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // '119', '112' 숫자 텍스트 (mainColor 사용은 기존과 동일)
                Text(
                    text = label.take(3),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = mainColor,
                    textAlign = TextAlign.Center
                )

                // '긴급신고', '경찰신고' 텍스트
                Text(
                    text = label.drop(4),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}