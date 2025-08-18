package com.example.dundun_hi.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dundun_hi.R
import com.example.dundun_hi.model.SharedPhoto
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

// 필터 타입 정의
enum class PhotoFilter(val displayName: String) {
    ALL("전체"),
    MY_PHOTOS("내 사진"),
    GUARDIAN_PHOTOS("상대방 사진")
}

// 정렬 타입 정의
enum class SortOrder(val displayName: String) {
    NEWEST("최신순"),
    OLDEST("오래된순")
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LastPhotoScreen(
    senderId: Int,
    receiverId: Int,
    viewerId: Int,
    onPhotoClick: (String, SortOrder, PhotoFilter) -> Unit = { _, _, _ -> }
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

    var currentFilter by remember { mutableStateOf(PhotoFilter.ALL) }
    var currentSort by remember { mutableStateOf(SortOrder.NEWEST) }

    // --- ▼▼▼▼▼ 변경된 부분 ▼▼▼▼▼ ---
    val filteredAndSortedPhotos = remember(photos, currentFilter, currentSort) {
        val filtered = when (currentFilter) {
            PhotoFilter.ALL -> photos
            PhotoFilter.MY_PHOTOS -> photos.filter { it.fromMe }
            PhotoFilter.GUARDIAN_PHOTOS -> photos.filter { !it.fromMe }
        }

        // 설명:
        // 정렬 로직을 수정하여 아직 업로드되지 않은 로컬 사진(localUri가 있고 remoteUrl이 없는 사진)을
        // '최신순'에서는 항상 맨 위로, '오래된순'에서는 항상 맨 아래로 보내도록 합니다.
        // 이를 위해 `sortedWith`와 여러 정렬 기준을 조합하는 `compareBy`를 사용합니다.
        when (currentSort) {
            SortOrder.NEWEST -> filtered.sortedWith(
                // 1. 첫 번째 기준: 로컬 사진(아직 업로드 안 된)인지 여부로 내림차순 정렬 (true가 앞으로)
                // 2. 두 번째 기준: 첫 번째 기준이 같을 경우, sendAt 시간으로 내림차순 정렬 (최신이 앞으로)
                compareByDescending<SharedPhoto> { it.localUri != null && it.remoteUrl == null }
                    .thenByDescending { it.sendAt }
            )
            SortOrder.OLDEST -> filtered.sortedWith(
                // 1. 첫 번째 기준: 로컬 사진인지 여부로 오름차순 정렬 (false가 앞으로, 즉 로컬 사진이 뒤로)
                // 2. 두 번째 기준: 첫 번째 기준이 같을 경우, sendAt 시간으로 오름차순 정렬 (오래된 것이 앞으로)
                compareBy<SharedPhoto> { it.localUri != null && it.remoteUrl == null }
                    .thenBy { it.sendAt }
            )
        }
    }
    // --- ▲▲▲▲▲ 변경된 부분 ▲▲▲▲▲ ---

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(vm::onPhotoPicked) }

    val permState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ProfileHeader(
            seniorName = if (senderId == viewerId) "나" else receiverName ?: "시니어",
            guardianName = if (receiverId == viewerId) "나" else receiverName ?: "보호자"
        )

        FilterAndSortControls(
            currentFilter = currentFilter,
            currentSort = currentSort,
            onFilterChange = { currentFilter = it },
            onSortChange = { currentSort = it }
        )

        Box(modifier = Modifier.weight(1f)) {
            if (filteredAndSortedPhotos.isEmpty()) {
                EmptyPhotoState(currentFilter)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredAndSortedPhotos.size) { index ->
                        val photo = filteredAndSortedPhotos[index]
                        PhotoGridItem(
                            photo = photo,
                            onClick = {
                                val photoId = photo.remoteUrl ?: photo.localUri?.toString() ?: photo.id
                                onPhotoClick(photoId, currentSort, currentFilter)
                            }
                        )
                    }
                }
            }

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
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = "사진 추가"
                )
            }
        }
    }
}
@Composable
private fun ProfileHeader(
    seniorName: String,
    guardianName: String
) {
    // 본인과 상대방 색상 정의
    val myColor = Color(0xFF48D1CC) // 청록색 계열
    val opponentColor = Color(0xFF5B9EE1) // 파란색 계열
    val textColor = Color.White

    // '나'일 경우와 상대방일 경우를 구분하여 적용할 색상 결정
    val seniorAvatarBg: Color
    val guardianAvatarBg: Color

    if (seniorName == "나") {
        seniorAvatarBg = myColor
        guardianAvatarBg = opponentColor
    } else {
        seniorAvatarBg = opponentColor
        guardianAvatarBg = myColor
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "우리의 지난 이야기",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${seniorName}님과 ${guardianName}님의 소중한 순간들",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileAvatar(
                name = seniorName,
                backgroundColor = seniorAvatarBg, // 위에서 결정된 색상 적용
                contentColor = textColor
            )

            Spacer(modifier = Modifier.width(24.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_swap_horiz),
                contentDescription = "서로 연결됨",
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(24.dp))

            ProfileAvatar(
                name = guardianName,
                backgroundColor = guardianAvatarBg, // 위에서 결정된 색상 적용
                contentColor = textColor
            )
        }
    }
}
@Composable
private fun ProfileAvatar(
    name: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                // 변경된 부분: 회색 테두리 추가
                .border(1.5.dp, Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(2),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor // 변경된 부분: 전달받은 콘텐츠 색상 사용
            )
        }

        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}


// ------------------- 아래 코드는 변경사항 없습니다 -------------------

@Composable
private fun FilterAndSortControls(
    currentFilter: PhotoFilter,
    currentSort: SortOrder,
    onFilterChange: (PhotoFilter) -> Unit,
    onSortChange: (SortOrder) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PhotoFilter.values().forEach { filter ->
                FilterTab(
                    text = filter.displayName,
                    isSelected = currentFilter == filter,
                    onClick = { onFilterChange(filter) }
                )
            }
        }

        SortDropdown(
            currentSort = currentSort,
            onSortChange = onSortChange
        )

        Divider(
            color = Color.Gray.copy(alpha = 0.2f),
            thickness = 0.5.dp
        )
    }
}

@Composable
private fun FilterTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        fontSize = 16.sp
    )
}

@Composable
private fun SortDropdown(
    currentSort: SortOrder,
    onSortChange: (SortOrder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentSort.displayName,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_keyboard_arrow_down),
                contentDescription = "정렬 옵션",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortOrder.values().forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = sort.displayName,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onSortChange(sort)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PhotoGridItem(
    photo: SharedPhoto,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        when {
            photo.localUri != null -> {
                AsyncImage(
                    model = photo.localUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            photo.remoteUrl != null -> {
                AsyncImage(
                    model = photo.remoteUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_plus),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (photo.fromMe) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
private fun EmptyPhotoState(currentFilter: PhotoFilter) {
    val message = when (currentFilter) {
        PhotoFilter.ALL -> "아직 공유된 사진이 없어요"
        PhotoFilter.MY_PHOTOS -> "내가 보낸 사진이 없어요"
        PhotoFilter.GUARDIAN_PHOTOS -> "상대방이 보낸 사진이 없어요"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_plus),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "첫 번째 추억을 공유해보세요!",
            fontSize = 14.sp,
            color = Color.Gray.copy(alpha = 0.7f)
        )
    }
}