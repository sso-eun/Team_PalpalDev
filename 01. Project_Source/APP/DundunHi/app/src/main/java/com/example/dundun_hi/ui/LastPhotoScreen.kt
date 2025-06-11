package com.example.dundun_hi.ui           // ← 패키지는 예시, 맞게 조정

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource       // ★
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dundun_hi.R
import com.example.dundun_hi.model.SharedPhoto
import com.example.dundun_hi.ui.theme.LightGray
import com.example.dundun_hi.ui.theme.Sky
import com.example.dundun_hi.ui.LastPhotoViewModel          // ★ ui 패키지로 수정
import com.example.dundun_hi.ui.LastPhotoViewModelFactory   // ★
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LastPhotoScreen(
    userNum: Int,
    guardianId: Int
) {
    val ctx = LocalContext.current
    val vm: LastPhotoViewModel = viewModel(
        factory = remember { LastPhotoViewModelFactory(ctx, userNum, guardianId) }
    )
    val photos by vm.photos.collectAsState()

    /* ─ 갤러리 런처 ─ */
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(vm::onPhotoPicked) }

    /* ─ 32 이하 기기 권한 ─ */
    val permState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (Build.VERSION.SDK_INT < 33 && !permState.status.isGranted) {
                        permState.launchPermissionRequest()
                    } else {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                }
            ) {
                /* ★ Int 리소스를 Composable 로 감싸 줌 */
                Icon(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = "사진 추가",
                    tint = Color.Unspecified        // 원본 색 유지
                )
            }
        }
    ) { inner ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = inner.calculateTopPadding() + 16.dp,
                bottom = inner.calculateBottomPadding() + 16.dp
            )
        ) {
            items(photos) { photo ->
                ChatBubble(
                    surfaceColor = if (photo.fromMe) Sky else LightGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    when {
                        photo.localUri != null -> AsyncImage(
                            model = photo.localUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        photo.remoteUrl != null -> AsyncImage(
                            model = photo.remoteUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        else -> Image(
                            painter = painterResource(R.drawable.ic_location),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

/* ─ 말풍선 ─ */
@Composable
private fun ChatBubble(
    surfaceColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp,
        modifier = modifier
    ) { content() }
}
