package com.example.dundun_hi.ui.signup

// ui/signup/AuthLoadingScreen.kt (새로운 파일)

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dundun_hi.ui.signup.AuthLoadingViewModel
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import com.example.dundun_hi.data.OnboardingManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke


@Composable
fun AuthLoadingScreen(
    // --- MainActivity로부터 userId를 받도록 파라미터 추가 ---
    navController: NavController,
    userNum: Int,
    userId: String,
    seniorNum: Int,
    viewModel: AuthLoadingViewModel = viewModel()
) {

    LaunchedEffect(key1 = userId) {
        // userNum으로 가족관계증명서 현황 조회
        viewModel.checkAuthStatus(userNum)
        viewModel.setSeniorNum(seniorNum)
        viewModel.setUserName(userId)
    }

    val context = LocalContext.current
    val status by viewModel.authStatus.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val finalSeniorNum by viewModel.seniorNum.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // status 값에 따라 다른 UI를 보여줌
        when (status) {

            // Case 0: 확인 중
            0 -> {
                Text(text = "가족관계 확인 중", fontSize = 40.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "시간이 소요될 수 있습니다!",
                    fontSize = 28.sp,
                    color = Color(0xFF1AB277),
                    fontWeight = FontWeight.Bold
                )
                // 로딩표시 - 동글뱅이
                // Spacer(modifier = Modifier.height(24.dp))
                // CircularProgressIndicator()

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        // navController.navigate("main/$userNum/${Uri.encode(userId)}")
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            // popUpTo("auth_loading/{userNum}/{userId}/{seniorNum}") { inclusive = true }
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277)),
                    // border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text("메인으로 돌아가기", color = Color.White, fontSize = 18.sp)
                }
            }


            // Case 1: 승인 (수정된 디자인)
            1 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$userName 님 환영합니다!",
                        fontSize = 36.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // 확인 버튼
                    Button(
                        onClick = {
                            // 1. 다음 화면으로 넘어가기 직전에, '온보딩 완료' 상태를 저장합니다.
                            OnboardingManager.setOnboardingCompleted(context)

                            // 2. 그리고 나서 다음 화면으로 이동합니다.
                            // navController.navigate("senior_profile/$finalSeniorNum")
                            navController.navigate("senior_profile/$userNum/${Uri.encode(userId)}/$finalSeniorNum")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1AB277)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "어르신 정보 확인하러 가기",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            // Case 2: 반려
            2 -> {
                Text(
                    text = "가족관계 확인이\n반려되었습니다.",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 50.sp
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                    // --- popBackStack() 대신 명시적인 경로로 이동 ---
                    // FamilyCertificationScreen으로 이동하기 위한 경로

                    navController.navigate("family_certification") {
                        // 이전 로딩 화면은 스택에서 제거
                        popUpTo("auth_loading/{userNum}/{userId}/{seniorNum}") { inclusive = true }
                    }
                    // ---------------------------------------------
                },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                        //.clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1AB277)
                        ),
                        shape = RoundedCornerShape(28.dp)
                ){
                    Text(
                        "가족관계증명서 다시 업로드 하기",
                        color = Color.White,
                        fontSize = 18.sp,
                        // fontWeight = FontWeight.Medium,
                        )
                }
            }
            // null: 초기 로딩 상태
            else -> {
                CircularProgressIndicator()
                Text("인증 상태를 확인하고 있습니다...")
            }
        }
    }
}