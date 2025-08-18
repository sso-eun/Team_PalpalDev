//Alert_Screen.kt
package com.example.dundun_hi.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dundun_hi.R
import com.example.dundun_hi.data.AlertItem
import com.example.dundun_hi.data.AlertRepository
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.launch
import android.widget.Toast

@Composable
fun AlarmRecordScreen(navController: NavController) {
    val context = LocalContext.current
    val alertRepository = remember { AlertRepository.getInstance(context) }

    val fullFormat = SimpleDateFormat("yyyy/MM/dd", Locale.KOREA)
    val dayFormat = SimpleDateFormat("dd", Locale.KOREA)
    val dayOfWeekLabels = listOf("일", "월", "화", "수", "목", "금", "토")

    // AlertRepository의 상태를 관찰
    // val alerts by remember { derivedStateOf { alertRepository.alertList } }

    // AlertRepository의 상태를 직접 관찰하여 변경이 즉시 반영되도록 수정
    val alerts = alertRepository.alertList

    // 화면 갱신을 위한 키
    // var updateKey by remember { mutableStateOf(0) }

    // 현재 날짜로 초기화된 캘린더
    var calendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        })
    }

    // 현재 날짜를 선택된 날짜로 설정
    var selectedDate by remember { mutableStateOf(fullFormat.format(calendar.time)) }

    // 주간 날짜 계산
    val weekDates = remember(calendar) {
        val firstDayOfWeek = Calendar.getInstance().apply {
            time = calendar.time
            add(Calendar.DAY_OF_MONTH, -get(Calendar.DAY_OF_WEEK) + 1)
        }

        List(7) { i ->
            Calendar.getInstance().apply {
                time = firstDayOfWeek.time
                add(Calendar.DAY_OF_MONTH, i)
            }
        }
    }

//    // 선택된 날짜의 알림 목록
//    val alertsForDay = remember(alerts, selectedDate, updateKey) {
//        alerts.filter { it.date == selectedDate }
//            .sortedBy { it.time }
//    }
        val alertsForDay = remember(alerts, selectedDate) {
        alerts.filter { it.date == selectedDate }
            .sortedBy { it.time }
    }

    // 수정할 알림을 저장할 상태
    var editingAlert by remember { mutableStateOf<AlertItem?>(null) }

    // 삭제 확인 다이얼로그 상태
    var showDeleteDialog by remember { mutableStateOf(false) }
    var alertToDelete by remember { mutableStateOf<AlertItem?>(null) }

    // ✅ 화면 진입시 새로고침
    LaunchedEffect(Unit) {
        android.util.Log.d("AlarmRecordScreen", "🖥 AlarmRecordScreen 최초 진입")
        alertRepository.refreshFromServer()
    }

    // ✅ 화면이 다시 포커스를 받을 때 갱신
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.lifecycle?.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_RESUME) {
                    alertRepository.refreshFromServer()
                    // updateKey += 1
                }
            }
        })
    }

    // ✅ 디버깅용: 알림 목록이 변경될 때마다 로그 출력
    LaunchedEffect(alerts.size) {
        android.util.Log.d("AlarmRecordScreen", "현재 알림 개수: ${alerts.size}")
        alerts.forEach { alert ->
            android.util.Log.d("AlarmRecordScreen", "알림: ${alert.date} ${alert.time} - ${alert.content}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("든든하이", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "홈",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F4FC), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text("알림 기록", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 날짜 선택 UI
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = {
                    calendar = Calendar.getInstance().apply {
                        time = calendar.time
                        add(Calendar.DAY_OF_MONTH, -7)
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_left),
                        contentDescription = "이전 주"
                    )
                }

                Text(
                    text = "${calendar.get(Calendar.MONTH) + 1}월",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    // 요일
                    Row(modifier = Modifier.fillMaxWidth()) {
                        dayOfWeekLabels.forEach { day ->
                            Text(
                                text = day,
                                fontSize = 20.sp,
                                color = Color.Gray,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 날짜
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weekDates.forEach { cal ->
                            val dateStr = fullFormat.format(cal.time)
                            val isSelected = dateStr == selectedDate
                            val isToday = cal.time == Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.time

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable { selectedDate = dateStr }
                                    .background(
                                        color = when {
                                            isSelected -> Color(0xFF28D5C5)
                                            isToday -> Color(0xFFE8F4FC)
                                            else -> Color.Transparent
                                        },
                                        shape = RoundedCornerShape(18.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayFormat.format(cal.time),
                                    color = when {
                                        isSelected -> Color.White
                                        isToday -> Color(0xFF2196F3)
                                        else -> Color.Black
                                    },
                                    fontSize = 20.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = {
                    calendar = Calendar.getInstance().apply {
                        time = calendar.time
                        add(Calendar.DAY_OF_MONTH, 7)
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = "다음 주"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ 새로고침 버튼 (디버깅/테스트용)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        alertRepository.refreshFromServer()
                        //updateKey += 1
                    }
                ) {
                    Text("새로고침", fontSize = 16.sp)
                }
            }

            // 알림 리스트
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(alertsForDay) { alert ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, Color(0xFF2196F3)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${alert.date} ${alert.time}", fontSize = 18.sp, color = Color.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(alert.content, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }

                            Row {
                                // 수정 버튼
                                IconButton(
                                    onClick = {
                                        editingAlert = alert
                                        navController.navigate("edit_alarm/${alert.id}")
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_edit),
                                        contentDescription = "수정",
                                        tint = Color(0xFF2196F3)
                                    )
                                }

                                // 삭제 버튼
                                IconButton(
                                    onClick = {
                                        alertToDelete = alert
                                        showDeleteDialog = true
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_delete),
                                        contentDescription = "삭제",
                                        tint = Color(0xFF2196F3)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { navController.navigate("add_alarm") },
            containerColor = Color(0xFF58D687),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_plus), contentDescription = "추가")
        }
    }

    // coroutineScope를 AlertDialog보다 먼저 선언해야 됨.
    val coroutineScope = rememberCoroutineScope()

    if (showDeleteDialog && alertToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                alertToDelete = null
            },
            title = { Text("알림 삭제") },
            text = { Text("이 알림을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        alertToDelete?.let { alert ->
                            // 코루틴 스코프 내에서 suspend 함수 호출
                            coroutineScope.launch {
                                val success = alertRepository.deleteAlert(alert)
                                if (success) {
                                    Toast.makeText(context, "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                }
                                // API 호출이 끝난 후 다이얼로그를 닫습니다.
                                showDeleteDialog = false
                                alertToDelete = null
                            }
                        }
                    }
                ) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    alertToDelete = null
                }) { Text("취소") }
            }
        )
    }

    
//    // 삭제 확인 다이얼로그
//    if (showDeleteDialog && alertToDelete != null) {
//        AlertDialog(
//            onDismissRequest = {
//                showDeleteDialog = false
//                alertToDelete = null
//            },
//            title = { Text("알림 삭제") },
//            text = { Text("이 알림을 삭제하시겠습니까?") },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        alertToDelete?.let { alert ->
//                            alertRepository.deleteAlert(alert)
//                            updateKey += 1
//                        }
//                        showDeleteDialog = false
//                        alertToDelete = null
//                    }
//                ) {
//                    Text("삭제")
//                }
//            },
//            dismissButton = {
//                TextButton(
//                    onClick = {
//                        showDeleteDialog = false
//                        alertToDelete = null
//                    }
//                ) {
//                    Text("취소")
//                }
//            }
//        )
//    }
}