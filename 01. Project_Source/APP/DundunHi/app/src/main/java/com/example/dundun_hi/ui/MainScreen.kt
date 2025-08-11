// MainScreen.kt
package com.example.dundun_hi.ui

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dundun_hi.R
import com.example.dundun_hi.ui.theme.*
import androidx.compose.ui.layout.ContentScale
import android.util.Log
import com.example.dundun_hi.ui.profile.ProfileViewModel
import com.example.dundun_hi.ui.HomeAddressPopup
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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
    userProfileImg: String = "",
    userNum: Int, // userNum 파라미터 추가
    temperature: Int,
    highTemp: Int,
    lowTemp: Int,
    weatherState: Int = 1, // 기본값은 맑음(1)
    precipitationType: Int = 0, // 기본값은 없음(0)
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

    // 프로필 이미지 URL 생성 (user_num 기반)
    val profileImageUrl = remember(userNum) {
        if (userNum > 0) {
            "https://port-0-dundunhi-manmbjl26e1dbc28.sel4.cloudtype.app/down/profile/$userNum"
        } else {
            ""
        }
    }

    // 오늘 날짜를 기준으로 억제 상태 관리
    val today = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
    val suppressedToday = remember {
        mutableStateOf(
            profileViewModel.getSuppressedDate() == today
        )
    }

    // 데이터 로딩 상태 관리
    var isDataLoaded by remember { mutableStateOf(false) }
    var shouldShowPopup by remember { mutableStateOf(false) }

    // ProfileViewModel의 데이터 로딩 상태를 관찰
    LaunchedEffect(profileViewModel) {
        // ProfileViewModel에서 데이터가 로드될 때까지 기다림
        // 이 부분은 ProfileViewModel의 구조에 따라 조정이 필요할 수 있습니다

        // 방법 1: 짧은 딜레이 후 체크 (임시 방편)
        delay(500) // 500ms 후에 체크

        // 방법 2: ProfileViewModel에 isLoading 상태가 있다면
        // while (profileViewModel.isLoading.value) {
        //     delay(100)
        // }

        isDataLoaded = true

        // 데이터가 로드된 후에 팝업 표시 여부 결정
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
            onHomeSet = {
                shouldShowPopup = false
            }
        )
    }

    // 프로필 이미지 로딩 상태 추적
    LaunchedEffect(profileImageUrl) {
        Log.d("MainScreen", "프로필 이미지 업데이트: $profileImageUrl")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 타이틀-----------------------------------------------------------------------
        Text(
            text = "든든하이",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 인사 + 날씨 카드 ---------------------------------------------------------------
        Surface(
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            color = Sky,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 이미지 (userNum 기반 URL 사용)
                if (profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profileImageUrl,
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
                            )
                    )
                } else {
                    // 기본 프로필 이미지 (사진이 없을 경우)
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
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
                        // 강수 형태가 있는 경우에만 아이콘 표시
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

        // 중앙 버튼 ------------------------------------------------------------------
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

        // 키오스크, 문화센터 ------------------------------------------------------------------
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
                        text = when(label) {
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
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            )
            if (index == 0) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}