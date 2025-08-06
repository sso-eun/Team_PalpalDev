package com.example.dundun_hi.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun EditAlertScreen(navController: NavController, alertId: String) {
    val context = LocalContext.current
    val alertRepository = remember { AlertRepository.getInstance(context) }
    val alert = remember { alertRepository.getAlertById(alertId) }
    
    var date by remember { mutableStateOf(alert?.date ?: "") }
    var time by remember { mutableStateOf(alert?.time ?: "") }
    var content by remember { mutableStateOf(alert?.content ?: "") }
    
    val calendar = Calendar.getInstance()

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
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                date = String.format("%04d/%02d/%02d", year, month + 1, dayOfMonth)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(date.ifEmpty { "날짜 선택" })
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                time = String.format("%02d:%02d", hourOfDay, minute)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
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
                        alertRepository.updateAlert(updatedAlert)
                        navController.navigateUp()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = date.isNotEmpty() && time.isNotEmpty() && content.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("수정 완료", fontSize = 16.sp, color = Color.White)
            }
        }
    }
} 