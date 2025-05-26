// KioskScreen.kt
package com.example.dundun_hi.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dundun_hi.R
import com.example.dundun_hi.ui.theme.DundunHiTheme
import com.example.dundun_hi.ui.theme.Sky

class KioskScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
        Triple("주민등록증", "https://www.youtube.com/watch?v=0DIjM8D5ru0&list=PLnuhV-jz5vg0nAhfdIS9INkTlNLTgf3jG&index=7", R.drawable.document),
        Triple("가족관계증명서", "https://www.youtube.com/watch?v=VX77TqEyCRc&list=PLnuhV-jz5vg0nAhfdIS9INkTlNLTgf3jG&index=8", R.drawable.document),
        Triple("카페", "https://www.youtube.com/watch?v=qfD7916bLwE&list=PLnuhV-jz5vg0nAhfdIS9INkTlNLTgf3jG&index=3", R.drawable.coffee),
        Triple("패스트푸드", "https://www.youtube.com/watch?v=XjheBjdiCFg&list=PLnuhV-jz5vg0nAhfdIS9INkTlNLTgf3jG", R.drawable.burger),
        Triple("대중교통(기차)", "https://www.youtube.com/watch?v=1OiqlmnYs-A&list=PLnuhV-jz5vg0nAhfdIS9INkTlNLTgf3jG&index=4", R.drawable.train),
        Triple("대중교통(버스)", "https://www.youtube.com/watch?v=5dnI3QcDGgU&list=PLnuhV-jz5vg0nAhfdIS9INkTlNLTgf3jG&index=6", R.drawable.bus)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
        items.forEach { (label, url, iconRes) ->
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
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = label,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
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
