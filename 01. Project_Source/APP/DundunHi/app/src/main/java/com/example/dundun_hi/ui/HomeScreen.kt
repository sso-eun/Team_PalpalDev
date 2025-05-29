//HomeScreen
package com.example.dundun_hi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onLoginClick:    () -> Unit,
    onSignupClick:   () -> Unit,
    onGuardianClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Top
    ) {
        // 상단 그룹
        Spacer(modifier = Modifier.height(80.dp))
        Text("든든하이", fontSize = 65.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("저희 앱과", fontSize = 32.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text("구면이세요?", fontSize = 50.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSignupClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text("초면이세요?", fontSize = 50.sp, fontWeight = FontWeight.SemiBold)
        }

        // 여기서 가변 weight 대신 고정 Spacer
        Spacer(modifier = Modifier.height(170.dp))

        // 하단 그룹
        Text("보호자이신가요?", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onGuardianClick,
            modifier = Modifier
                .wrapContentWidth()
                .height(45.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text("보호자용 페이지로 가기", fontSize = 20.sp)
        }

        // 추가 바닥 여백
        Spacer(modifier = Modifier.height(24.dp))
    }
}
