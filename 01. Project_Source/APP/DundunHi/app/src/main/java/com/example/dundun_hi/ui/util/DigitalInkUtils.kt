package com.example.dundun_hi.ui.util

import androidx.compose.ui.geometry.Offset
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*

/** paths(Offset 리스트들) → ML Kit Ink로 변환 */
fun buildInkFromPaths(paths: List<List<Offset>>): Ink {
    val builder = Ink.builder()
    paths.forEach { pts ->
        if (pts.isEmpty()) return@forEach
        val strokeBuilder = Ink.Stroke.builder()
        pts.forEach { p ->
            strokeBuilder.addPoint(Ink.Point.create(p.x, p.y))
        }
        builder.addStroke(strokeBuilder.build())
    }
    return builder.build()
}

/** 한국어(ko) 모델이 없으면 다운로드 후 실행 */
private fun withKoreanRecognizer(
    onReady: (DigitalInkRecognizer) -> Unit,
    onError: (Throwable) -> Unit
) {
    val id = DigitalInkRecognitionModelIdentifier.fromLanguageTag("ko")
        ?: return onError(IllegalStateException("Korean model id not found"))
    val model = DigitalInkRecognitionModel.builder(id).build()
    val manager = RemoteModelManager.getInstance()

    manager.isModelDownloaded(model)
        .addOnSuccessListener { downloaded ->
            val start = {
                val recognizer = DigitalInkRecognition.getClient(
                    DigitalInkRecognizerOptions.builder(model).build()
                )
                onReady(recognizer)
            }
            if (downloaded) {
                start()
            } else {
                val cond = DownloadConditions.Builder().build()
                manager.download(model, cond)
                    .addOnSuccessListener { start() }
                    .addOnFailureListener { onError(it) }
            }
        }
        .addOnFailureListener { onError(it) }
}

/** 필기 인식 수행 */
fun recognizeInkKorean(
    paths: List<List<Offset>>,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
) {
    val ink = buildInkFromPaths(paths)
    withKoreanRecognizer(
        onReady = { recognizer ->
            recognizer.recognize(ink)
                .addOnSuccessListener { res ->
                    val best = res.candidates.firstOrNull()?.text.orEmpty()
                    onSuccess(best)
                }
                .addOnFailureListener { onError(it) }
        },
        onError = onError
    )
}