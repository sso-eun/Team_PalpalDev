// Location.kt
package com.example.dundun_hi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
fun LocationScreen() {
    val activities = listOf(
        ActivityItem("귀가", "04/01 오후 5:30", R.drawable.ic_home_location),
        ActivityItem("외출", "04/01 오후 1:03", R.drawable.ic_exit),
        ActivityItem("귀가", "03/31 오후 9:30", R.drawable.ic_home_location),
        ActivityItem("외출", "04/01 오전 11:30", R.drawable.ic_exit)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // ───── 상단 제목 + 아이콘 ─────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "든든하이",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "홈",
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ───── 섹션 제목 ─────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5F1FB), shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text("활동 내역", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ───── 활동 카드 리스트 ─────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            activities.forEach { activity ->
                ActivityCard(item = activity)
            }
        }
    }
}

data class ActivityItem(
    val type: String, // 귀가, 외출
    val time: String,
    val iconRes: Int
)

@Composable
fun ActivityCard(item: ActivityItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.type, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Image(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.type,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.time, fontSize = 14.sp)
        }
    }
}