package com.example.dundun_hi.ui.signup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dundun_hi.network.RetrofitClient
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FamilyCertificationScreen(
    onConfirm: () -> Unit = {},
    onTestConfirm: () -> Unit = {},     // 테스트용 임시 버튼
    viewModel: FamilyCertViewModel = viewModel(factory = FamilyCertViewModelFactory(RetrofitClient.memberService))
) {
    var elderName by remember { mutableStateOf("") }
    var elderPhone by remember { mutableStateOf("") }

    val uploadResult by viewModel.uploadResult.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // TODO: 실제 가디언 user_num으로 대체
            val currentUserNum = 47
            viewModel.uploadCertificate(context, selectedUri, currentUserNum)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // ... (상단 타이틀 UI는 동일)

        Text(text = "어르신 이름", /*...*/)
        OutlinedTextField(value = elderName, onValueChange = { elderName = it }, /*...*/)
        Spacer(Modifier.height(24.dp))
        Text(text = "어르신 전화번호", /*...*/)
        OutlinedTextField(value = elderPhone, onValueChange = { elderPhone = it.filter { c -> c.isDigit() } }, /*...*/)

        Spacer(Modifier.height(16.dp))

        // --- 1. '어르신 정보 확인' 버튼 (디자인에 맞게 수정) ---
        Button(
            onClick = { viewModel.searchSenior(elderPhone.trim()) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            enabled = elderPhone.isNotBlank(), // 전화번호가 입력되어야 활성화
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text("어르신 정보 확인", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        // --- 2. 검색 결과 메시지 UI (버튼 아래로 위치 이동 및 문구 수정) ---
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            when (val state = searchState) {
                is SearchState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                is SearchState.Success -> Text("시니어 정보가 확인되었습니다.\n가족관계증명서를 업로드 해주세요.", color = Color(0xFF1AB277), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                is SearchState.NotFound -> Text("등록되지 않은 회원입니다. 다시 입력해주세요.", color = Color.Red)
                is SearchState.Error -> Text("오류가 발생했습니다: ${state.message}", color = Color.Red)
                is SearchState.Idle -> Spacer(modifier = Modifier.height(36.dp)) // 공간 차지
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- 3. '가족관계증명서 업로드' 버튼 (디자인에 맞게 수정) ---
        Button(
            onClick = { galleryLauncher.launch("image/*") },
            enabled = searchState is SearchState.Success, // 검색 성공 시에만 활성화
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFF1AB277))
        ) {
            Text("가족관계증명서 업로드", color = Color(0xFF1AB277), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        // --- 4. 업로드 결과 메시지 UI (버튼 아래로 위치 이동) ---
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            if (uploadResult?.rsCode == 200) {
                // 디자인 시안의 파일 이름 대신, 서버 응답 메시지를 활용
                Text("업로드 완료: ${uploadResult?.message}", color = Color(0xFF1AB277))
            } else if (uploadResult != null) {
                Text("업로드 실패: ${uploadResult?.message}", color = Color.Red)
            }
        }

        Spacer(Modifier.weight(1f)) // 남은 공간을 모두 차지

        // --- 5. 최종 '확인' 버튼 (업로드 성공 시에만 활성화) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                enabled = uploadResult?.rsCode == 200,
                onClick = { onConfirm() },
                modifier = Modifier.width(200.dp).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
            ) {
                Text("확인", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
        // --- 여기에 임시 테스트 버튼 추가 ---
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onTestConfirm() }, // MainActivity에 정의한 테스트 로직 호출
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color.Blue) // 테스트용 버튼임을 구분하기 위해 파란색 테두리
        ) {
            Text(
                text = "테스트용 로딩페이지로 이동",
                color = Color.Blue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // ------------------------------------
    }
}