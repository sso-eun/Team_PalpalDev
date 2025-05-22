package com.example.dundun_hi

import android.content.Intent
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dundun_hi.ui.theme.DundunHiTheme

@Composable
fun CameraScreen(navController: NavController) {
    CameraScreenContent(
        onLastPhotoClick = { navController.navigate("lastphoto") }
    )
}

@Composable
private fun CameraScreenContent(
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

        // 상단 타이틀
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "든든하이",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_home), // 홈 아이콘 리소스 필요
                contentDescription = "Home",
                modifier = Modifier.size(24.dp)
            )
        }

        // 카드 1: 바로 사진찍기
        SurfaceCardWithIcon(
            label = "바로 사진찍기",
            iconResId = R.drawable.ic_camera,
            onClick = {
                context.startActivity(
                    Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                )
            }
        )

        // 카드 2: 사진 보러가기
        SurfaceCardWithIcon(
            label = "사진 보러가기",
            iconResId = R.drawable.ic_gallery,
            onClick = {
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                intent.type = "image/*"
                context.startActivity(intent)
            }
        )

        // 카드 3: 지난 사진 보기
        SurfaceCardWithIcon(
            label = "지난 사진 보기",
            iconResId = R.drawable.ic_chat,
            onClick = onLastPhotoClick
        )
    }
}

@Composable
private fun SurfaceCardWithIcon(
    label: String,
    iconResId: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFDFF6FF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

class CameraScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DundunHiTheme {
                val nav = rememberNavController()
                CameraScreen(nav)
            }
        }
    }
}
