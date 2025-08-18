package com.example.dundun_hi.ui.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.BorderStroke
//소은. OCR 연결
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.remember
/**
 * 한 화면에서 SMS 인증 전체 처리
 * 1) 전화번호 입력 → sendCode()
 * 2) 인증번호 입력 → verifyCode()
 * 성공 시 onNext() 호출
 */
//보호자, 일반 사용자 모두 공통으로 사용하는 전화번호 인증 페이지
@Composable
fun CombinedAuthScreen(
    viewModel: SignupViewModel = viewModel(),
    userType: Int,
    //ocr 네비컨트롤, 콜백_소은
    onRequestOcr: () -> Unit,
    ocrPrefill: String,
    onNext: () -> Unit,
    // 수정1: 하단에 추가할 UI를 위한 '슬롯' 파라미터 추가
    // 기본값은 비어있으므로, 아무것도 넘겨주지 않으면 아무것도 표시되지 않음
    bottomContent: @Composable () -> Unit = {},



) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code  by remember { mutableStateOf("") }
    var isCodeWrong by remember { mutableStateOf(false) }

    // userType에 따라 제목 텍스트를 결정하는 변수 추가
    val title = if (userType == 1) "보호자 회원가입" else "회원가입"

    val sendResult   by viewModel.sendCodeResult.collectAsState()
    val userCreationState by viewModel.userCreationState.collectAsState()
    // val verifyResult by viewModel.verifyCodeResult.collectAsState()

    LaunchedEffect(userCreationState) {
        if (userCreationState is UserCreationState.Success) {
            onNext()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        //horizontalAlignment = Alignment.CenterHorizontally // 중앙 정렬로 변경
        horizontalAlignment = Alignment.Start
    ) {
        // 상단 든든하이, 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "든든하이",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        // 회원가입 중앙
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 50.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(Modifier.height(24.dp))
        // 이름 입력
        Text(
            text = "이름",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
//        OutlinedTextField(
//            value = name,
//            onValueChange = { name = it },
//            placeholder = { Text("이름을 입력해주세요") },
//            singleLine = true,
////            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp),
//
//            //OCR 추가
//            readOnly = true,
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { onRequestOcr() }
//        )

        //ocr 입력 box 레이어 추가
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },   // 직접 입력 허용하려면 유지
                readOnly = true,                 // 탭하면 OCR로
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("이름을 입력해주세요") }
            )

            // ← TextField 위를 덮는 투명 클릭 레이어
            Box(
                Modifier
                    .matchParentSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onRequestOcr() }          // 여기서 이동
            )
        }

        Spacer(Modifier.height(30.dp))
        // 전화번호 입력
        Text(
            text = "전화번호",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it.filter { c -> c.isDigit() } },
            placeholder = { Text("전화번호를 입력해주세요") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(30.dp))
        // 인증번호 입력
        Text(
            text = "인증번호 입력",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter { c -> c.isDigit() } },
            placeholder = { Text("인증번호를 입력해주세요") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        // 인증번호 틀림 메시지
        if (isCodeWrong) {
            Text(
                text = "인증번호가 틀렸습니다.",
                color = Color.Red,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }
        Spacer(Modifier.height(32.dp))
        // 인증번호 받기 버튼
        Button(
            onClick = { viewModel.sendVerificationCode(phone.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text(
                text = "인증번호 받기",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(12.dp))
        // 인증번호 확인하기 버튼
        Button(
            onClick = {
                // ViewModel 함수 호출 시, 받아온 userType을 함께 전달합니다.
                viewModel.verifyCodeAndCreateUser(
                    name = name.trim(),
                    phone = phone.trim(),
                    authCode = code.trim(),
                    userType = userType // 전달받은 userType 사용
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF039BE5))
        ) {
            Text(
                text = "인증번호 확인하기",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(12.dp))
        // 인증번호 다시 받기 버튼
        Button(
            onClick = { viewModel.sendVerificationCode(phone.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF265080))
        ) {
            Text(
                text = "인증번호 다시 받기",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 인증번호 그거 안돼서 우선 임시버튼으로 이렇게 해놨음
        Spacer(Modifier.height(12.dp))

        // --- 2. '슬롯'에 전달된 UI를 실제로 화면에 표시하는 부분 ---
        // MainActivity에서 보호자 링크를 넘겨주면 그 링크가 여기에 표시됩니다.
        bottomContent()
    }


    //ocr 처리
    LaunchedEffect(ocrPrefill) {
        if (ocrPrefill.isNotBlank()) name = ocrPrefill
    }


    // 인증 성공/실패 시 상태 변경
//    LaunchedEffect(verifyResult) {
//        verifyResult?.let { result ->
//            if (result.rsCode == 200) {
//                isCodeWrong = false
//                onNext()
//            } else {
//                isCodeWrong = true
//            }
//        }
//    }
}