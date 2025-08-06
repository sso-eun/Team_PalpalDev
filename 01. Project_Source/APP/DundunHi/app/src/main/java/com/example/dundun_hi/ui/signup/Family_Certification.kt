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

// 어르신 정보 검증을 위한 ViewModel
class ElderVerificationViewModel : ViewModel() {
    private val memberService = RetrofitClient.memberService
    
    private val _verificationState = MutableStateFlow<ElderVerificationState>(ElderVerificationState.Idle)
    val verificationState: StateFlow<ElderVerificationState> = _verificationState
    
    fun verifyElderInfo(elderName: String, elderPhone: String) {
        viewModelScope.launch {
            try {
                _verificationState.value = ElderVerificationState.Loading
                
                // 1. findID API 호출하여 user_num 가져오기
                val findIdResponse = memberService.findId(FindIdRequest(elderPhone))
                
                if (findIdResponse.isSuccessful) {
                    val findIdResult = findIdResponse.body()
                    if (findIdResult != null) {
                        val userNum = findIdResult.userNum.toIntOrNull()
                        if (userNum != null) {
                            // 2. getMember API 호출하여 회원 정보 가져오기
                            val memberResponse = memberService.getMember(userNum)
                            
                            // 3. 입력된 이름과 API에서 가져온 이름 비교
                            // 4. user_num도 비교 (findIdResult.userNum과 memberResponse.userNum)
                            if (memberResponse.userId == elderName && 
                                findIdResult.userNum.toIntOrNull() == memberResponse.userNum) {
                                _verificationState.value = ElderVerificationState.Success
                            } else {
                                _verificationState.value = ElderVerificationState.Error("등록되지 않은 회원입니다. 다시 입력해주세요.")
                            }
                        } else {
                            _verificationState.value = ElderVerificationState.Error("등록되지 않은 회원입니다. 다시 입력해주세요.")
                        }
                    } else {
                        _verificationState.value = ElderVerificationState.Error("등록되지 않은 회원입니다. 다시 입력해주세요.")
                    }
                } else {
                    _verificationState.value = ElderVerificationState.Error("등록되지 않은 회원입니다. 다시 입력해주세요.")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is java.net.SocketTimeoutException -> "서버 응답이 늦습니다. 다시 시도해주세요."
                    is java.net.UnknownHostException -> "인터넷 연결을 확인해주세요."
                    else -> "네트워크 오류가 발생했습니다. 다시 시도해주세요."
                }
                _verificationState.value = ElderVerificationState.Error(errorMessage)
            }
        }
    }
    
    fun resetState() {
        _verificationState.value = ElderVerificationState.Idle
    }
}

sealed class ElderVerificationState {
    object Idle : ElderVerificationState()
    object Loading : ElderVerificationState()
    object Success : ElderVerificationState()
    data class Error(val message: String) : ElderVerificationState()
}

@Composable
fun FamilyCertificationScreen(
    onConfirm: () -> Unit = {},
    onUpload: () -> Unit = {}
) {
    val verificationViewModel: ElderVerificationViewModel = viewModel()
    val verificationState by verificationViewModel.verificationState.collectAsState()
    
    var elderName by remember { mutableStateOf("") }
    var elderPhone by remember { mutableStateOf("") }
    var uploadedFileName by remember { mutableStateOf("") }
    var isFileUploaded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // 갤러리 접근을 위한 launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // 파일 이름 생성 (현재 날짜 + 시간)
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
            modifier = Modifier
                .fillMaxWidth(),
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
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(52.dp))

        // 어르신 정보 확인 버튼
        Button(
            onClick = { 
                if (elderName.isNotBlank() && elderPhone.isNotBlank()) {
                    verificationViewModel.verifyElderInfo(elderName.trim(), elderPhone.trim())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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
        when (verificationState) {
            is ElderVerificationState.Loading -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "정보를 확인하고 있습니다...",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            is ElderVerificationState.Success -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "시니어 정보가 확인되었습니다.",
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            is ElderVerificationState.Error -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = (verificationState as ElderVerificationState.Error).message,
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
                galleryLauncher.launch("image/*")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(28.dp)),
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
                text = "업로드 완료: $uploadedFileName",
                color = Color.Red,
                fontSize = 18.sp,
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
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp),
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
