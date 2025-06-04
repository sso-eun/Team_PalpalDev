// app/src/main/java/com/example/dundun_hi/ui/profile/UpdateProfileScreen.kt

package com.example.dundun_hi.ui.profile

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
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
import coil.compose.AsyncImage
import com.example.dundun_hi.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * UpdateProfileScreen: 프로필 수정 화면
 *
 * @param viewModel       ProfileViewModel을 주입받아 userNum:Int로 서버 데이터를 조회
 * @param userId          로그인된 사용자의 ID(또는 이름) → 화면 상단에 표시
 * @param onUpdateSuccess 프로필이 성공적으로 수정되었을 때 호출, 보통 navController.popBackStack() 으로 이전 화면으로 돌아가는 콜백
 */
@Composable
fun UpdateProfileScreen(
    viewModel: ProfileViewModel,
    userId: String,
    onUpdateSuccess: () -> Unit
) {
    val context = LocalContext.current

    // ── 1) ViewModel에서 내려온 서버 데이터(회원 정보) ─────────────────────────
    val userTel by remember { derivedStateOf { viewModel.userTel } }
    val userProfileImg by remember { derivedStateOf { viewModel.userProfileImg } }
    val userHomeLat by remember { derivedStateOf { viewModel.userHomeLat } }
    val userHomeLon by remember { derivedStateOf { viewModel.userHomeLon } }
    val userCondition by remember { derivedStateOf { viewModel.userCondition } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }

    // ── 2) Compose 내부에서 사용자 입력을 받을 로컬 상태 ───────────────────────
    var telEditable by remember { mutableStateOf(userTel) }
    var profileImgUri by remember { mutableStateOf<Uri?>(null) }
    var setHome by remember { mutableStateOf(false) }
    var hLat by remember { mutableStateOf(userHomeLat) }
    var hLon by remember { mutableStateOf(userHomeLon) }
    var condition by remember { mutableStateOf(userCondition) }

    // “+” 아이콘을 눌렀을 때 나타낼 AlertDialog(갤러리/카메라/기본 이미지)
    var showImageDialog by remember { mutableStateOf(false) }

    // ── 3) 로컬 상태에 ViewModel 값을 복사 (초기화 및 서버 변경 반영) ─────────────
    LaunchedEffect(userTel) {
        telEditable = userTel
    }
    LaunchedEffect(userProfileImg) {
        profileImgUri = if (userProfileImg.isNotEmpty()) Uri.parse(userProfileImg) else null
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

    // ── 5) 카메라 촬영용 Uri 생성 ───────────────────────────────────────────────
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val createCameraUri = {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    // ── 6) 갤러리에서 선택할 때 사용하는 launcher ─────────────────────────────────
    val pickFromGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            profileImgUri = uri
        }
    }

    // ── 7) 카메라 촬영해서 이미지 얻어올 때 사용하는 launcher ────────────────────
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraImageUri != null) {
            profileImgUri = cameraImageUri
        }
    }

    // ── 8) 위치 권한 요청 런처 (집 위치 버튼이 눌렸을 때 사용) ──────────────────────
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // ── 9) AlertDialog(앨범/카메라/기본이미지 선택) ─────────────────────────────────
    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text(text = "프로필 사진 선택") },
            text = {
                Column {
                    Text(
                        text = "사진을 선택하세요",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. 앨범에서 가져오기\n2. 카메라로 촬영하기\n3. 기본 이미지 사용",
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        // 갤러리에서 사진 선택
                        pickFromGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        showImageDialog = false
                    }) {
                        Text("앨범에서 가져오기", fontSize = 16.sp)
                    }
                    TextButton(onClick = {
                        // 카메라로 촬영
                        cameraImageUri = createCameraUri()
                        cameraImageUri?.let {
                            takePictureLauncher.launch(it)
                        }
                        showImageDialog = false
                    }) {
                        Text("카메라로 촬영하기", fontSize = 16.sp)
                    }
                    TextButton(onClick = {
                        // 기본 이미지 사용 → 빈 Uri로 둠(또는 미리 정해둔 리소스 경로)
                        profileImgUri = null
                        showImageDialog = false
                    }) {
                        Text("기본 이미지 사용", fontSize = 16.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // ── 10) 실제 UI 레이아웃 ─────────────────────────────────────────────────
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

            // ── 프로필 카드 (이미지 + 전화번호 수정) ───────────────────────────────────
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
                        if (profileImgUri != null) {
                            AsyncImage(
                                model = profileImgUri,
                                contentDescription = "프로필 사진",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF0F0F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_camera),
                                    contentDescription = "프로필 사진 아이콘",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        // “+” 아이콘 (오른쪽 아래) → 클릭하면 AlertDialog
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 8.dp, y = 8.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    showImageDialog = true
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

                    // (2) 전화번호 입력 필드
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

            // ── 집 위치 설정 카드 ─────────────────────────────────────────────────────────
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
                                // 현재 위치 받아서 hLat, hLon에 저장
                                fusedLocationClient.lastLocation
                                    .addOnSuccessListener { location ->
                                        if (location != null) {
                                            hLat = location.latitude
                                            hLon = location.longitude
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "위치를 가져오지 못했습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
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
                        text = "집 위치를 “예”로 설정하면 수정 완료 시 현재 위도·경도 값을 보냅니다.",
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
                        text = "외출 중으로 상태를 변경하려면 “예”를 선택하세요.",
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
                    // ① ViewModel.updateProfile(...) 호출
                    //    - newTel: telEditable
                    //    - newProfileImg: profileImgUri?.toString() ?: "" (빈 문자열이면 서버에 “” 전송)
                    //    - newHomeLat: hLat
                    //    - newHomeLon: hLon
                    //    - isOuting: condition
                    viewModel.updateProfile(
                        newTel = telEditable,
                        newProfileImg = profileImgUri?.toString() ?: "",
                        newHomeLat = hLat,
                        newHomeLon = hLon,
                        isOuting = condition
                    ) {
                        // 수정 성공 시 호출: 이전 화면으로 돌아가기
                        Toast.makeText(context, "프로필 수정 완료", Toast.LENGTH_SHORT).show()
                        onUpdateSuccess()
                    }
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
    // Preview 용 더미 데이터(뷰모델 없이 화면만 확인용)
}
