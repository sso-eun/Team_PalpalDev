package com.example.dundun_hi.ui.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SeniorInfoScreen(
    viewModel: SeniorProfileViewModel,
    onConfirm: () -> Unit = {}
) {
    val seniorProfile by viewModel.seniorProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val elderName = seniorProfile?.userId ?: ""
    val elderPhone = seniorProfile?.userTel ?: ""
    val elderAddress = seniorProfile?.userHomeLat?.let { lat ->
        seniorProfile?.userHomeLot?.let { lon -> "$lat, $lon" }
    } ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            // 1. Guardian_SignupScreen과 일관성을 위해 vertical padding 수정
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // 2. 화면 상단에 일관된 타이틀 블록 추가
        Text("든든하이", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("어르신 정보", fontSize = 50.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(32.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Text(text = "어르신 이름", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = elderName,
                onValueChange = {},
                readOnly = true,
                // 3. 필드와 라벨 사이 간격을 위해 padding(top = 8.dp) 추가
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray
                )
            )
            Spacer(Modifier.height(24.dp))

            Text(text = "어르신 전화번호", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = elderPhone,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray
                )
            )
            Spacer(Modifier.height(24.dp))

            Text(text = "어르신 주소", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
            if (elderAddress.isBlank()) {
                OutlinedTextField(
                    value = "주소가 입력되어있지 않아요!\n마이페이지에서 입력해주세요!",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Red,
                        disabledBorderColor = Color(0xFF1AB277)
                    )
                )
            } else {
                OutlinedTextField(
                    value = elderAddress,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.Gray
                    )
                )
            }

            // 4. 버튼 위치 조정을 위해 weight Spacer를 고정 Spacer로 변경
            Spacer(Modifier.height(40.dp))

            Button(
                onClick = { onConfirm() },
                modifier = Modifier.fillMaxWidth().height(70.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
            ) {
                Text("확인", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}