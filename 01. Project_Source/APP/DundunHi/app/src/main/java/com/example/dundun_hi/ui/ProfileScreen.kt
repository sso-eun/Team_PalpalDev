package com.example.dundun_hi.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dundun_hi.R
import com.example.dundun_hi.data.AlertRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F0FA))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("든든하이", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "홈",
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 프로필 카드
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.size(80.dp)) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color(0xFFCCCCCC), CircleShape)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_camera_small),
                            contentDescription = "카메라",
                            modifier = Modifier
                                .size(24.dp)
                                .offset(x = 4.dp, y = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("김숙자", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("010-1234-5678", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 위치 카드
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = "위치",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("위치", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "추가",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { navController.navigate("activity_history") }
                    )
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("외출 중 입니다.", fontSize = 16.sp, textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 알림 카드 (오늘 날짜만 필터링)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_alarm),
                        contentDescription = "알림",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("알림", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "추가",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { navController.navigate("alarm") }
                    )
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                Column(modifier = Modifier.padding(16.dp)) {
                    val today = remember {
                        val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.KOREA)
                        formatter.format(Date())
                    }

                    val todayAlerts = remember {
                        AlertRepository.alertList
                            .filter { it.date == today }
                            .sortedBy { it.time }
                    }

                    if (todayAlerts.isEmpty()) {
                        Text("오늘 등록된 알림이 없습니다.", fontSize = 16.sp, textAlign = TextAlign.Center)
                    } else {
                        todayAlerts.forEachIndexed { index, alert ->
                            Text("${alert.date} ${alert.time}\n${alert.content}", fontSize = 16.sp)
                            if (index != todayAlerts.lastIndex) {
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
}
