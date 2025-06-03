// app/src/main/java/com/example/dundun_hi/ui/profile/UpdateProfileScreen.kt

package com.example.dundun_hi.ui.profile

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dundun_hi.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * UpdateProfileScreen: 프로필 수정 화면
 *
 * @param viewModel ProfileViewModel을 주입받아 userNum:Int로 서버 데이터를 조회
 * @param userId    로그인된 사용자의 ID(String) → 화면 상단에 표시
 */
@Composable
fun UpdateProfileScreen(
    viewModel: ProfileViewModel,
    userId: String
) {
    val context = LocalContext.current

    // ── 1) ViewModel에서 내려온 서버 데이터(회원 정보) ─────────────────────────
    val userIdFromServer by remember { derivedStateOf { viewModel.userId } }
    val userTel by remember { derivedStateOf { viewModel.userTel } }
    val userProfileImg by remember { derivedStateOf { viewModel.userProfileImg } }
    val userHomeLat by remember { derivedStateOf { viewModel.userHomeLat } }
    val userHomeLon by remember { derivedStateOf { viewModel.userHomeLon } }
    val userCondition by remember { derivedStateOf { viewModel.userCondition } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }

    // ── 2) Compose 내부에서 사용자 입력을 받을 로컬 상태 ───────────────────────
    var nameEditable by remember { mutableStateOf("") }
    var telEditable by remember { mutableStateOf("") }
    var profileImgUrl by remember { mutableStateOf("") }
    var setHome by remember { mutableStateOf(false) }
    var hLat by remember { mutableStateOf(0.0) }
    var hLon by remember { mutableStateOf(0.0) }
    var condition by remember { mutableStateOf(false) }

    // ── 3) ViewModel 값이 바뀔 때마다 로컬 상태에 복사(LaunchedEffect) ─────────
    LaunchedEffect(userIdFromServer) {
        nameEditable = userIdFromServer
    }
    LaunchedEffect(userTel) {
        telEditable = userTel
    }
    LaunchedEffect(userProfileImg) {
        profileImgUrl = userProfileImg
    }
    LaunchedEffect(userHomeLat) {
        hLat = userHomeLat
    }
    LaunchedEffect(userHomeLon) {
        hLon = userHomeLon
    }
    LaunchedEffect(userCondition) {
        condition = userCondition
    }

    // ── 4) 위치 권한 요청 및 FusedLocationProviderClient 초기화 ─────────────────
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    )
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // ── UI 레이아웃 ───────────────────────────────────────────────────────────
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAF6FB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── 상단 타이틀 ───────────────────────────────────────────────────────
            Text(
                text = "든든하이",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ── 사용자 로그인 ID (userId) 화면에 표시 ──────────────────────────────────
            Text(
                text = "안녕하세요, ${userId}님",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ── 로딩/오류 표시 ────────────────────────────────────────────────────
            if (isLoading) {
                Text(text = "로딩 중...", color = Color.Gray, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
            } else if (errorMessage != null) {
                Text(text = "오류: $errorMessage", color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── 프로필 카드 (이미지 + 이름 + 전화번호) ────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // (1) 프로필 이미지 영역
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF0F0F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImgUrl.isNotEmpty()) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_camera),
                                    contentDescription = "프로필 사진(로딩 시)",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_camera),
                                    contentDescription = "프로필 사진 아이콘",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(x = 40.dp, y = 40.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    // TODO: 갤러리/카메라로 이미지 선택 후 profileImgUrl 업데이트
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_plus),
                                contentDescription = "프로필 수정 아이콘",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // (2) 이름 입력 필드
                    Text(
                        text = "이름",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = nameEditable,
                        onValueChange = { nameEditable = it },
                        placeholder = { Text("이름을 입력해주세요") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // (3) 전화번호 입력 필드
                    Text(
                        text = "전화번호",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = telEditable,
                        onValueChange = { telEditable = it },
                        placeholder = { Text("전화번호를 입력해주세요") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 집 위치 설정 카드 ──────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "현재 위치를 집으로 설정할까요?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                setHome = true
                                fusedLocationClient.lastLocation
                                    .addOnSuccessListener { location ->
                                        if (location != null) {
                                            hLat = location.latitude
                                            hLon = location.longitude
                                        }
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (setHome) Color(0xFF34C759) else Color(0xFFD3D3D3),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "예",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { setHome = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!setHome) Color(0xFF34C759) else Color(0xFFD3D3D3),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "아니요",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "집 위치를 “예”로 설정하면 수정 완료 시 lat/lon 값을 전달합니다.",
                        fontSize = 14.sp,
                        color = Color(0xFFAAAAAA),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 외출 여부 카드 ─────────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "현재 외출중이신가요?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { condition = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (condition) Color(0xFF34C759) else Color(0xFFD3D3D3),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "예",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { condition = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!condition) Color(0xFF34C759) else Color(0xFFD3D3D3),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "아니요",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "외출중으로 상태를 변경하고 싶으시다면 “예”를 선택하세요.",
                        fontSize = 14.sp,
                        color = Color(0xFFAAAAAA),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── 수정 완료 버튼 ─────────────────────────────────────────────────────────
            Button(
                onClick = {
                    // ViewModel에 수정된 값을 반영
                    viewModel.updateTel(telEditable)
                    viewModel.updateCondition(condition)
                    if (setHome) {
                        viewModel.updateHomeLocation(hLat, hLon)
                    }
                    viewModel.updateProfileImg(profileImgUrl)

                    Toast.makeText(context, "프로필이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
            ) {
                Text(
                    text = "수정 완료",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateProfileScreenPreview() {
    // Preview용 더미 데이터를 넣고 싶다면 여기에 뷰모델 대신 FakeViewModel을 주입하세요.
}
