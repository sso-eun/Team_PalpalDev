package com.example.dundun_hi.ui.signup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SeniorInfoScreen(
    onConfirm: () -> Unit = {}
) {
    var elderName by remember { mutableStateOf("") }
    var elderPhone by remember { mutableStateOf("") }
    var elderAddress by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // 상단 든든하이
        Text(
            text = "든든하이",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        
        // 회원가입 중앙
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "회원가입",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "어르신 정보",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(32.dp))
        
        // 어르신 이름
        Text(
            text = "어르신 이름",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = elderName,
            onValueChange = { elderName = it },
            placeholder = { Text("이름을 입력해주세요") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(24.dp))
        
        // 어르신 전화번호
        Text(
            text = "어르신 전화번호",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = elderPhone,
            onValueChange = { elderPhone = it },
            placeholder = { Text("전화번호를 입력해주세요") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(24.dp))
        
        // 어르신 주소
        Text(
            text = "어르신 주소",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = elderAddress,
            onValueChange = { elderAddress = it },
            placeholder = { Text("주소를 입력해주세요") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(Modifier.height(88.dp))
        
        // 확인 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { onConfirm() },
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
            ) {
                Text(
                    text = "확인",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

