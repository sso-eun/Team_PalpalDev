// app/src/main/java/com/example/dundun_hi/ui/login/LoginScreen.kt

package com.example.dundun_hi.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun LoginScreen(
    vm: LoginViewModel = viewModel(),
    onLoginSuccess: (userNum: String, userId: String) -> Unit,
    onFindIdClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // ViewModel이 관리하는 로그인 응답 상태
    val loginRes by vm.loginState
    val errorMsg by vm.error

    loginRes?.let { response ->
        LaunchedEffect(response) {
            // 서버 응답에 userId가 null이므로, 여기서는 “입력한 name”을 userId로
            val userNumStr = response.userNum
            val userIdFromInput = name
            onLoginSuccess(userNumStr, userIdFromInput)
            vm.clearLoginState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("든든하이", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("로그인", fontSize = 65.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(32.dp))

        Text("이름", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
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

        Text("전화번호", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
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
                // Retrofit 호출 (LoginViewModel에 userId=‘name’, userPw=‘phone’ 전달)
                vm.login(name.trim(), phone.trim())
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text("로그인하기", color = Color.White, fontSize = 30.sp)
        }

        Spacer(Modifier.height(40.dp))

        Text(text = "가입한 이름을 까먹으셨나요?", color = Color.Gray, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "이름 찾기",
            color = Color.Black,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable { onFindIdClick() }
        )

        // 에러 메시지 표시
        errorMsg?.let { msg ->
            Spacer(Modifier.height(16.dp))
            Text(
                text = "로그인 실패: $msg",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
