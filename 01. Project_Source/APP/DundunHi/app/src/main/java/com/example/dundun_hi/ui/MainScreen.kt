// MainScreen.kt
package com.example.dundun_hi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dundun_hi.R
import com.example.dundun_hi.ui.theme.BorderGray
import com.example.dundun_hi.ui.theme.ButtonCamBlue
import com.example.dundun_hi.ui.theme.ButtonMapBlue
import com.example.dundun_hi.ui.theme.ButtonMsgTeal
import com.example.dundun_hi.ui.theme.ButtonPhoneGreen
import com.example.dundun_hi.ui.theme.LightGray
import com.example.dundun_hi.ui.theme.Sky

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
    onProfileClick: () -> Unit
) {
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
                // 프로필 이미지
                if (userProfileImg.isNotEmpty()) {
                    AsyncImage(
                        model = userProfileImg,
                        contentDescription = "프로필 사진",
                        modifier = Modifier
                            .size(60.dp)
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
                            .size(60.dp)
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
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text("어서오세요", fontSize = 30.sp, fontWeight = FontWeight.Bold)
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
            )
            if (index == 0) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(
        userName = "길동님",
        userProfileImg = "",
        temperature = 19,
        highTemp = 25,
        lowTemp = 7,
        weatherState = 3, // 구름 많음 상태로 미리보기
        precipitationType = 2, // 비/눈 상태로 미리보기
        onPhonePageClick = {},
        onMessagePageClick = {},
        onCameraPageClick = {},
        onMapPageClick = {},
        onFindCultureCenter = {},
        onKioskPageClick = {},
        onProfileClick = {}
    )
}
