package com.example.dundun_hi.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val primaryColor = Color(0xFF1AB277) // 앱의 메인 컬러

    // 애니메이션 상태
    val scaleAnimation = remember { Animatable(0.5f) }
    val alphaAnimation = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        // 스케일 애니메이션
        scaleAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        )
        // 알파 애니메이션
        alphaAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = LinearEasing
            )
        )
        // 2초 후 메인 화면으로 이동
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            // DH 로고
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(scaleAnimation.value)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DH",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 앱 이름
            Text(
                text = "든든하이",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(alphaAnimation.value)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 앱 설명
            Text(
                text = "시니어를 위한 스마트폰 도우미",
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.alpha(alphaAnimation.value)
            )
        }
    }
} 