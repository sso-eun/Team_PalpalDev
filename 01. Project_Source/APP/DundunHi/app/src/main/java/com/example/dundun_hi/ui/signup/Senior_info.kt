package com.example.dundun_hi.ui.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SeniorInfoScreen(
    viewModel: SeniorProfileViewModel,
    onConfirm: () -> Unit = {}
) {
    val seniorProfile by viewModel.seniorProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val roadAddress by viewModel.roadAddress.collectAsState()
    val coords by viewModel.coords.collectAsState()

    val context = LocalContext.current

    // 프로필 로드 후 좌표가 있으면 자동으로 도로명 주소 가져오기
    LaunchedEffect(seniorProfile) {
        val lat = seniorProfile?.userHomeLat?.toDoubleOrNull()
        val lon = seniorProfile?.userHomeLot?.toDoubleOrNull()
        if (lat != null && lon != null) {
            viewModel.setAddressFromLatLng(context, lat, lon)
        }
    }

    val elderName = seniorProfile?.userId ?: ""
    val elderPhone = seniorProfile?.userTel ?: ""

    val elderAddress = when {
        !roadAddress.isNullOrBlank() -> roadAddress!!
        coords != null -> String.format("%.6f, %.6f", coords!!.first, coords!!.second)
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("든든하이", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("어르신 정보", fontSize = 50.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(32.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Text("어르신 이름", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = elderName,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray
                )
            )
            Spacer(Modifier.height(24.dp))

            Text("어르신 전화번호", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = elderPhone,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray
                )
            )
            Spacer(Modifier.height(24.dp))

            Text("어르신 주소", fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
            if (elderAddress.isBlank()) {
                OutlinedTextField(
                    value = "주소가 입력되어있지 않아요!\n마이페이지에서 입력해주세요!",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Red,
                        disabledBorderColor = Color(0xFF1AB277)
                    )
                )
            } else {
                OutlinedTextField(
                    value = elderAddress,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.Gray
                    )
                )
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
            ) {
                Text("확인", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
