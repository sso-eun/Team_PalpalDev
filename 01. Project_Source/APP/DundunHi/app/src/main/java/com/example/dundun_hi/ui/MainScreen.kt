package com.example.dundun_hi.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.dundun_hi.R
import com.example.dundun_hi.ui.HomeAddressPopup
import com.example.dundun_hi.ui.profile.ProfileViewModel
import com.example.dundun_hi.ui.theme.BorderGray
import com.example.dundun_hi.ui.theme.ButtonCamBlue
import com.example.dundun_hi.ui.theme.ButtonMapBlue
import com.example.dundun_hi.ui.theme.ButtonMsgTeal
import com.example.dundun_hi.ui.theme.ButtonPhoneGreen
import com.example.dundun_hi.ui.theme.LightGray
import com.example.dundun_hi.ui.theme.Sky
import kotlinx.coroutines.delay

// 날씨 상태를 나타내는 enum class
enum class WeatherState(val code: Int, val iconRes: Int) {
    SUNNY(1, R.drawable.ic_weather_sunny),
    CLOUDY(3, R.drawable.ic_weather_cloudy),
    OVERCAST(4, R.drawable.ic_weather_overcast);

    companion object {
        fun fromCode(code: Int) = values().find { it.code == code } ?: SUNNY
    }
}

// 강수 형태를 나타내는 enum class
enum class PrecipitationType(val code: Int, val iconRes: Int) {
    NONE(0, 0),
    RAIN(1, R.drawable.ic_weather_rain),
    SLEET(2, R.drawable.ic_weather_sleet),
    SNOW(3, R.drawable.ic_weather_snow);

    companion object {
        fun fromCode(code: Int) = values().find { it.code == code } ?: NONE
    }
}

