package com.example.dundun_hi.ui.util

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import androidx.core.graphics.withTranslation


fun makeOcrReadyBitmap(
    size: IntSize,
    paths: List<List<Offset>>,
    baseStrokePx: Float = 12f,   // 기본보다 굵게
    scale: Float = 2f,           // 2~3 배 추천
    paddingPx: Int = 24          // 가장자리 여백
): Bitmap {
    val w = (size.width * scale).roundToInt() + paddingPx * 2
    val h = (size.height * scale).roundToInt() + paddingPx * 2
    val bmp = Bitmap.createBitmap(w.coerceAtLeast(1), h.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bmp)

    // 배경 흰색
    canvas.drawColor(Color.White.toArgb())

    val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = baseStrokePx
        color = Color.Black.toArgb()
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    // 스케일/패딩 적용해서 그리기
    canvas.withTranslation(paddingPx.toFloat(), paddingPx.toFloat()) {
        scale(scale, scale)

        for (pts in paths) {
            if (pts.size < 2) continue
            val p = AndroidPath().apply {
                moveTo(pts.first().x, pts.first().y)
                for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
            }
            drawPath(p, paint)
        }

    }
    return bmp
}