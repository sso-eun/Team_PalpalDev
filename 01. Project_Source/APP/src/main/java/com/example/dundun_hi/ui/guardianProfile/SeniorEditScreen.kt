
package com.example.dundun_hi.ui.guardianProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dundun_hi.R

/**
 * SeniorEditScreen: 어르신 정보 수정 화면
 */
@Composable
fun SeniorEditScreen(
    viewModel: GuardianProfileViewModel,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    // ViewModel에서 어르신 정보 가져오기
    val seniorId by remember { derivedStateOf { viewModel.seniorId } }
    val seniorTel by remember { derivedStateOf { viewModel.seniorTel } }
    val seniorAddress by remember { derivedStateOf { viewModel.seniorAddress } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }

    // 수정용 상태들
    var editName by remember { mutableStateOf("") }
    var editTel by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    // 초기값 설정
    LaunchedEffect(seniorId, seniorTel, seniorAddress) {
        if (editName.isEmpty()) editName = seniorId
        if (editTel.isEmpty()) editTel = seniorTel
        if (editAddress.isEmpty()) editAddress = seniorAddress
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F0FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── 상단 헤더 ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {


            Text(
                text = "연결된 계정 정보 수정",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(48.dp)) // IconButton과 균형 맞추기
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 수정 폼 카드 ─────────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 이름 입력
//                Text(
//                    text = "이름",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black
//                )
                Text(
                    text = seniorId,
                    fontSize = 30.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

//                Spacer(modifier = Modifier.height(20.dp))
//
//                // 전화번호 입력
//                Text(
//                    text = "전화번호",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(
//                    value = editTel,
//                    onValueChange = {
//                        editTel = it
//                        saveError = null
//                    },
//                    modifier = Modifier.fillMaxWidth(),
//                    placeholder = { Text("전화번호를 입력하세요") },
//                    singleLine = true,
//                    shape = RoundedCornerShape(8.dp)
//                )
//
//                Spacer(modifier = Modifier.height(20.dp))

                // 주소 입력
                Text(
                    text = "집 주소",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editAddress,
                    onValueChange = {
                        editAddress = it
                        saveError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    placeholder = { Text("상세한 주소를 입력하세요\n(예: 서울특별시 강남구 테헤란로 123)") },
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 주소 입력 도움말
                Text(
                    text = "※ 정확한 주소를 입력해야 위치 서비스가 제대로 작동합니다.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 에러 메시지
                if (saveError != null) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = saveError!!,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 저장 버튼
                Button(
                    onClick = {
                        // 입력 검증
                        when {
                            editName.isBlank() -> {
                                saveError = "이름을 입력해주세요"
                            }
                            editTel.isBlank() -> {
                                saveError = "전화번호를 입력해주세요"
                            }
                            editAddress.isBlank() -> {
                                saveError = "주소를 입력해주세요"
                            }
                            else -> {
                                isSaving = true
                                saveError = null

                                viewModel.updateSeniorProfile(
                                    newName = editName,
                                    newTel = editTel,
                                    newAddress = editAddress,
                                    onSuccess = {
                                        isSaving = false
                                        onSaveSuccess()
                                    },
                                    onError = { error ->
                                        isSaving = false
                                        saveError = error
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277)),
                    enabled = !isSaving && !isLoading
                ) {
                    if (isSaving) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("저장 중...", color = Color.White, fontSize = 16.sp)
                        }
                    } else {
                        Text(
                            text = "저장하기",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 취소 버튼
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    //border = ButtonStroke(1.dp, Color(0xFF1AB277)),
                    enabled = !isSaving
                ) {
                    Text(
                        text = "취소",
                        color = Color(0xFF1AB277),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 안내 메시지 카드 ─────────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_call),//icon바꿔ㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓ
//                        contentDescription = null,
//                        tint = Color(0xFF2196F3),
//                        modifier = Modifier.size(20.dp)
//                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "주소 입력 안내",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• 도로명 주소나 지번 주소 모두 가능합니다\n• 건물명이나 아파트 동/호수까지 입력하면 더 정확합니다\n• 주소가 정확하지 않으면 위치 서비스에 오류가 발생할 수 있습니다",
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}