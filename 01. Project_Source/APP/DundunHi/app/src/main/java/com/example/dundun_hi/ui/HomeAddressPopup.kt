package com.example.dundun_hi.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dundun_hi.ui.profile.ProfileViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun HomeAddressPopup(
    context: Context,
    userNum: Int,
    userTel: String,
    userProfileImg: String,
    userCondition: String,
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit,
    onSuppressToday: () -> Unit,
    onHomeSet : () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var suppressChecked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("지금 자택이신가요?", fontSize = 20.sp) },
        text = {
            Column {
                Text("현재 위치를 집으로 설정할까요?", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = suppressChecked,
                        onCheckedChange = { suppressChecked = it }
                    )
                    Text("오늘 하루 보지 않기")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // ✅ 체크박스 상태에 따라 억제 설정
                if (suppressChecked) onSuppressToday()

                coroutineScope.launch {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            if (location != null) {
                                viewModel.updateProfileWithoutImage(
                                    newTel = userTel,
                                    newHomeLat = location.latitude,
                                    newHomeLon = location.longitude,
                                    isOuting = (userCondition == "1") // 문자열이면 이렇게 변환
                                ) {
                                    Toast.makeText(
                                        context,
                                        "자택이 설정되었습니다",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onHomeSet()
                                }
                            } else {
                                Toast.makeText(context, "위치를 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
                            }
                        }

                }
                onDismiss() // ✅ 마지막에 팝업 닫기
            }) {
                Text("자택으로 설정")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (suppressChecked) onSuppressToday() // ✅ 체크되어 있으면 억제
                onDismiss()
            }) {
                Text("아니요")
            }
        }
    )
}