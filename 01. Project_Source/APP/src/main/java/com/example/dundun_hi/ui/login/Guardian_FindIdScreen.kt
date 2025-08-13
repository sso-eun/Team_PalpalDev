package com.example.dundun_hi.ui.login

import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun Guardian_FindIdScreen(
    viewModel: FindIdViewModel = viewModel(),  // 수정: FindIdViewModel 사용
                 onIdFound: (String) -> Unit,
                 onLoginClick:    () -> Unit
) {
    var phone by remember { mutableStateOf("") }

    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("든든하이", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("이름 찾기", fontSize = 65.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(32.dp))

        Text("전화번호", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
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

        Button(
            onClick = {
                viewModel.findIdByPhone(phone)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text("이름 확인하기", color = Color.White, fontSize = 30.sp)
        }

        Spacer(Modifier.height(30.dp))

        // 서버 요청 결과에 따른 UI 처리
        when (state) {

            is FindIdResult.Loading -> {
                Text("로딩 중...", fontSize = 30.sp, color = Color.Gray)
            }
            is FindIdResult.Success -> {
                val foundId = (state as FindIdResult.Success).userId
                Spacer(Modifier.height(40.dp))
                Text(
                    text = "찾은 이름: $foundId",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(Modifier.height(40.dp))
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
                ) {
                    Text("다시 로그인하러 가기", fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
                }

            }
            is FindIdResult.Error -> {
                val msg = (state as FindIdResult.Error).errorMessage
                Text(
                    text = "오류: $msg",
                    fontSize = 16.sp,
                    color = Color.Red
                )
            }
            else -> { /* Idle 상태일 때는 아무 것도 표시하지 않음 */ }
        }
    }
}


