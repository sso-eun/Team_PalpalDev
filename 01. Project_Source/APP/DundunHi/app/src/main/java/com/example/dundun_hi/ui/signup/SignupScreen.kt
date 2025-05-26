package com.example.dundun_hi.ui.signup

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
import com.example.dundun_hi.data.SignupRequest

@Composable
fun SignupScreen(onSignup: (SignupRequest) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    // 0=집, 1=외출 (Int로 선언!)
    var userCondition by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("든든하이", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("회원가입", fontSize = 65.sp, fontWeight = FontWeight.ExtraBold)
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

        Text("현재 집에 계신가요?", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        // Yes/No 버튼
        Row(Modifier.fillMaxWidth()) {
            Button(
                onClick = { userCondition = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userCondition == 0) Color(0xFF1AB277) else Color.LightGray
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("예", fontSize = 20.sp, color = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Button(
                onClick = { userCondition = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userCondition == 1) Color(0xFF1AB277) else Color.LightGray
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("아니오", fontSize = 20.sp, color = Color.White)
            }
        }

        Spacer(Modifier.height(40.dp))
        Button(
            onClick = {
                val req = SignupRequest(
                    user_type       = 0,
                    user_id         = name,
                    user_pw         = phone,
                    user_tel        = phone,
                    user_profile_img= "",
                    user_home_lat   = "",
                    user_home_lot   = "",
                    user_condition  = userCondition
                )
                onSignup(req)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text("회원가입 완료", color = Color.White, fontSize = 25.sp)
        }
    }
}
