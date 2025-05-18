package com.example.enter_exit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActivityScreen() {
    val activityList = listOf(
        ActivityLog("귀가", "04/01", "오후 5:30"),
        ActivityLog("외출", "04/01", "오후 1:03"),
        ActivityLog("귀가", "03/31", "오후 9:30"),
        ActivityLog("외출", "04/01", "오전 11:30"),
    )

    Column(modifier = Modifier.padding(16.dp)) {
        // 상단 제목
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "든든하이",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 4.dp)
            )
            Image(
                painter = painterResource(R.drawable.ic_home_appbar),
                contentDescription = "홈 아이콘",
                modifier = Modifier.size(34.dp)
            )
        }

        // 활동 내역 타이틀
        Text(
            text = "활동 내역",
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(6.dp))
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 활동 카드 리스트
        LazyColumn {
            items(activityList) { item ->
                ActivityItem(item)
            }
        }
    }
}

@Composable
fun ActivityItem(activity: ActivityLog) {
    val iconRes = when (activity.type) {
        "귀가" -> R.drawable.ic_home_entry
        "외출" -> R.drawable.ic_door_alt
        else -> R.drawable.ic_home_entry
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = activity.type,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${activity.date} ${activity.time}",
            fontSize = 14.sp
        )
    }
}
