package com.example.dundun_hi.ui.login

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun LoginScreen(
    navController: NavController,
    vm: LoginViewModel = viewModel(),
    onFindIdClick: () -> Unit,
    onSignupClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val loginResult by vm.loginResult.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(loginResult) {
        when (val result = loginResult) {
            is LoginResult.GoToAuthLoading -> {
                navController.navigate("auth_loading/${result.userNum}/${Uri.encode(result.userId)}/${result.seniorNum}") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is LoginResult.GoToMain -> {
                navController.navigate("main/${result.userNum}/${Uri.encode(result.userId)}") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is LoginResult.Error -> {
                // 에러 메시지는 아래 UI에서 직접 표시
            }
            else -> { /* Idle, Loading */ }
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
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(24.dp))

        Text("전화번호", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = { Text("전화번호를 입력해주세요") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(40.dp))

        Button(
            onClick = { vm.performLoginAndRoute(name.trim(), phone.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            if (loginResult is LoginResult.Loading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("로그인하기", color = Color.White, fontSize = 30.sp)
            }
        }

        Spacer(Modifier.height(40.dp))

        // '회원가입' 링크 추가
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "회원가입",
                modifier = Modifier.clickable { onSignupClick() },
                fontSize = 18.sp
            )
            Spacer(Modifier.padding(horizontal = 8.dp))
            Text("|", color = Color.Gray, fontSize = 18.sp)
            Spacer(Modifier.padding(horizontal = 8.dp))
            Text(
                text = "이름 찾기",
                modifier = Modifier.clickable { onFindIdClick() },
                fontSize = 18.sp
            )
        }

        // 기존 에러 메시지 표시 로직 유지
        if (loginResult is LoginResult.Error) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "로그인 실패: ${(loginResult as LoginResult.Error).message}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}