package com.example.loading_screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loading_screen.ui.theme.Loading_ScreenTheme

class MainPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Loading_ScreenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainPageScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainPageScreen(modifier: Modifier = Modifier) {
    Text(
        text = "메인 페이지입니다!",
        fontSize = 24.sp,
        color = Color.Black,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun MainPageScreenPreview() {
    Loading_ScreenTheme {
        MainPageScreen()
    }
}