//Enter_Exit
package com.example.dundun_hi.ui

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
import com.example.dundun_hi.R

@Composable
fun ActivityHistoryScreen() {
    val activities = listOf(
        ActivityItem("귀가", "04/01 오후 5:30", R.drawable.ic_home_small),
        ActivityItem("외출", "04/01 오후 1:03", R.drawable.ic_walk),
        ActivityItem("귀가", "03/31 오후 9:30", R.drawable.ic_home_small),
        ActivityItem("외출", "04/01 오전 11:30", R.drawable.ic_walk),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // 상단 바
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("든든하이", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "홈",
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 제목
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8F4FC), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text("활동 내역", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 활동 카드 목록
        activities.forEach { activity ->
            ActivityCard(activity)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

data class ActivityItem(val label: String, val time: String, val iconRes: Int)

@Composable
fun ActivityCard(activity: ActivityItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(activity.label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Image(
                    painter = painterResource(id = activity.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(activity.time, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}
