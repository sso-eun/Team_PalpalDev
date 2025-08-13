package com.example.dundun_hi.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun AddAlarmScreen(navController: NavController) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val alertRepository = remember { AlertRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    // userNum을 SharedPreferences에서 안정적으로 불러오기
    val sharedPreferences = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)

    // 여러 방법으로 userNum 가져오기 시도
    val userNum = remember {
        val fromPrefs = sharedPreferences.getString("user_num", null)?.toIntOrNull()

        // NavController에서도 시도
        val fromNav = try {
            navController.previousBackStackEntry?.arguments?.getString("userNum")?.toIntOrNull()
        } catch (e: Exception) {
            null
        }

        // 가장 확실한 값 선택
        fromPrefs ?: fromNav ?: 0
    }

    val userType = sharedPreferences.getString("user_type", "0")?.toIntOrNull() ?: 0

    // 디버깅을 위한 로그 추가
    LaunchedEffect(Unit) {
        val prefsUserNum = sharedPreferences.getString("user_num", "null")
        val prefsUserType = sharedPreferences.getString("user_type", "null")
        val prefsUserId = sharedPreferences.getString("user_id", "null")

        android.util.Log.d("AddAlarmScreen", "=== SharedPreferences 상태 ===")
        android.util.Log.d("AddAlarmScreen", "user_num: $prefsUserNum")
        android.util.Log.d("AddAlarmScreen", "user_type: $prefsUserType")
        android.util.Log.d("AddAlarmScreen", "user_id: $prefsUserId")
        android.util.Log.d("AddAlarmScreen", "최종 사용할 userNum: $userNum")
        android.util.Log.d("AddAlarmScreen", "최종 사용할 userType: $userType")

        // SharedPreferences의 모든 값 확인
        val allPrefs = sharedPreferences.all
        android.util.Log.d("AddAlarmScreen", "모든 SharedPreferences 값: $allPrefs")

        // SharedPreferences 파일 경로도 확인
        android.util.Log.d("AddAlarmScreen", "SharedPreferences 파일 경로 확인")
    }

    // 가디언인 경우 시니어 번호를 찾기 위한 상태
    var seniorUserNum by remember { mutableStateOf<Int?>(null) }

    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }

    // 가디언인 경우 시니어 번호 찾기
    LaunchedEffect(userNum, userType) {
        if (userType == 1) { // 가디언인 경우
            try {
                val certResponse = com.example.dundun_hi.network.RetrofitClient.memberService.getCertList(
                    page = 1,
                    limit = 10
                )
                if (certResponse.isSuccessful && certResponse.body() != null) {
                    val certList = certResponse.body()!!.results
                    val seniorCert = certList.find { it.guardian_no == userNum }
                    if (seniorCert != null) {
                        seniorUserNum = seniorCert.senior_num
                    }
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("든든하이", fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
                text = "일정 추가",
                modifier = Modifier.padding(12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("날짜", fontWeight = FontWeight.Bold, fontSize = 22.sp)
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
                placeholder = { Text("날짜 선택하기...", fontSize = 20.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("시간", fontWeight = FontWeight.Bold, fontSize = 22.sp)
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
                placeholder = { Text("시간 선택하기...", fontSize = 20.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("내용작성", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = contentText,
            onValueChange = { contentText = it },
            placeholder = { Text("내용을 작성해주세요...", fontSize = 20.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (selectedDate.isNotBlank() && selectedTime.isNotBlank() && contentText.isNotBlank()) {
                    coroutineScope.launch {
                        // 가디언인 경우 시니어 번호 사용, 일반 사용자인 경우 본인 번호 사용
                        val targetUserNum = if (userType == 1 && seniorUserNum != null) seniorUserNum!! else userNum

                        // userNum 검증 및 로그
                        if (targetUserNum <= 0) {
                            android.util.Log.e("AddAlarmScreen", "유효하지 않은 userNum: $targetUserNum")
                            Toast.makeText(context, "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        // API 명세서에 맞는 올바른 형식으로 데이터 준비
                        val dateTime = "$selectedDate $selectedTime"

                        // ISO 8601 DATETIME 형식으로 변환 (API 명세서의 user_date_info 요구사항)
                        val isoDateTime = try {
                            val dateParts = selectedDate.split("/")
                            val timeParts = selectedTime.split(":")
                            "${dateParts[0]}-${dateParts[1]}-${dateParts[2]}T${timeParts[0]}:${timeParts[1]}:00"
                        } catch (e: Exception) {
                            "${selectedDate}T${selectedTime}:00"
                        }

                        try {
                            // API 명세서에 따른 올바른 파라미터 순서
                            // addAlertToServerAndLocal(user_num, user_date_title, user_date_time, user_date_info)
                            val success = alertRepository.addAlertToServerAndLocal(
                                userNum = targetUserNum,         // 실제 userNum 사용
                                title = contentText,              // user_date_title
                                dateTime = dateTime,              // user_date_time
                                dateInfo = isoDateTime            // user_date_info (DATETIME 형식)
                            )

                            // 로그 추가 (디버깅용)
                            android.util.Log.d("AddAlarmScreen", "실제 사용할 targetUserNum: $targetUserNum")
                            android.util.Log.d("AddAlarmScreen", "dateTime: $dateTime")
                            android.util.Log.d("AddAlarmScreen", "isoDateTime: $isoDateTime")
                            android.util.Log.d("AddAlarmScreen", "contentText: $contentText")
                            if (success) {
                                Toast.makeText(context, "일정이 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "서버 저장 실패", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "서버 저장 중 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26C4B5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("추가하기", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}