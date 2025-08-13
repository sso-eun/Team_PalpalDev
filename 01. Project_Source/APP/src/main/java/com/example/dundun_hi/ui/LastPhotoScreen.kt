package com.example.dundun_hi.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dundun_hi.R
import com.example.dundun_hi.model.SharedPhoto
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

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

    val permState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)

    //빨간줄 에러 아님
    fun formatDate(iso: String?): String {
        return try {
            if (iso.isNullOrBlank()) return ""
            ZonedDateTime.parse(iso)
                .withZoneSameInstant(java.time.ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.KOREA))
        } catch (e: Exception) {
            ""
        }
    }

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
                },
                        containerColor = Color(0xFF1AB277)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = "사진 추가",
                    tint = Color.White // 아이콘은 흰색으로 대비
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(photos) { photo ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    // 🧑 이름 (크기 키움)
                    Text(
                        text = if (photo.senderUserId == viewerId.toString()) "나" else photo.senderName,
                        fontSize = 30.sp, // ← 기존 20sp + 10sp
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // 🕒 날짜 (이름 아래 즉시)
                    Text(
                        text = formatDate(photo.sendAt),
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // 🖼️ 이미지 (가로폭 80%로 줄이고 가운데 정렬)
                    val imageModifier = Modifier
                        .fillMaxWidth(0.8f) // ← 80% 크기
                        .aspectRatio(1f)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp))

                    when {
                        photo.localUri != null -> AsyncImage(
                            model = photo.localUri,
                            contentDescription = null,
                            modifier = imageModifier,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )

                        photo.remoteUrl != null -> AsyncImage(
                            model = photo.remoteUrl,
                            contentDescription = null,
                            modifier = imageModifier,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )

                        else -> Image(
                            painter = painterResource(R.drawable.ic_plus),
                            contentDescription = null,
                            modifier = imageModifier,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
            }}}}