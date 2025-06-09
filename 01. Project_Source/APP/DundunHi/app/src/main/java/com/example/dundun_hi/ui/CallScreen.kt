// CallScreen.kt
package com.example.dundun_hi.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dundun_hi.R

data class CallShortcut(val label: String, val phoneNumber: String)
@Composable
fun CallScreen(
    contacts: List<CallShortcut>,
    onAddShortcut: (Int) -> Unit
) {
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ── 타이틀 (왼쪽 고정)
        Text(
            text = "든든하이",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        // ── 연락처 슬롯 (항상 3개)
        contacts.plus(List(3 - contacts.size) { null })
            .take(3)
            .forEachIndexed { idx, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clickable {
                            item?.let {
                                ctx.startActivity(
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it.phoneNumber}"))
                                )
                            } ?: onAddShortcut(idx)
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F0FA)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item?.label ?: "단축키 등록",
                            fontSize = 50.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Image(
                            painter = painterResource(
                                if (item != null) R.drawable.ic_call
                                else R.drawable.ic_plus
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

        Spacer(modifier = Modifier.height(32.dp))

        // ── 긴급 신고 버튼들 (최대 180×180dp 고정 + 화면 줄어들면 비율에 맞춰 축소)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
        ) {
            EmergencyButton(
                modifier = Modifier
                    .weight(1f)                             // 남는 가로 공간을 동일 비율로 나눔
                    .aspectRatio(1f)                        // 너비 == 높이가 되도록
                    .sizeIn(maxWidth = 180.dp, maxHeight = 180.dp), // 최대 크기 180×180dp
                label = "119신고",
                iconRes = R.drawable.ic_call_119,
                borderColor = Color(0xFFF27A54)
            ) {
                ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:119")))
            }
            EmergencyButton(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .sizeIn(maxWidth = 180.dp, maxHeight = 180.dp),
                label = "112신고",
                iconRes = R.drawable.ic_call_112,
                borderColor = Color(0xFF3B6FE0)
            ) {
                ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")))
            }
        }
    }
}

@Composable
private fun EmergencyButton(
    modifier: Modifier = Modifier,
    label: String,
    iconRes: Int,
    borderColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = label,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}
