package com.example.dundun_hi.ui.signup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dundun_hi.data.CodeAuthSendResponse

/**
 * 1단계: 전화번호 입력 및 인증번호 발송 UI
 * 성공(rsCode == 200)이면 onNext() 호출
 */
@Composable
fun AuthPhoneScreen(
    viewModel: SignupViewModel = viewModel(),
    onNext: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    val sendResult by viewModel.sendCodeResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "회원가입",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { input ->
                // 숫자만 입력 가능하도록 필터링
                phone = input.filter { it.isDigit() }
            },
            label = { Text("전화번호를 입력해주세요") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.sendVerificationCode(phone) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "전화번호 인증하기")
            //dddddddddddddd
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 발송 결과 메시지 표시
        sendResult?.let { result: CodeAuthSendResponse ->
            if (result.rsCode == 200) {
                // 성공 시 다음 화면으로 이동
                LaunchedEffect(Unit) {
                    onNext()
                }
            } else {
                Text(
                    text = result.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
