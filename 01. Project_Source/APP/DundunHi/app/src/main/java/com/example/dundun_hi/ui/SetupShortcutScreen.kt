package com.example.dundun_hi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * @param index   설정할 단축키 슬롯 번호 (0,1,2)
 * @param onDone  설정 완료 후 호출되는 콜백
 */
@Composable
fun SetupShortcutScreen(
    index: Int,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "단축키 설정 화면 (slot #$index)")

        // TODO: 실제 연락처 목록을 보여주고,
        //       선택하면 onDone() 콜백을 호출하도록 구현하세요.

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("설정 완료")
        }
    }
}
