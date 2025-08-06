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

@Composable
fun AuthLoadingScreen(
    // --- MainActivity로부터 userId를 받도록 파라미터 추가 ---
    navController: NavController,
    userNum: Int,
    userId: String,
    viewModel: AuthLoadingViewModel = viewModel()
) {
    // --- 화면이 처음 생성될 때 ViewModel에 userId를 설정하는 로직 추가 ---
    LaunchedEffect(key1 = userId) {
        // userNum으로 가족관계 조회
        viewModel.checkAuthStatus(userNum)
        viewModel.setUserName(userId)
    }
    // -------------------------------------------------------------

    val status by viewModel.authStatus.collectAsState()
    val userName by viewModel.userName.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // status 값에 따라 다른 UI를 보여줌
        when (status) {
            // Case 0: 확인 중
            0 -> {
                Text(text = "가족관계 확인 중", fontSize = 40.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "시간이 소요될 수 있습니다!", fontSize = 20.sp, color = Color(0xFF1AB277))
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator()
            }
            // Case 1: 승인
            1 -> {
                Text(text = "$userName 님 환영합니다!", fontSize = 40.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))

                // TODO: "시니어 정보 확인하기" 클릭 시 시니어 정보 화면으로 이동하는 로직 추가
                // 이거 로딩페이지에서  guardian_auth_upload 조회했을 때 받는 senior_num을 회원 조회 API에 전달해서 사용하면 됨

                Text(text = "시니어 정보 확인하기", fontSize = 20.sp, color = Color(0xFF1AB277))
            }
            // Case 2: 반려
            2 -> {
                Text(text = "가족관계 확인이\n반려되었습니다.", fontSize = 40.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))

                // "가족관계확인 페이지로 돌아가기" 버튼
                Button(onClick = {
                    // FamilyCertificationScreen으로 돌아감
                    navController.popBackStack()
                }) {
                    Text("가족관계확인 페이지로 돌아가기", color = Color.Red)
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