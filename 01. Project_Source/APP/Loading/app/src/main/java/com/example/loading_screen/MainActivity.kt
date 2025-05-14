package com.example.loading_screen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loading_screen.ui.theme.Loading_ScreenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Loading_ScreenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DundunScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        // 3초 후 Toast 메시지 표시
        Handler(Looper.getMainLooper()).postDelayed({
            Toast.makeText(this, "메인 페이지로 이동합니다!", Toast.LENGTH_SHORT).show()
        }, 3000) // 3000ms = 3초
    }
}

@Composable
fun DundunScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 "든든하이" 텍스트
        Text(
            text = "든든하이",
            fontSize = 16.sp,
            color = Color.Black
        )
        // 중앙 "길동님 환영합니다!" 텍스트
        Text(
            text = "길동님 환영합니다!",
            fontSize = 24.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 16.dp)
        )
        // 하단 초록색 버튼
        Button(
            onClick = { /* 버튼 클릭 시 동작 */ },
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text(
                text = "3초뒤 메인페이지 이동",
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DundunScreenPreview() {
    Loading_ScreenTheme {
        DundunScreen()
    }
}