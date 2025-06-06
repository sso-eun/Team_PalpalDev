package com.example.dundun_hi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.dundun_hi.data.AlertItem
import com.example.dundun_hi.data.AlertRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlertScreen(navController: NavController, alertId: String) {
    val alert = remember { AlertRepository.getAlertById(alertId) }
    
    var date by remember { mutableStateOf(alert?.date ?: "") }
    var time by remember { mutableStateOf(alert?.time ?: "") }
    var content by remember { mutableStateOf(alert?.content ?: "") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // 수정 성공 여부를 추적
    var isUpdateSuccess by remember { mutableStateOf(false) }

    // 수정 성공 시 이전 화면으로 돌아가기
    LaunchedEffect(isUpdateSuccess) {
        if (isUpdateSuccess) {
            navController.navigateUp()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("알림 수정", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "닫기"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Date and Time Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(date.ifEmpty { "날짜 선택" })
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(time.ifEmpty { "시간 선택" })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content Input
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("알림 내용") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (date.isNotEmpty() && time.isNotEmpty() && content.isNotEmpty()) {
                        val updatedAlert = AlertItem(
                            id = alertId,
                            date = date,
                            time = time,
                            content = content
                        )
                        AlertRepository.updateAlert(updatedAlert)
                        isUpdateSuccess = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = date.isNotEmpty() && time.isNotEmpty() && content.isNotEmpty()
            ) {
                Text("수정 완료")
            }
        }

        // Date Picker Dialog
        if (showDatePicker) {
            val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val datePickerState = rememberDatePickerState()
            
            AlertDialog(
                onDismissRequest = { showDatePicker = false },
                icon = { Icon(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null) },
                title = { Text("날짜 선택") },
                text = {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                date = dateFormatter.format(Date(millis))
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("확인")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("취소")
                    }
                },
                containerColor = Color.White,
                iconContentColor = Color(0xFF2196F3),
                titleContentColor = Color.Black,
                textContentColor = Color.DarkGray,
                tonalElevation = 8.dp
            )
        }

        // Time Picker Dialog
        if (showTimePicker) {
            val timePickerState = rememberTimePickerState()
            
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                icon = { Icon(painter = painterResource(id = R.drawable.ic_clock), contentDescription = null) },
                title = { Text("시간 선택") },
                text = {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier.padding(16.dp)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val hour = timePickerState.hour.toString().padStart(2, '0')
                            val minute = timePickerState.minute.toString().padStart(2, '0')
                            time = "$hour:$minute"
                            showTimePicker = false
                        }
                    ) {
                        Text("확인")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("취소")
                    }
                },
                containerColor = Color.White,
                iconContentColor = Color(0xFF2196F3),
                titleContentColor = Color.Black,
                textContentColor = Color.DarkGray,
                tonalElevation = 8.dp
            )
        }
    }
} 