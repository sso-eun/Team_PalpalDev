package com.example.dundun_hi.ui.signup

import android.net.Uri
import android.widget.Toast
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dundun_hi.network.RetrofitClient

@Composable
fun FamilyCertificationScreen(
    signupViewModel: SignupViewModel,
    familyCertViewModel: FamilyCertViewModel,
    onConfirm: () -> Unit,
    onTestConfirm: () -> Unit
) {
    var elderName by remember { mutableStateOf("") }
    var elderPhone by remember { mutableStateOf("") }

    val uploadResult by familyCertViewModel.uploadResult.collectAsState()
    val searchState by familyCertViewModel.searchState.collectAsState()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val currentUserNum = signupViewModel.createdUserNum
            if (currentUserNum != null) {
                familyCertViewModel.uploadCertificate(context, selectedUri, currentUserNum)
            } else {
                Toast.makeText(context, "사용자 정보가 없어 업로드할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp), // 상하 여백은 24dp로 유지
        horizontalAlignment = Alignment.Start
    ) {
        // 1. 헤더 UI를 이전 디자인 스타일로 변경
        Text("든든하이", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("회원가입", fontSize = 50.sp, fontWeight = FontWeight.ExtraBold)
                Text("어르신과 연결", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(32.dp))

        Text(text = "어르신 이름", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = elderName,
            onValueChange = { elderName = it },
            placeholder = { Text("이름을 입력해주세요") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(text = "어르신 전화번호", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = elderPhone,
            onValueChange = { elderPhone = it.filter { c -> c.isDigit() } },
            placeholder = { Text("전화번호를 입력해주세요") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp)
        )

        // 여백
        Spacer(Modifier.height(40.dp))

        // '어르신 정보 확인' 버튼
        Button(
            onClick = { familyCertViewModel.verifySenior(elderName.trim(), elderPhone.trim()) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text("어르신 정보 확인", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        // 검색 결과 메시지 UI
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            when (val state = searchState) {
                is SearchState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                is SearchState.Success -> Text("시니어 정보가 확인되었습니다.", color = Color(0xFF1AB277))
                is SearchState.NotFound -> Text("등록되지 않은 회원입니다. 다시 입력해주세요.", color = Color.Red)
                is SearchState.Error -> Text("오류: ${state.message}", color = Color.Red)
                else -> { /* 초기 상태 */ }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 2. 버튼 스타일에 그림자(shadow) 효과 추가 (이전 코드 참고)
        Button(
            onClick = { galleryLauncher.launch("image/*") },
            enabled = searchState is SearchState.Success,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFF1AB277))
        ) {
            Text("가족관계증명서 업로드", color = Color(0xFF1AB277), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        // 업로드 결과 메시지 UI
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            if (uploadResult?.rsCode == 200) {
                Text("✔️ 업로드가 완료되었습니다.", color = Color(0xFF1AB277))
            } else if (uploadResult != null) {
                Text("⚠️ 업로드 실패: ${uploadResult?.message}", color = Color.Red)
            }
        }

        Spacer(Modifier.weight(1f))

        // 최종 '확인' 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val isEnabled = uploadResult?.rsCode == 200
            Button(
                enabled = isEnabled,
                onClick = { onConfirm() },
                modifier = Modifier.width(200.dp).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                // 3. 비활성화 상태의 버튼 색상 지정 (이전 코드 참고)
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1AB277),
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text("확인", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 테스트 버튼
        Spacer(Modifier.height(8.dp))
//        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
//            Button(
//                onClick = { onTestConfirm() },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF1AB277),
//                    disabledContainerColor = Color.Gray
//                )
//            ) {
//                Text("임시버튼 (테스트용)", color = Color.White)
//            }
//        }
    }
}