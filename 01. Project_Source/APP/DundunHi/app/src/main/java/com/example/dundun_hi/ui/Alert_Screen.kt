//Alert_Screen
package com.example.dundun_hi.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dundun_hi.R

@Composable
fun AlarmRecordScreen(navController: NavController, modifier: Modifier = Modifier) {
    val selectedDate = remember { mutableStateOf("27") }

    val alerts = mapOf(
        "27" to listOf(
            AlertItem("04/27 오전 9:30", "혈압 약 먹기"),
            AlertItem("04/27 오후 9:30", "노래 교실")
        ),
        "25" to listOf(
            AlertItem("04/25 오전 10:00", "병원 진료")
        )
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("든든하이", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "홈",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F4FC), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text("알림 기록", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("4월", fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterVertically))
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
                                fontSize = 25.sp,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(alerts[selectedDate.value] ?: emptyList()) { alert ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, if (alert.title == "혈압 약 먹기") Color(0xFF2196F3) else Color.Transparent),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = alert.time, fontSize = 14.sp, color = Color.Black)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = alert.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = {
                navController.navigate("add_alarm")
            },
            containerColor = Color(0xFF58D687),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus),
                contentDescription = "추가"
            )
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
