// KioskScreen.kt
package com.example.dundun_hi

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dundun_hi.ui.theme.DundunHiTheme
import com.example.dundun_hi.ui.theme.Sky

class KioskScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DundunHiTheme {
                KioskScreen()
            }
        }
    }
}

@Composable
fun KioskScreen() {
    val context = LocalContext.current
    val items = listOf(
        "주민등록증" to "https://www.youtube.com/watch?v=EXAMPLE_ID1",
        "패스트푸드" to "https://www.youtube.com/watch?v=EXAMPLE_ID2",
        "카페" to "https://www.youtube.com/watch?v=EXAMPLE_ID3",
        "대중교통" to "https://www.youtube.com/watch?v=EXAMPLE_ID4"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "든든하이",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        items.forEach { (label, url) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                color = Sky,
                shape = RoundedCornerShape(12.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = label,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KioskScreenPreview() {
    DundunHiTheme {
        KioskScreen()
    }
}
