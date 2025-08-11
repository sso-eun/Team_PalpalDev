// app/src/main/java/com/example/dundun_hi/ui/profile/UpdateProfileScreen.kt
package com.example.dundun_hi.ui.profile

import android.Manifest
import android.content.ContentValues
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.google.android.gms.location.LocationServices

@Composable
fun UpdateProfileScreen(
    viewModel: ProfileViewModel,
    userId: String,
    onUpdateSuccess: () -> Unit
) {
    val context = LocalContext.current

    // ViewModel 데이터
    val userTel by remember { derivedStateOf { viewModel.userTel } }
    val userProfileImg by remember { derivedStateOf { viewModel.userProfileImg } }
    val userHomeLat by remember { derivedStateOf { viewModel.userHomeLat } }
    val userHomeLon by remember { derivedStateOf { viewModel.userHomeLon } }
    val userCondition by remember { derivedStateOf { viewModel.userCondition } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }

    // 로컬 UI 상태
    var telEditable by remember { mutableStateOf(userTel) }
    var profileImgUri by remember { mutableStateOf<Uri?>(null) } // 미리보기 전용
    var setHome by remember { mutableStateOf(false) }
    var hLat by remember { mutableStateOf(userHomeLat) }
    var hLon by remember { mutableStateOf(userHomeLon) }
    var condition by remember { mutableStateOf(userCondition) }
    var showImageDialog by remember { mutableStateOf(false) }

    // ViewModel 변화 반영
    LaunchedEffect(userTel) { telEditable = userTel }
    LaunchedEffect(userProfileImg) {
        profileImgUri = if (userProfileImg.isNotEmpty()) Uri.parse(userProfileImg) else null
    }
    LaunchedEffect(userHomeLat) { hLat = userHomeLat }
    LaunchedEffect(userHomeLon) { hLon = userHomeLon }
    LaunchedEffect(userCondition) { condition = userCondition }

    // 위치 권한 요청
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
    }
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // 카메라 촬영용 URI
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val createCameraUri = {
        val values = ContentValues().apply { put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") }
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    // 갤러리 선택 런처
    val pickFromGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val prev = profileImgUri
            profileImgUri = uri // 즉시 미리보기
            viewModel.uploadSeniorProfileImage(uri) { ok, err ->
                if (!ok) {
                    profileImgUri = prev // 실패 시 롤백
                    Toast.makeText(context, err ?: "업로드 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 카메라 촬영 런처 (스마트 캐스트 회피)
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val shotUri = cameraImageUri
        if (success && shotUri != null) {
            val prev = profileImgUri
            profileImgUri = shotUri // 즉시 미리보기
            viewModel.uploadSeniorProfileImage(shotUri) { ok, err ->
                if (!ok) {
                    profileImgUri = prev // 실패 시 롤백
                    Toast.makeText(context, err ?: "업로드 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 이미지 선택 다이얼로그
    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text(text = "프로필 사진 선택") },
            text = {
                Column {
                    Text("사진을 선택하세요", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("1. 앨범에서 가져오기\n2. 카메라로 촬영하기\n3. 기본 이미지 사용", fontSize = 14.sp)
                }
            },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        pickFromGalleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                        showImageDialog = false
                    }) { Text("앨범에서 가져오기", fontSize = 16.sp) }

                    TextButton(onClick = {
                        cameraImageUri = createCameraUri()
                        val uri = cameraImageUri
                        if (uri == null) {
                            Toast.makeText(context, "카메라를 시작할 수 없습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            takePictureLauncher.launch(uri)
                        }
                        showImageDialog = false
                    }) { Text("카메라로 촬영하기", fontSize = 16.sp) }

                    TextButton(onClick = {
                        profileImgUri = null
                        viewModel.clearSeniorProfileImage { ok, err ->
                            if (!ok) Toast.makeText(context, err ?: "이미지 초기화 실패", Toast.LENGTH_SHORT).show()
                        }
                        showImageDialog = false
                    }) { Text("기본 이미지 사용", fontSize = 16.sp) }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageDialog = false }) { Text("취소") }
            }
        )
    }

    // 화면 UI
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
            Text("든든하이", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(24.dp))
            Text("안녕하세요, ${userId}님", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Text("로딩 중...", color = Color.Gray, fontSize = 16.sp)
                Spacer(Modifier.height(16.dp))
            } else if (errorMessage != null) {
                Text("오류: $errorMessage", color = Color.Red, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
            }

            // 프로필 카드
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
                    // 이미지 영역
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
                        // “+” 아이콘
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 8.dp, y = 8.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { showImageDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_plus),
                                contentDescription = "프로필 수정 아이콘",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 전화번호
                    Text("전화번호", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Spacer(Modifier.height(4.dp))
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
                    Spacer(Modifier.height(24.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // 집 위치 카드
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
                    Text("현재 위치를 집으로 설정할까요?", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                setHome = true
                                val fused = LocationServices.getFusedLocationProviderClient(context)
                                //빨간줄 원래 이럼--------------------------------
                                fused.lastLocation.addOnSuccessListener { location ->
                                    if (location != null) {
                                        hLat = location.latitude
                                        hLon = location.longitude
                                    } else {
                                        Toast.makeText(context, "위치를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1AB277),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) { Text("예", fontSize = 16.sp, fontWeight = FontWeight.Medium) }

                        Button(
                            onClick = { setHome = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1AB277),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) { Text("아니요", fontSize = 16.sp, fontWeight = FontWeight.Medium) }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "집 위치를 “예”로 설정하면 수정 완료 시 현재 위도·경도 값을 보냅니다.",
                        fontSize = 14.sp,
                        color = Color(0xFFAAAAAA),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 외출 여부 카드
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
                    Text("현재 외출중이신가요?", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { condition = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1AB277),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) { Text("예", fontSize = 16.sp, fontWeight = FontWeight.Medium) }

                        Button(
                            onClick = { condition = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1AB277),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) { Text("아니요", fontSize = 16.sp, fontWeight = FontWeight.Medium) }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("외출 중으로 상태를 변경하려면 “예”를 선택하세요.", fontSize = 14.sp, color = Color(0xFFAAAAAA), lineHeight = 20.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            // 저장 버튼 (이미지 제외하고 나머지만 Partial 업데이트)
            Button(
                onClick = {
                    viewModel.updateProfileWithoutImage( // ViewModel에 구현돼 있어야 함
                        newTel = telEditable,
                        newHomeLat = hLat,
                        newHomeLon = hLon,
                        isOuting = condition
                    ) {
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
                Text("수정 완료", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateProfileScreenPreview() {
    // Preview용
}
