package com.example.dundun_hi.ui

import android.content.Intent
import android.net.Uri

import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.example.dundun_hi.R // R 클래스를 임포트합니다.
import com.example.dundun_hi.data.RealUserRepository
import com.example.dundun_hi.ui.profile.ProfileViewModel
import com.example.dundun_hi.ui.profile.ProfileViewModelFactory
import com.example.dundun_hi.ui.theme.DundunHiTheme

@Composable
fun CameraScreen(
    userId: Int,
    navController: NavController
) {
    val context = LocalContext.current

    val repository = remember { RealUserRepository() }
    val profileViewModel: ProfileViewModel = viewModel(
        key = "CameraProfileViewModel_$userId",
        factory = ProfileViewModelFactory(repository, userId, context)
    )

    LaunchedEffect(userId) {
        if (profileViewModel.connectedUserId == null) {
            profileViewModel.fetchConnectedUser()
        }
    }

    CameraScreenContent(
        navController = navController, // navController 전달
        userId = userId, // userId 전달
        onLastPhotoClick = {
            val connectedReceiverId = profileViewModel.getConnectedReceiverId()

            if (connectedReceiverId != null) {
                navController.navigate("lastphoto/$userId/$connectedReceiverId")
            } else {
                Toast.makeText(
                    context,
                    "연결된 가족이 없습니다. 가족 인증을 먼저 완료해주세요.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )
}

@Composable
private fun CameraScreenContent(
    navController: NavController? = null,
    userId: Int = 0, // userId 매개변수 추가
    onLastPhotoClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {



//        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                // SharedPreferences에서 저장된 사용자 정보를 가져와서 main 화면으로 이동
                val sharedPreferences = context.getSharedPreferences("user_prefs", 0)
                val userNum = sharedPreferences.getString("user_num", "0") ?: "0"
                val savedUserId = sharedPreferences.getString("user_id", "") ?: ""

                if (savedUserId.isNotEmpty() && userNum != "0") {
                    navController?.navigate("main/$userNum/${Uri.encode(savedUserId)}") {
                        // 현재 camera 화면을 스택에서 제거하지 않고 유지
                        launchSingleTop = true
                    }
                } else {
                    // SharedPreferences에 정보가 없다면 현재 userId를 사용
                    if (userId != 0) {
                        navController?.navigate("main/$userId/${Uri.encode("사용자")}") {
                            launchSingleTop = true
                        }
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


        Text(
            text = "오늘 어떤 순간을 공유할까요?",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold // 변경된 부분: 굵게 처리
        )


        Spacer(modifier = Modifier.height(16.dp))

        /* 바로 사진찍기 */
        SurfaceCard(
            iconResId = R.drawable.ic_camera,
            title = "바로 사진찍기",
            subtitle = "카메라를 열어 순간을 기록해요",
            // 변경된 부분: 이미지와 유사한 선명한 녹색 단색
            colors = listOf(Color(0xFF50D38A), Color(0xFF50D38A)),
            onClick = {
                context.startActivity(
                    Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                )
            }
        )

        /* 사진 보러가기(갤러리) */
        SurfaceCard(
            iconResId = R.drawable.ic_gallery,
            title = "사진 보러가기",
            subtitle = "갤러리에서 사진을 구경해요",
            colors = listOf(Color(0xFF2DB6F4), Color(0xFF2DB6F4)),
            onClick = {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_APP_GALLERY)

                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                }
            }
        )

        /* 지난 사진 보기(앱 내부) */
        SurfaceCard(
            iconResId = R.drawable.ic_history,
            title = "지난 이야기",
            subtitle = "가족과 함께한 추억을 봐요",
            colors = listOf(Color(0xFF569AFF), Color(0xFF9EBAF3)),
            onClick = onLastPhotoClick
        )
    }
}

/* ───────── 공용 카드 컴포저블 (drawable resource 버전) ───────── */
@Composable
private fun SurfaceCard(
    @DrawableRes iconResId: Int,
    title: String,
    subtitle: String,
    colors: List<Color>,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(cardShape)
            .background(Brush.verticalGradient(colors))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 25.sp,
                color = Color.White.copy(alpha = 0.8f)
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