@Composable
fun MainScreen(
    userName: String,
    userProfileImg: String = "",   // 사용 안 해도 시그니처 유지
    userNum: Int,                  // 사용 안 해도 시그니처 유지
    temperature: Int,
    highTemp: Int,
    lowTemp: Int,
    weatherState: Int = 1,
    precipitationType: Int = 0,
    onPhonePageClick: () -> Unit,
    onMessagePageClick: () -> Unit,
    onCameraPageClick: () -> Unit,
    onMapPageClick: () -> Unit,
    onFindCultureCenter: () -> Unit,
    onKioskPageClick: () -> Unit,
    onProfileClick: () -> Unit,
    profileViewModel: ProfileViewModel
) {
    val context = LocalContext.current

    // ✅ 메인은 ViewModel이 관리하는 URL(= 캐시버스트 포함)을 그대로 사용
    val profileImageUrl by remember { derivedStateOf { profileViewModel.userProfileImg } }
    val imageVersion by remember { derivedStateOf { profileViewModel.imageVersion } }

    // ── 이미지 강제 새로고침 1회 옵션: URL이 바뀌면 한 번만 캐시 무시
    var forceNoCache by remember { mutableStateOf(false) }
    LaunchedEffect(profileImageUrl) {
        if (profileImageUrl.isNotBlank()) {
            forceNoCache = true
            Log.d("MainScreen", "프로필 이미지 업데이트(강제 새로고침 1회): $profileImageUrl")
        }
    }

    val imageModel = remember(profileImageUrl, imageVersion) {
        if (profileImageUrl.isBlank()) {
            null
        } else {
            ImageRequest.Builder(context)
                .data(profileImageUrl)
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.WRITE_ONLY)
                .build()
        }
    }


    // 오늘 날짜를 기준으로 억제 상태 관리
    val today = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
    val suppressedToday = remember {
        mutableStateOf(profileViewModel.getSuppressedDate() == today)
    }

    // 데이터 로딩 상태 관리
    var isDataLoaded by remember { mutableStateOf(false) }
    var shouldShowPopup by remember { mutableStateOf(false) }

    // ProfileViewModel의 데이터 로딩 상태를 관찰
    LaunchedEffect(profileViewModel) {
        delay(500)
        isDataLoaded = true
        if (!suppressedToday.value && profileViewModel.isHomeLocationEmpty()) {
            shouldShowPopup = true
        }
    }

    // 홈 위치가 설정되면 팝업 숨김
    LaunchedEffect(profileViewModel.isHomeLocationEmpty()) {
        if (isDataLoaded && !profileViewModel.isHomeLocationEmpty()) {
            shouldShowPopup = false
        }
    }

    // 팝업 표시
    if (shouldShowPopup && !suppressedToday.value) {
        HomeAddressPopup(
            context = context,
            userNum = profileViewModel.userNumber,
            userTel = profileViewModel.userTel,
            userProfileImg = profileViewModel.userProfileImg,
            userCondition = profileViewModel.userConditionString,
            viewModel = profileViewModel,
            onDismiss = { shouldShowPopup = false },
            onSuppressToday = {
                profileViewModel.setSuppressedDate(today)
                suppressedToday.value = true
                shouldShowPopup = false
            },
            onHomeSet = { shouldShowPopup = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 타이틀
        Text(
            text = "든든하이",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 인사 + 날씨 카드
        Surface(
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            color = Sky,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ✅ ViewModel이 주는 URL 사용(업데이트 직후 1회 캐시 무시)
                if (imageModel != null) {
                    key(profileImageUrl, forceNoCache) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "프로필 사진",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .scale(1.1f)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                ),
                            onSuccess = {
                                if (forceNoCache) {
                                    Log.d("MainScreen", "프로필 새로고침 성공(캐시 OFF 1회)")
                                    forceNoCache = false
                                }
                            },
                            onError = {
                                if (forceNoCache) {
                                    Log.w("MainScreen", "프로필 새로고침 실패(캐시 OFF 1회) - ${it.result.throwable}")
                                    forceNoCache = false
                                }
                            },
                            onLoading = { /* no-op */ }
                        )
                    }
                } else {
                    // 기본 프로필
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(1.1f)
                            .clip(CircleShape)
                            .background(Color(0xFFCCCCCC))
                            .border(
                                width = 2.dp,
                                color = Color.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_default_profile),
                            contentDescription = "기본 프로필",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("환영해요!", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text(userName, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
                Divider(
                    color = BorderGray,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text("${temperature}°C", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(WeatherState.fromCode(weatherState).iconRes),
                            contentDescription = "날씨 상태",
                            modifier = Modifier.size(36.dp)
                        )
                        if (precipitationType > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(PrecipitationType.fromCode(precipitationType).iconRes),
                                contentDescription = "강수 형태",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Text("최고: ${highTemp}°", fontSize = 25.sp, fontWeight = FontWeight.Light)
                    Text("최저: ${lowTemp}°", fontSize = 25.sp, fontWeight = FontWeight.Light)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 중앙 버튼
        val buttons = listOf(
            Triple("전화", R.drawable.ic_phone, ButtonPhoneGreen) to onPhonePageClick,
            Triple("내 정보", R.drawable.ic_profile, ButtonMsgTeal) to onProfileClick,
            Triple("카메라", R.drawable.ic_camera_main, ButtonCamBlue) to onCameraPageClick,
            Triple("지도", R.drawable.ic_map, ButtonMapBlue) to onMapPageClick
        )

        buttons.chunked(2).forEach { rowButtons ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowButtons.forEach { (buttonData, onClick) ->
                    val (label, iconRes, borderColor) = buttonData
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(4.dp)
                            .border(
                                width = 2.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .clickable(onClick = onClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = label,
                                tint = borderColor,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = label,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 키오스크, 문화센터
        val items = listOf(
            "문화센터 찾기" to onFindCultureCenter,
            "키오스크" to onKioskPageClick
        )
        items.forEachIndexed { index, (label, action) ->
            ListItem(
                colors = ListItemDefaults.colors(containerColor = LightGray),
                headlineContent = {
                    Text(label, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                },
                supportingContent = {
                    Text(
                        text = when (label) {
                            "문화센터 찾기" -> "주변 문화센터 활동을 찾아보세요"
                            else -> "키오스크 사용법을 같이 배워봐요"
                        },
                        fontSize = 23.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightGray, RoundedCornerShape(8.dp))
                    .clickable { action() }
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            )
            if (index == 0) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
