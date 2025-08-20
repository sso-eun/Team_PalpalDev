// HomeScreen.kt

package com.example.dundun_hi.ui

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    onAutoSignedIn: () -> Unit,
    // onGuardianClick 파라미터 제거
    // 모든 사용자 (가디언 및 시니어) 통합하여 로그인 안내
) {

    val context = LocalContext.current
    val activity = context as? Activity

    BackHandler(enabled = true) {
        activity?.finishAffinity()
        // 또는 activity?.finishAndRemoveTask() // API 21+, 최근 작업목록에서도 제거
    }


    val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
    val num = prefs.getString("user_num", null)
    val id  = prefs.getString("user_id",  null)

    val ok = !num.isNullOrBlank() && !id.isNullOrBlank() && id?.lowercase() != "null"
    if (ok) onAutoSignedIn()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

        // 중앙 정렬로 레이아웃 개선
        verticalArrangement = Arrangement.Center
    ) {
        Text("든든하이", fontSize = 65.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("저희 앱과", fontSize = 32.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text("구면이세요?", fontSize = 50.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSignupClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
        ) {
            Text("초면이세요?", fontSize = 50.sp, fontWeight = FontWeight.SemiBold)
        }
        // 하단의 '보호자용 페이지' 관련 UI 전체 제거



    }

}