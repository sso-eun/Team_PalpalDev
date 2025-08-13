package com.example.dundun_hi.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GuardianScreen(
    onGuardianFindIdClick:()->Unit,
    onSubmit: (name: String, phone: String) -> Unit = { _, _ -> },
    onSignupClick:()->Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // 작은 헤더
        Text(
            text = "든든하이",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        // 보호자 로그인 타이틀
        Text(
            text = "보호자 로그인",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(32.dp))

        // 이름 입력
        Text(
            text = "이름",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("이름을 입력해주세요") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(24.dp))

        // 전화번호 입력
        Text(
            text = "전화번호",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = { Text("전화번호를 입력해주세요") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(40.dp))

        // 확인 버튼
        Button(
            onClick = { onSubmit(name, phone) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text(
                text = "로그인하기",
                color = Color.White,
                fontSize = 18.sp
            )
        }
        Spacer(Modifier.height(40.dp))

        // 회원가입과 이름찾기를 가로로 정렬
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = "회원가입",
                color = Color.Black,
                fontSize = 18.sp,
                modifier = Modifier.clickable { onSignupClick() }
            )
            Spacer(Modifier.padding(horizontal = 8.dp))
            Text(
                text = "|",
                color = Color.Gray,
                fontSize = 18.sp
            )
            Spacer(Modifier.padding(horizontal = 8.dp))
            Text(
                text = "이름찾기",
                color = Color.Black,
                fontSize = 18.sp,
                modifier = Modifier.clickable { onGuardianFindIdClick() }
            )
        }
    }
}


