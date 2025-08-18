// app/src/main/java/com/example/dundun_hi/ui/PhotoDetailScreen.kt
package com.example.dundun_hi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dundun_hi.R
import com.example.dundun_hi.model.SharedPhoto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    targetPhotoId: String = "", // 찾아갈 사진의 ID
    sortOrder: String = "NEWEST", // 피드에서 전달받은 정렬 상태
    filter: String = "ALL", // 피드에서 전달받은 필터 상태
    senderId: Int,
    receiverId: Int,
    viewerId: Int,
    onBackClick: () -> Unit
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
    val receiverName by vm.receiverName.collectAsState()

    // 피드와 동일한 필터링 및 정렬 적용
    val filteredAndSortedPhotos = remember(photos, filter, sortOrder) {
        // 1단계: 필터링
        val filtered = when (filter) {
            "MY_PHOTOS" -> photos.filter { it.fromMe }
            "GUARDIAN_PHOTOS" -> photos.filter { !it.fromMe }
            else -> photos // "ALL"
        }

        // 2단계: 정렬
        when (sortOrder) {
            "OLDEST" -> filtered.sortedBy { it.sendAt }
            else -> filtered.sortedByDescending { it.sendAt } // 기본값: 최신순
        }
    }

    // 선택한 사진의 정확한 위치 찾기
    val targetPhotoIndex = remember(filteredAndSortedPhotos, targetPhotoId) {
        if (targetPhotoId.isBlank()) return@remember 0

        filteredAndSortedPhotos.indexOfFirst { photo ->
            val photoId = photo.remoteUrl ?: photo.localUri?.toString() ?: photo.id
            photoId == targetPhotoId
        }.takeIf { it >= 0 } ?: 0
    }

    val listState = rememberLazyListState()

    // 타겟 사진 위치로 스크롤
    LaunchedEffect(targetPhotoIndex, filteredAndSortedPhotos.size) {
        if (filteredAndSortedPhotos.isNotEmpty() && targetPhotoIndex >= 0) {
            listState.animateScrollToItem(targetPhotoIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "공유 사진",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(filteredAndSortedPhotos) { photo ->
                PhotoDetailItem(
                    photo = photo,
                    authorName = when {
                        photo.fromMe -> "나"
                        else -> receiverName ?: "상대방"
                    },
                    isTargetPhoto = run {
                        val photoId = photo.remoteUrl ?: photo.localUri?.toString() ?: photo.id
                        photoId == targetPhotoId
                    }
                )
            }
        }
    }
}

@Composable
private fun PhotoDetailItem(
    photo: SharedPhoto,
    authorName: String,
    isTargetPhoto: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 1.dp)
            .then(
                // 선택한 사진에 약간의 강조 효과 (옵션)
                if (isTargetPhoto) {
                    Modifier.background(Color.Blue.copy(alpha = 0.05f))
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column {
            // 사용자 정보 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 아바타
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (photo.fromMe) Color(0xFF6200EE) else Color(0xFF03DAC6),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = authorName.take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 이름과 날짜를 가로로 배치
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )

                    // 날짜 표시를 이름 옆에
                    if (photo.sendAt != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${photo.getRelativeTime()}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // 사진
            when {
                photo.localUri != null -> {
                    AsyncImage(
                        model = photo.localUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )
                }
                photo.remoteUrl != null -> {
                    AsyncImage(
                        model = photo.remoteUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(Color.Gray.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_plus),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // 날짜만 표시 (좋아요, 댓글 버튼 제거됨)
            if (photo.sendAt != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = photo.getFormattedDate(),
                        fontSize = 12.sp,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }

            // 구분선
            Divider(
                color = Color.Gray.copy(alpha = 0.1f),
                thickness = 8.dp
            )
        }
    }
}