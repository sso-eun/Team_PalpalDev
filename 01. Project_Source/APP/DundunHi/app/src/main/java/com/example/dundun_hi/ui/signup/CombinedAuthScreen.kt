package com.example.dundun_hi.ui.signup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 한 화면에서 SMS 인증 전체 처리
 * 1) 전화번호 입력 → sendCode()
 * 2) 인증번호 입력 → verifyCode()
 * 성공 시 onNext() 호출
 */
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1) 전화번호 입력 & 발송
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it.filter { c -> c.isDigit() } },
            label = { Text("전화번호") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.sendVerificationCode(phone) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("인증번호 받기")
        }
        // 발송 실패 메시지

        Spacer(Modifier.height(24.dp))

        // 2) 인증번호 입력 & 확인
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter { c -> c.isDigit() } },
            label = { Text("인증번호") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.verifyAuthCode(code) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("인증번호 확인")
        }
        // 검증 실패 메시지

    }

    // 인증 성공 시 onNext() 호출
    LaunchedEffect(verifyResult) {
        if (verifyResult?.rsCode == 200) {
            onNext()
        }
    }
}
