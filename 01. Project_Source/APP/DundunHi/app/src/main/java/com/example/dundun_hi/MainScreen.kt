
// MainScreen.kt
package com.example.dundun_hi

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dundun_hi.ui.theme.BorderGray
import com.example.dundun_hi.ui.theme.ButtonCamBlue
import com.example.dundun_hi.ui.theme.ButtonMapBlue
import com.example.dundun_hi.ui.theme.ButtonMsgTeal
import com.example.dundun_hi.ui.theme.ButtonPhoneGreen
import com.example.dundun_hi.ui.theme.LightGray
import com.example.dundun_hi.ui.theme.Sky

@Composable
fun MainScreen(
    userName: String,
    temperature: Int,
    highTemp: Int,
    lowTemp: Int,
    onPhonePageClick: () -> Unit,
    onMessagePageClick: () -> Unit,
    onCameraPageClick: () -> Unit,
    onMapPageClick: () -> Unit,
    onFindCultureCenter: () -> Unit,
    onKioskPageClick: () -> Unit,
    onProfileClick: () -> Unit,
    onGuardianProfileClick: () -> Unit
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${temperature}°C", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("최고: ${highTemp}°", fontSize = 25.sp, fontWeight = FontWeight.Light)
                    Text("최저: ${lowTemp}°", fontSize = 25.sp, fontWeight = FontWeight.Light)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 중앙 버튼 ------------------------------------------------------------------
        val buttonLabels = listOf("전화", "문자", "카메라", "지도")
        buttonLabels.chunked(2).forEach { rowData ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowData.forEach { label ->
                    // 라벨에 따른 테두리 색 결정
                    val borderColor = when(label) {
                        "전화"   -> ButtonPhoneGreen
                        "문자"   -> ButtonMsgTeal
                        "카메라" -> ButtonCamBlue
                        "지도"   -> ButtonMapBlue
                        else     -> MaterialTheme.colorScheme.primary
                    }
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
                            .clickable {
                                when (label) {
                                    "전화"   -> onPhonePageClick()
                                    "문자"   -> onMessagePageClick()
                                    "카메라" -> onCameraPageClick()
                                    "지도"   -> onMapPageClick()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 40.sp, fontWeight = FontWeight.Bold)
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

        Text(text="profile",color=Color.Black, fontSize = 18.sp,
            modifier = Modifier
                .padding(top=4.dp)
                .clickable{onProfileClick()}
        )

        Text(text="Guardian_Profile",color=Color.Black, fontSize = 18.sp,
            modifier = Modifier
                .padding(top=4.dp)
                .clickable{onGuardianProfileClick()}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(
        userName = "길동님",
        temperature = 19,
        highTemp = 25,
        lowTemp = 7,
        onPhonePageClick = {},
        onMessagePageClick = {},
        onCameraPageClick = {},
        onMapPageClick = {},
        onFindCultureCenter = {},
        onKioskPageClick = {},
        onProfileClick ={},
        onGuardianProfileClick =  {}
    )
}
