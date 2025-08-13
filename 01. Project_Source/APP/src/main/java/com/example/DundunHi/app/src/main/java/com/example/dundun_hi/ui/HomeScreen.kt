// HomeScreen.kt

package com.example.dundun_hi.ui

import androidx.compose.foundation.layout.*
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
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    onOCRClick: () -> Unit
    // onGuardianClick 파라미터 제거
    // 모든 사용자 (가디언 및 시니어) 통합하여 로그인 안내
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

        // 중앙 정렬로 레이아웃 개선
        verticalArrangement = Arrangement.Center
    ) {
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
        // 하단의 '보호자용 페이지' 관련 UI 전체 제거


        //임시_소은_OCR버튼
        Button(
            onClick = onOCRClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text("임시 OCR", fontSize = 50.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}