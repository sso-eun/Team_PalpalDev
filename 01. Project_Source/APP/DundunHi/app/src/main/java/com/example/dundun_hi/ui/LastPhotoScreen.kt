package com.example.dundun_hi.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dundun_hi.model.SharedPhoto
import com.example.dundun_hi.ui.theme.LightGray
import com.example.dundun_hi.ui.theme.Sky

@Composable
fun LastPhotoScreen(
    photos: List<SharedPhoto>,
    onAddPhoto: () -> Unit = {}
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp)   // ★ PaddingValues import 필요
    ) {
        items(photos) { photo ->
            ChatBubble(
                surfaceColor = if (photo.fromMe) Sky else LightGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = photo.resId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

/* ───── ChatBubble: 파일 안에서만 쓰이므로 private ───── */
@Composable
private fun ChatBubble(
    surfaceColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp,
        modifier = modifier
    ) {
        content()
    }
}