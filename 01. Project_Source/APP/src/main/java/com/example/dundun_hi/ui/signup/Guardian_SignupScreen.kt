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
import com.example.dundun_hi.data.SignupRequest

@Composable
fun Guardian_SignupScreen(
    viewModel: SignupViewModel,
    onSignupSuccess: () -> Unit
) {
    val ctx = LocalContext.current

    var name by remember { mutableStateOf("") }

    val phone = viewModel.lastTelNum

    // 0 = 집, 1 = 외출
    var userCondition by remember { mutableStateOf(0) }

    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.Start
    ) {

        Text(
            text = "회원가입",
            fontSize = 65.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(32.dp))

        Text("이름", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("이름을 입력해주세요") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),

            )

        Spacer(Modifier.height(30.dp))

        Text(
            text = "현재 집에 계신가요?",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth()) {
            Button(
                onClick = { userCondition = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userCondition == 0)
                        Color(0xFF1AB277)
                    else
                        Color(0xFFDFDFE0)
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "예",
                    fontSize = 30.sp,
                    color = Color.White

                )
            }
            Spacer(Modifier.width(16.dp))
            Button(
                onClick = { userCondition = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userCondition == 1)
                        Color(0xFF1AB277)
                    else
                        Color(0xFFDFDFE0)
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "아니요",
                    fontSize = 30.sp,
                    color = Color.White
                )
            }
        }
        Spacer(Modifier.height(100.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    Toast.makeText(ctx, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.signup(
                        SignupRequest(
                            //usertype이 일반 유저 회원가입이랑 유일한 다른점
                            user_type       = 1,
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
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text(
                text = "회원가입 완료하기",
                color = Color.White,
                fontSize = 30.sp
            )
        }
    }

    LaunchedEffect(state) {
        if (state is SignupResult.Success) {
            Log.d("SignupFlow", "SignupScreen LaunchedEffect: state is Success")
            onSignupSuccess()
        }
    }
}
