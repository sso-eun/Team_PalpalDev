// 25.08.12 영역에 그린 글씨를 bitmap으로 변환하는 유틸
// author : soeun

package com.example.dundun_hi.ui.util

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Path as AndroidPath
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.createBitmap

/**
 * 사용자가 Canvas에 그린 선(points)을 Bitmap으로 내보냅니다.
 *
 * @param size           캔버스 실제 픽셀 크기(IntSize)
 * @param paths          저장된 모든 선 (각 선은 Offset 리스트)
 * @param bgColor        배경색(Compose Color)
 * @param strokeColor    선 색(Compose Color)
 * @param strokeWidthPx  선 두께(px)
 */

fun drawPathsToBitmap(
    size: IntSize,
    paths: List<List<Offset>>,
    bgColor: Color = Color(0xFFE6E6E6),
    strokeColor: Color = Color.Black,
    strokeWidthPx: Float = 6f
): Bitmap {
    // 안전장치
    val width = size.width.coerceAtLeast(1)
    val height = size.height.coerceAtLeast(1)

    val bitmap = createBitmap(width, height)
    val canvas = AndroidCanvas(bitmap)

    // 배경
    canvas.drawColor(bgColor.toArgb())

    // 페인트
    val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        color = strokeColor.toArgb()   // <-- Compose Color를 Int로 변환
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    // 모든 선 다시 그리기
    for (points in paths) {
        if (points.size < 2) continue
        val p = AndroidPath().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        canvas.drawPath(p, paint)
    }

    return bitmap
}