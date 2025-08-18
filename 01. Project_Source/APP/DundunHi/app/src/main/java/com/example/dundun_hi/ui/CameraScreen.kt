// CameraScreen.kt
package com.example.dundun_hi.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dundun_hi.R
import com.example.dundun_hi.ui.theme.DundunHiTheme

/* ───────── 실제 NavController 버전 ───────── */
@Composable
fun CameraScreen(
    userId: Int,                  // ★ NavArgument로 받은 값
    navController: NavController
) {
    CameraScreenContent(
        navController = navController,
        onLastPhotoClick = { navController.navigate("lastphoto/$userId") }
    )
}

/* ───────── 순수 UI만 담은 Content ───────── */
@Composable
private fun CameraScreenContent(
    navController: NavController? = null,
    onLastPhotoClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        /* 타이틀 */
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                // SharedPreferences에서 저장된 사용자 정보를 가져와서 main 화면으로 이동
                val sharedPreferences = context.getSharedPreferences("user_prefs", 0)
                val userNum = sharedPreferences.getString("user_num", "0") ?: "0"
                val userId = sharedPreferences.getString("user_id", "") ?: ""

                if (userId.isNotEmpty() && userNum != "0") {
                    navController?.navigate("main/$userNum/${Uri.encode(userId)}") {
                        // 현재 camera 화면을 스택에서 제거하지 않고 유지
                        launchSingleTop = true
                    }
                }
            }
        ) {
            Text(
                text = "든든하이",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "홈으로 이동",
                tint = Color(0xFF000000),
                modifier = Modifier.size(24.dp)
            )
        }

        /* 바로 사진찍기 */
        SurfaceCard(
            label = "바로 사진찍기",
            onClick = {
                context.startActivity(
                    Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                )
            }
        )

        /* 사진 보러가기(갤러리) */
        SurfaceCard(
            label = "사진 보러가기",
            onClick = {
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                intent.type = "image/*"
                context.startActivity(intent)
            }
        )

        /* 지난 사진 보기(앱 내부) */
        SurfaceCard(
            label = "지난 이야기",
            onClick = onLastPhotoClick
        )
    }
}

/* ───────── 공용 카드 컴포저블 ───────── */
@Composable
private fun SurfaceCard(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFDFF6FF)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    DundunHiTheme {
        CameraScreenContent(
            onLastPhotoClick = { }
        )
    }
}