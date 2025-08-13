// app/src/main/java/com/example/dundun_hi/ui/guardianProfile/GuardianUpdateProfileScreen.kt
package com.example.dundun_hi.ui.guardianProfile

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dundun_hi.R

private const val TAG = "GuardianUpdateScreen"

/**
 * 보호자 전용 프로필 수정 화면
 * - 이름: 표시만
 * - 전화번호: 수정 가능
 * - 프로필 사진: 갤러리/카메라/기본 이미지
 * - 위치/외출: 제거
 */
@Composable
fun GuardianUpdateProfileScreen(
    viewModel: GuardianProfileViewModel,
    userId: String,
    userNum: Int, // 현재 미사용이지만 시그니처 유지
    onUpdateSuccess: () -> Unit
) {
    val context = LocalContext.current

    // ViewModel 상태 관찰 (Single source of truth)
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }
    val vmTel by remember { derivedStateOf { viewModel.guardianTel } }
    val vmProfile by remember { derivedStateOf { viewModel.guardianProfileImg } }
    val profileUri: Uri? = vmProfile?.takeIf { it.isNotBlank() }?.let(Uri::parse)

    var telEditable by remember(vmTel) { mutableStateOf(vmTel) }
    var showImageDialog by remember { mutableStateOf(false) }

    // 카메라 촬영용 임시 URI (런처보다 위에서 remember)
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // 갤러리/카메라 런처
    val pickFromGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        Log.d(TAG, "Gallery picked uri=$uri")
        if (uri != null) {
            viewModel.onProfileImageSelected(uri.toString())
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        Log.d(TAG, "Camera takePicture success=$success, uri=$cameraImageUri")
        if (success && cameraImageUri != null) {
            viewModel.onProfileImageSelected(cameraImageUri.toString())
        }
    }

    fun createCameraUri(): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )
        Log.d(TAG, "Created camera content uri=$uri")
        return uri
    }

    // 이미지 선택 다이얼로그
    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text("프로필 사진 선택") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("사진을 선택하세요", fontSize = 14.sp)
                    Text("• 앨범에서 가져오기\n• 카메라로 촬영하기\n• 기본 이미지 사용", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        Log.d(TAG, "Click: 앨범에서 가져오기")
                        pickFromGalleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                        showImageDialog = false
                    }) { Text("앨범에서 가져오기") }

                    TextButton(onClick = {
                        Log.d(TAG, "Click: 카메라로 촬영하기")
                        cameraImageUri = createCameraUri()
                        cameraImageUri?.let { takePictureLauncher.launch(it) }
                        showImageDialog = false
                    }) { Text("카메라로 촬영하기") }

                    TextButton(onClick = {
                        Log.d(TAG, "Click: 기본 이미지 사용")
                        viewModel.onProfileImageSelected("") // 기본 이미지
                        showImageDialog = false
                    }) { Text("기본 이미지 사용") }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    Log.d(TAG, "Dialog dismissed")
                    showImageDialog = false
                }) { Text("취소") }
            }
        )
    }

    // 화면 레이아웃
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAF6FB))
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 상단 타이틀
                Text("든든하이", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("보호자 프로필 수정", fontSize = 18.sp, color = Color.Gray)

                Spacer(Modifier.height(24.dp))

                // 오류 메시지
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = "오류: $errorMessage",
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // 카드: 프로필 (이미지 + 이름 표시 + 전화번호 편집)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 프로필 이미지
                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileUri != null) {
                                AsyncImage(
                                    model = profileUri,
                                    contentDescription = "프로필 사진",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                // 기본 썸네일
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF0F0F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_camera),
                                        contentDescription = "기본 이미지",
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                            // 우하단 플로팅 버튼(+) - 이미지 선택
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable {
                                        Log.d(TAG, "Open image dialog")
                                        showImageDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_plus),
                                    contentDescription = "이미지 변경",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // 이름: 텍스트 표시만
                        Text(
                            text = "이름: $userId",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(16.dp))

                        // 전화번호 입력
                        Text(
                            text = "전화번호",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = telEditable,
                            onValueChange = { telEditable = it },
                            placeholder = { Text("전화번호를 입력하세요") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 가이드 텍스트
                Text(
                    text = "사진과 전화번호만 수정 가능합니다.",
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E)
                )

                Spacer(Modifier.height(96.dp)) // 아래 버튼 공간 확보용
            }

            // 하단 고정 저장 버튼
            Button(
                onClick = {
                    Log.d(
                        TAG,
                        "Save clicked: tel=$telEditable, rawImg=${viewModel.guardianProfileImg}"
                    )
                    viewModel.updateProfile(
                        newTel = telEditable,
                        newProfileImg = viewModel.guardianProfileImg.orEmpty(),
                        context = context
                    ) {
                        Toast.makeText(context, "프로필 수정 완료", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Save success; navigate back")
                        onUpdateSuccess()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
            ) {
                Text("수정 완료", fontSize = 18.sp, color = Color.White)
            }

            // 로딩 오버레이
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x66000000)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}
