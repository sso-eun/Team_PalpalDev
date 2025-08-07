// app/src/main/java/com/example/dundun_hi/ui/guardianProfile/GuardianProfileScreen.kt

package com.example.dundun_hi.ui.guardianProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dundun_hi.R

/**
 * GuardianProfileScreen: 보호자 프로필 화면 (어르신 정보 포함)
 */
@Composable
fun GuardianProfileScreen(
    viewModel: GuardianProfileViewModel,
    onEditSeniorClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val guardianId by remember { derivedStateOf { viewModel.guardianId } }
    val guardianTel by remember { derivedStateOf { viewModel.guardianTel } }
    val guardianProfileImg by remember { derivedStateOf { viewModel.guardianProfileImg } }
    val seniorId by remember { derivedStateOf { viewModel.seniorId.value } }
    val seniorTel by remember { derivedStateOf { viewModel.seniorTel } }
    val seniorProfileImg by remember { derivedStateOf { viewModel.seniorProfileImg } }
    val seniorAddress by remember { derivedStateOf { viewModel.seniorAddress } }
    val seniorCondition by remember { derivedStateOf { viewModel.seniorCondition } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F0FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("든든하이 보호자", fontSize = 32.sp, fontWeight = FontWeight.Bold)

            TextButton(onClick = onLogoutClick) {
                Text("로그아웃", fontSize = 16.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF1AB277))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("정보를 불러오는 중...", fontSize = 16.sp, color = Color.Gray)
                }
            }
        } else if (errorMessage != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("오류가 발생했습니다", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, fontSize = 14.sp, color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.refresh() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))) {
                        Text("다시 시도", color = Color.White)
                    }
                }
            }
        } else {
            GuardianProfileCard(
                guardianId = guardianId,
                guardianTel = guardianTel,
                guardianProfileImg = guardianProfileImg
            )

            Spacer(modifier = Modifier.height(16.dp))

            SeniorProfileCard(
                seniorId = seniorId,
                seniorTel = seniorTel,
                seniorProfileImg = seniorProfileImg,
                seniorCondition = seniorCondition,
                seniorAddress = seniorAddress,
                onEditSeniorClick = onEditSeniorClick
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Composable
fun SeniorProfileCard(
    seniorId: String,
    seniorTel: String,
    seniorProfileImg: String?,
    seniorCondition: Boolean,
    seniorAddress: String,
    onEditSeniorClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(16.dp)
        ) {
            if (seniorProfileImg.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFCCCCCC))
                        .border(width = 1.dp, color = Color.Gray, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_default_profile),
                        contentDescription = "기본 프로필",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                AsyncImage(
                    model = seniorProfileImg,
                    contentDescription = "어르신 프로필 사진",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(width = 1.dp, color = Color.Gray, shape = CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = seniorId, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = seniorTel, fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = seniorAddress, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (seniorCondition) "외출중" else "집에 있는중",
                fontSize = 14.sp,
                color = if (seniorCondition) Color(0xFF1AB277) else Color.Red
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onEditSeniorClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
            ) {
                Text("어르신 정보 수정", color = Color.White)
            }
        }
    }
}


@Composable
fun GuardianProfileCard(guardianId: String, guardianTel: String, guardianProfileImg: String?) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(16.dp)
        ) {
            if (guardianProfileImg.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFCCCCCC))
                        .border(width = 1.dp, color = Color.Gray, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_default_profile),
                        contentDescription = "기본 프로필",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                AsyncImage(
                    model = guardianProfileImg,
                    contentDescription = "프로필 사진",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(width = 1.dp, color = Color.Gray, shape = CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = guardianId,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = guardianTel,
                fontSize = 20.sp,
                color = Color.Gray
            )
        }
    }
}