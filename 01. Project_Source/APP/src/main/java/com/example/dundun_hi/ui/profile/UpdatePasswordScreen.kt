// app/src/main/java/com/example/dundun_hi/ui/profile/UpdatePasswordScreen.kt

package com.example.dundun_hi.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.dundun_hi.data.UserRepository

@Composable
fun UpdatePasswordScreen(
    userNum: String,
    navController: NavHostController,
    repository: UserRepository
) {
    val context = LocalContext.current

    // 로컬 상태
    var currentPw by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // userNum(String)을 Int로 변환하고, Factory를 통해 ViewModel 생성
    val userNumInt = userNum.toIntOrNull() ?: 0
    val factory = ProfileViewModelFactory(repository, userNumInt)
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 현재 비밀번호 입력
            OutlinedTextField(
                value = currentPw,
                onValueChange = { currentPw = it },
                label = { Text("현재 비밀번호") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 새 비밀번호 입력
            OutlinedTextField(
                value = newPw,
                onValueChange = { newPw = it },
                label = { Text("새 비밀번호") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // 오류 메시지 표시
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 변경하기 버튼
            Button(
                onClick = {
                    if (currentPw.isBlank() || newPw.isBlank()) {
                        errorMessage = "모든 항목을 입력해 주세요."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null

                    profileViewModel.updatePassword(
                        userNum = userNum,
                        currentPw = currentPw,
                        newPw = newPw
                    ) { success, message ->
                        isLoading = false
                        if (success) {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            errorMessage = message
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "변경하기", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}
