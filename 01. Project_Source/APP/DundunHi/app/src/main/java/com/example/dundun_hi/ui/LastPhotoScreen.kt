// app/src/main/java/com/example/dundun_hi/ui/LastPhotoScreen.kt
package com.example.dundun_hi.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dundun_hi.R
import com.example.dundun_hi.model.SharedPhoto
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LastPhotoScreen(
    senderId: Int,
    receiverId: Int,
    viewerId: Int
) {
    val ctx = LocalContext.current
    val vm: LastPhotoViewModel = viewModel(
        factory = LastPhotoViewModelFactory(
            context = ctx,
            senderId = senderId,
            receiverId = receiverId,
            viewerId = viewerId
        )
    )
    val photos by vm.photos.collectAsState()

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(vm::onPhotoPicked) }

    // (참고) Android 13 (API 33) 이상에서는 READ_EXTERNAL_STORAGE 권한이 필요 없습니다.
    val permState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Android 13 미만일 때만 권한을 확인하고 요청합니다.
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && !permState.status.isGranted) {
                        permState.launchPermissionRequest()
                    } else {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = "사진 추가"
                )
            }
        }
    ) { innerPadding ->
        // (개선) 사진이 없을 때와 있을 때를 구분해서 표시
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "아직 나눈 이야기가 없어요.\n오른쪽 아래 버튼으로 사진을 추가해보세요!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(vertical = 16.dp), // 아이템 위아래에 여백 추가
                verticalArrangement = Arrangement.spacedBy(24.dp) // 아이템 사이 간격 추가
            ) {
                items(photos) { photo ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // (개선) 작성자 이름을 photo 객체에서 가져오도록 준비
                        // ViewModel에서 photo.authorName에 "우리딸" 또는 "엄마" 등을 넣어줘야 합니다.
                        Text(
                            text = photo.authorName, // "우리딸" -> photo.authorName
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // (개선) 로딩 중, 에러 발생 시 보여줄 이미지를 추가
                        AsyncImage(
                            // ViewModel에서 생성해준 완전한 URL을 사용합니다.
                            model = photo.localUri ?: photo.remoteUrl,
                            contentDescription = "지난 이야기 사진",
                            placeholder = painterResource(id = R.drawable.ic_loading), // 로딩 중에 보여줄 이미지
                            error = painterResource(id = R.drawable.ic_error), // 에러 시 보여줄 이미지
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f) // 1:1 비율 유지
                                .clip(RoundedCornerShape(16.dp)), // 좀 더 둥글게
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}