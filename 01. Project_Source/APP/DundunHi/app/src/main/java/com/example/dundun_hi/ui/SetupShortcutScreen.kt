//SetupScreen
package com.example.dundun_hi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dundun_hi.model.CallViewModel

/**
 * @param index   설정할 단축키 슬롯 번호 (0,1,2)
 * @param onDone  설정 완료 후 호출되는 콜백
 */
@Composable
fun SetupShortcutScreen(
    index: Int,
    onDone: () -> Unit,
    viewModel: CallViewModel = viewModel()
) {
    var label by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "단축키 등록",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "이름",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = label,
            onValueChange = { 
                label = it
                showError = false
            },
            placeholder = { Text("이름을 입력해주세요") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color(0xFFCCCCCC)
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "전화번호",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { 
                phoneNumber = it.filter { char -> char.isDigit() }
                showError = false
            },
            placeholder = { Text("전화번호를 입력해주세요") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color(0xFFCCCCCC)
            )
        )

        if (showError) {
            Text(
                text = "이름과 전화번호를 모두 입력해주세요",
                color = Color.Red,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (label.isNotBlank() && phoneNumber.isNotBlank()) {
                    viewModel.saveShortcut(index, label, phoneNumber)
                    onDone()
                } else {
                    showError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1AB277)
            )
        ) {
            Text(
                text = "저장하기",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
