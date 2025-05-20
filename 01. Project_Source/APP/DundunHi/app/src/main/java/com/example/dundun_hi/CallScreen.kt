//// CallScreen.kt
//package com.example.dundun_hi
//
//import android.content.Intent
//import android.net.Uri
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.dundun_hi.ui.theme.ButtonPhoneGreen
//import com.example.dundun_hi.ui.theme.LightGray
//import com.example.dundun_hi.ui.theme.Sky
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.collectAsState
//import androidx.lifecycle.viewmodel.compose.viewModel
//
//@Composable
//fun CallScreen(
//    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
//) {
//    val shortcuts by viewModel.shortcuts.collectAsState(initial = emptyList())
//    val context = LocalContext.current
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//            .background(Color.White)
//            .padding(16.dp)
//    ) {
//        // 타이틀
//        Text(
//            text = "든든하이",
//            style = MaterialTheme.typography.titleLarge,
//            fontWeight = FontWeight.Bold
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//
//        if (shortcuts.isEmpty()) {
//            // 빈 상태: 가이드 카드 3개
//            repeat(3) {
//                Surface(
//                    tonalElevation = 4.dp,
//                    shape = RoundedCornerShape(12.dp),
//                    color = Sky,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                        .clickable { viewModel.onAddShortcut() }
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(64.dp)
//                            .padding(16.dp),
//                        contentAlignment = Alignment.CenterStart
//                    ) {
//                        Text(
//                            text = "빠른 전화를 위해 단축키를 설정하세요",
//                            fontSize = 20.sp
//                        )
//                    }
//                }
//            }
//        } else {
//            // 저장된 단축키 리스트
//            shortcuts.forEach { item ->
//                Surface(
//                    tonalElevation = 4.dp,
//                    shape = RoundedCornerShape(12.dp),
//                    color = Sky,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                        .clickable {
//                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.phoneNumber}"))
//                            context.startActivity(intent)
//                        }
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(64.dp)
//                            .padding(16.dp),
//                        contentAlignment = Alignment.CenterStart
//                    ) {
//                        Text(
//                            text = item.label,
//                            fontSize = 24.sp,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//            }
//            Spacer(modifier = Modifier.height(24.dp))
//            // 긴급 신고 버튼
//            Row(
//                Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                // 119 신고
//                Box(
//                    modifier = Modifier
//                        .size(120.dp)
//                        .border(2.dp, Color.Red, RoundedCornerShape(12.dp))
//                        .clickable {
//                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:119"))
//                            context.startActivity(intent)
//                        },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "119 신고",
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 18.sp
//                    )
//                }
//                // 112 신고
//                Box(
//                    modifier = Modifier
//                        .size(120.dp)
//                        .border(2.dp, Color.Blue, RoundedCornerShape(12.dp))
//                        .clickable {
//                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
//                            context.startActivity(intent)
//                        },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "112 신고",
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 18.sp
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun CallScreenPreview() {
//    CallScreen()
//}
