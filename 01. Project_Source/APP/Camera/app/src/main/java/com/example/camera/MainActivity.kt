package com.example.camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.ui.theme.CameraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFE6F0FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // 상단 "든든하이" 텍스트와 집 아이콘
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "든든하이",
                    fontSize = 45.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "홈",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(70.dp)
                )

            }


            // "바로 사진찍기" 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(Color(0xFFD6EAF8))
                    .padding(24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "바로 사진찍기",
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = "카메라",
                    modifier = Modifier.size(90.dp)

                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "사진 불러오기" 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(Color(0xFFD6EAF8))
                    .padding(24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "사진 불러오기",
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                    contentDescription = "갤러리",
                    modifier = Modifier.size(90.dp)
                )
            }
        }
    }
}