package com.example.dundun_hi.ui.signup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FamilyCertificationScreen(
    onConfirm: () -> Unit = {},
    onUpload: () -> Unit = {}
) {
    var elderName by remember { mutableStateOf("") }
    var elderPhone by remember { mutableStateOf("") }
    var uploadedFileName by remember { mutableStateOf("") }
    var isFileUploaded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // 갤러리 접근을 위한 launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // 파일 이름 생성 (현재 날짜 + 시간)
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val currentTime = dateFormat.format(Date())
            val fileName = "가족관계증명서_$currentTime.jpg"
            
            uploadedFileName = fileName
            isFileUploaded = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // 상단 든든하이
        Text(
            text = "든든하이",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        // 회원가입 중앙
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "회원가입",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "어르신 정보," ,
                    fontSize = 24.sp ,
                    fontWeight = FontWeight.Bold
                )

            }
        }
        Spacer(Modifier.height(32.dp))
        // 어르신 이름
        Text(
            text = "어르신 이름",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = elderName,
            onValueChange = { elderName = it },
            placeholder = { Text("이름을 입력해주세요") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(24.dp))
        // 어르신 전화번호
        Text(
            text = "어르신 전화번호",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = elderPhone,
            onValueChange = { elderPhone = it.filter { c -> c.isDigit() } },
            placeholder = { Text("전화번호를 입력해주세요") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(52.dp))

        // 가족관계증명서 업로드 버튼
        Button(
            onClick = { 
                galleryLauncher.launch("image/*")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFF1AB277)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = "가족관계증명서 업로드",
                color = Color(0xFF1AB277),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 업로드 완료 메시지
        if (isFileUploaded) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "업로드 완료: $uploadedFileName",
                color = Color.Red,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(88.dp))

        // 확인 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { onConfirm() },
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
            ) {
                Text(
                    text = "확인",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
