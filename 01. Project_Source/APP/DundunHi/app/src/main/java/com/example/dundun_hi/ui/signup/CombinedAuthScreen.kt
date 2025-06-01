package com.example.dundun_hi.ui.signup

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
import androidx.compose.runtime.LaunchedEffect
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

/**
 * 한 화면에서 SMS 인증 전체 처리
 * 1) 전화번호 입력 → sendCode()
 * 2) 인증번호 입력 → verifyCode()
 * 성공 시 onNext() 호출
 */
//보호자, 일반 사용자 모두 공통으로 사용하는 전화번호 인증 페이지
@Composable
fun CombinedAuthScreen(
    viewModel: SignupViewModel = viewModel(),
    onNext: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var code  by remember { mutableStateOf("") }

    val sendResult   by viewModel.sendCodeResult.collectAsState()
    val verifyResult by viewModel.verifyCodeResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // ─── “회원가입” 타이틀 추가 ───
        Text(
            text = "회원가입",
            fontSize = 65.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(32.dp))

        // ─── 1) “전화번호” 레이블 ───
        Text(
            text = "전화번호",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        // ─── 전화번호 입력란 ───
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it.filter { c -> c.isDigit() } },
            placeholder = { Text("전화번호를 입력해주세요") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),

        )
        Spacer(Modifier.height(40.dp))

        // ─── “인증번호 받기” 버튼 ───
        Button(
            onClick = { viewModel.sendVerificationCode(phone.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text(
                text = "인증번호 받기",
                color = Color.White,
                fontSize = 30.sp
            )
        }

        Spacer(Modifier.height(60.dp))

        // ─── 2) “인증번호” 레이블 ───
        Text(
            text = "인증번호",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        // ─── 인증번호 입력란 ───
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter { c -> c.isDigit() } },
            placeholder = { Text("문자로 발송된 인증번호를 입력해주세요") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),

        )
        Spacer(Modifier.height(40.dp))

        // ─── “인증번호 확인하기” 버튼 ───
        Button(
            onClick = { viewModel.verifyAuthCode(code.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text(
                text = "인증번호 확인하기",
                color = Color.White,
                fontSize = 30.sp
            )
        }
    }

    // 인증 성공 시 onNext() 호출
    LaunchedEffect(verifyResult) {
        if (verifyResult?.rsCode == 200) {
            onNext()
        }
    }
}
