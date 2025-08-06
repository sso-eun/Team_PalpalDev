package com.example.dundun_hi.ui.signup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.FindIdRequest
import com.example.dundun_hi.data.FindIdResponse
import com.example.dundun_hi.data.MemberResponse
import com.example.dundun_hi.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// FamilyCertViewModel을 사용하도록 변경
@Composable
fun FamilyCertificationScreen(
    onConfirm: () -> Unit = {},
    onUpload: () -> Unit = {},
    onTestConfirm: () -> Unit = {}
) {
    val familyCertViewModel: FamilyCertViewModel = viewModel(
        factory = FamilyCertViewModelFactory(RetrofitClient.memberService)
    )
    val searchState by familyCertViewModel.searchState.collectAsState()

    var elderName by remember { mutableStateOf("") }
    var elderPhone by remember { mutableStateOf("") }
    var uploadedFileName by remember { mutableStateOf("") }
    var isFileUploaded by remember { mutableStateOf(false) }
    var seniorNum by remember { mutableStateOf<Int?>(null) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    // LoginViewModel에서 현재 로그인한 유저의 user_num 가져오기
    val loginViewModel: com.example.dundun_hi.ui.login.LoginViewModel = viewModel()
    val loginState by loginViewModel.loginState

    // 갤러리 접근을 위한 launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri = it
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val currentTime = dateFormat.format(Date())
            val fileName = "가족관계증명서_$currentTime.jpg"
            uploadedFileName = fileName
            isFileUploaded = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // 상단 든든하이
        Text(
            text = "든든하이",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        // 회원가입 중앙
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "회원가입",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "어르신 정보" ,
                    fontSize = 24.sp ,
                    fontWeight = FontWeight.Bold
                )

            }
        }
        Spacer(Modifier.height(32.dp))
        // 어르신 이름
        Text(
            text = "어르신 이름",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = elderName,
            onValueChange = { elderName = it },
            placeholder = { Text("이름을 입력해주세요") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(24.dp))
        // 어르신 전화번호
        Text(
            text = "어르신 전화번호",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = elderPhone,
            onValueChange = { elderPhone = it.filter { c -> c.isDigit() } },
            placeholder = { Text("전화번호를 입력해주세요") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(52.dp))
        // 어르신 정보 확인 버튼
        Button(
            onClick = {
                if (elderName.isNotBlank() && elderPhone.isNotBlank()) {
                    familyCertViewModel.verifySenior(elderName.trim(), elderPhone.trim(),
                        onSuccess = { num -> seniorNum = num },
                        onError = { /* 에러 메시지는 아래 상태에서 처리 */ }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text(
                text = "어르신 정보 확인",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // 검증 결과 메시지 표시
        when (searchState) {
            is SearchState.Loading -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "정보를 확인하고 있습니다...",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            is SearchState.Success -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "시니어 정보가 확인되었습니다.",
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            is SearchState.NotFound, is SearchState.Error -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "등록되지 않은 회원입니다. 다시 입력해주세요.",
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            else -> {}
        }
        Spacer(Modifier.height(16.dp))
        // 가족관계증명서 업로드 버튼
        Button(
            onClick = {
                // 갤러리로 이동
                galleryLauncher.launch("image/*")
            },
            modifier = Modifier.fillMaxWidth().height(56.dp).shadow(4.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFF1AB277)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = "가족관계증명서 업로드",
                color = Color(0xFF1AB277),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // 업로드 완료 메시지
        if (isFileUploaded) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "파일 선택 완료: $uploadedFileName",
                color = Color.Green,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
        // 실제 업로드 버튼 (파일 선택 후에만 표시)
        if (isFileUploaded && selectedUri != null) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    // 업로드 전 유저 번호와 시니어 번호가 모두 있어야 함
                    val userNum = loginState?.userNum?.toIntOrNull()
                    if (userNum != null && seniorNum != null && selectedUri != null) {
                        familyCertViewModel.uploadCertificate(context, selectedUri!!, userNum)
                    } else {
                        // 에러 메시지 표시
                        // Toast.makeText(context, "업로드에 필요한 정보가 부족합니다.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
            ) {
                Text(
                    text = "서버에 업로드",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // 업로드 결과 메시지 표시
        val uploadResult by familyCertViewModel.uploadResult.collectAsState()
        uploadResult?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it.message,
                color = if (it.rsCode == 0) Color.Green else Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(88.dp))
        // 확인 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { onConfirm() },
                modifier = Modifier.width(200.dp).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
            ) {
                Text(
                    text = "확인",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

