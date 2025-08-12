package com.example.handwrite // ← 패키지명 맞게 변경

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OCRScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
        verticalArrangement = Arrangement.spacedBy(16.dp)

    ) {
        Text(
            text = "손으로 쓰세유",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E6E6)),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            DrawingBoard(
                strokeWidth = 6f,
                strokeColor = Color.Black,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE6E6E6))
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun DrawingBoard(
    strokeWidth: Float,
    strokeColor: Color,
    modifier: Modifier = Modifier
) {
    val paths = remember { mutableStateListOf<List<Offset>>() }
    var current by remember { mutableStateOf<List<Offset>>(emptyList()) }

    androidx.compose.foundation.Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { start ->
                    current = listOf(start)
                },
                onDrag = { change, drag ->
//                    change.consume()
                    val last = current.lastOrNull() ?: Offset.Zero
                    current = current + (last + drag)
                },
                onDragEnd = {
                    if (current.isNotEmpty()) paths += current
                    current = emptyList()
                },
                onDragCancel = { current = emptyList() }
            )
        }
    ) {
        paths.forEach { points ->
            if (points.size > 1) {
                val p = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                }
                drawPath(p, color = strokeColor, style = Stroke(width = strokeWidth))
            }
        }
        if (current.size > 1) {
            val p = Path().apply {
                moveTo(current.first().x, current.first().y)
                for (i in 1 until current.size) lineTo(current[i].x, current[i].y)
            }
            drawPath(p, color = strokeColor, style = Stroke(width = strokeWidth))
        }
    }
}
