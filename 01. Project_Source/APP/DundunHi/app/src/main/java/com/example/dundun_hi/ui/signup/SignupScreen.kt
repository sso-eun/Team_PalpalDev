package com.example.dundun_hi.ui.signup

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dundun_hi.data.SignupRequest

@Composable
fun SignupScreen(
    viewModel: SignupViewModel,
    onSignupSuccess: () -> Unit
) {
    val ctx = LocalContext.current

    var name by remember { mutableStateOf("") }
    // SMS 인증에서 ViewModel에 저장된 전화번호 사용
    val phone = viewModel.lastTelNum

    // 0 = 집, 1 = 외출
    var userCondition by remember { mutableStateOf(0) }

    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("회원가입", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름을 입력해주세요") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(24.dp))

        Text("현재 집에 계신가요?", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Button(
                onClick = { userCondition = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userCondition == 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.weight(1f)
            ) { Text("예") }
            Spacer(Modifier.width(16.dp))
            Button(
                onClick = { userCondition = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userCondition == 1)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.weight(1f)
            ) { Text("아니요") }
        }
        Spacer(Modifier.height(40.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    Toast.makeText(ctx, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.signup(
                        SignupRequest(
                            user_type       = 0,
                            user_id         = name,
                            user_pw         = phone,
                            user_tel        = phone,
                            user_profile_img= "",
                            user_home_lat   = "",
                            user_home_lot   = "",
                            user_condition  = userCondition
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("회원가입 완료", style = MaterialTheme.typography.titleMedium)
        }
    }

    LaunchedEffect(state) {
        if (state is SignupResult.Success) {
            Log.d("SignupFlow", "SignupScreen LaunchedEffect: state is Success")
            onSignupSuccess()
        }
    }
}
