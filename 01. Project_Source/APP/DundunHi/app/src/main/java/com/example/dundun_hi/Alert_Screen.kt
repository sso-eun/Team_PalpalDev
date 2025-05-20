package com.example.dundun_hi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.Alignment

@Composable
fun AlertScreen(modifier: Modifier = Modifier) {
    val selectedDate = remember { mutableStateOf("27") }

    val alerts = listOf(
        AlertItem("04/27 오전 9:30", "혈압 약 먹기"),
        AlertItem("04/27 오후 9:30", "노래 교실")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 상단 제목 + 아이콘 (든든하이 + 홈)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "든든하이",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp)) // 텍스트와 아이콘 간격
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "홈",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        // 알림 기록 탭 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8F4FC), shape = RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "알림 기록",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 날짜 선택 바
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            val days = listOf("화", "수", "목", "금", "토", "일", "월")
            val dates = listOf("25", "26", "27", "28", "29", "30", "5/1")

            days.zip(dates).forEach { (day, date) ->
                val isSelected = selectedDate.value == date
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedDate.value = date },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = day, fontSize = 14.sp)
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(
                                color = if (isSelected) Color(0xFF28D5C5) else Color.Transparent,
                                shape = RoundedCornerShape(15.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date,
                            fontSize = 14.sp,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 알림 카드 목록
        LazyColumn {
            items(alerts) { alert ->
                AlertCard(alert)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

data class AlertItem(val time: String, val title: String)

@Composable
fun AlertCard(alert: AlertItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9F9F9), shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(text = alert.time, fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = alert.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
