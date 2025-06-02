package com.example.dundun_hi.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import java.util.*

@Composable
fun AddAlarmScreen(navController: NavController) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("든든하이", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "홈",
                tint = Color(0xFF4A90E2),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = Color(0xFFE6F4FB),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "알림 추가",
                modifier = Modifier.padding(12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("날짜", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                DatePickerDialog(
                    context,
                    { _: DatePicker, y, m, d ->
                        selectedDate = String.format("%04d/%02d/%02d", y, m + 1, d)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        ) {
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null)
                },
                placeholder = { Text("날짜 선택하기...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("시간", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                TimePickerDialog(
                    context,
                    { _: TimePicker, h, m ->
                        selectedTime = String.format("%02d:%02d", h, m)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }
        ) {
            OutlinedTextField(
                value = selectedTime,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_clock), contentDescription = null)
                },
                placeholder = { Text("시간 선택하기...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("내용작성", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = contentText,
            onValueChange = { contentText = it },
            placeholder = { Text("내용을 작성해주세요...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (selectedDate.isNotBlank() && selectedTime.isNotBlank() && contentText.isNotBlank()) {
                    AlertRepository.alertList.add(
                        AlertItem(date = selectedDate, time = selectedTime, content = contentText)
                    )
                    navController.popBackStack()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26C4B5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("추가하기", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
