package com.example.dundun_hi.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import com.example.dundun_hi.ui.util.josa
import com.example.dundun_hi.ui.util.makeOcrReadyBitmap
import com.example.dundun_hi.ui.util.recognizeInkKorean
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions


fun recognizeTextFromBitmap(
    bitmap: Bitmap,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
) {
    val image = InputImage.fromBitmap(bitmap, 0)
    val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    recognizer.process(image)
        .addOnSuccessListener { result -> onSuccess(result.text) }
        .addOnFailureListener { e -> onError(e) }
}

@Composable
fun OCRScreen(
    onDone: (String) -> Unit = {}
) {
    val density = LocalDensity.current

    val paths = remember { mutableStateListOf<List<Offset>>() }
    var current by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }

    var recognized by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var inkLoading by remember { mutableStateOf(false) }

    //입력 후 팝업
    var confirmName by remember { mutableStateOf<String?>(null) }
    var showConfirm by remember { mutableStateOf(false) }

    val nm = confirmName.orEmpty()
    val particle = remember(nm) { josa(nm, "이/가") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            modifier = Modifier.fillMaxWidth().height(260.dp)
        ) {
            DrawingBoard(
                strokeWidth = 10f,
                strokeColor = Color.Black,
                paths = paths,
                current = current,
                onChangeCurrent = { current = it },
                onCommitPath = { if (it.isNotEmpty()) paths += it },
                onSizeChanged = { boardSize = it },
                modifier = Modifier.fillMaxSize().background(Color(0xFFE6E6E6)).padding(4.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

//            Button(onClick = {
//                val all = paths + listOf(current).filter { it.isNotEmpty() }
//                preview = makeOcrReadyBitmap(
//                    size = boardSize,
//                    paths = all,
//                    baseStrokePx = 14f,  // 조금 더 굵게
//                    scale = 2.5f,
//                    paddingPx = 32
//                )
//                recognized = null
//            }) { Text("비트맵 변환") }

//            Button(
//                enabled = !loading,
//                onClick = {
//                    val all = paths + listOf(current).filter { it.isNotEmpty() }
//                    val bmpForOcr = preview ?: makeOcrReadyBitmap(
//                        size = boardSize,
//                        paths = all,
//                        baseStrokePx = 14f,
//                        scale = 2.5f,
//                        paddingPx = 32
//                    )
//                    loading = true
//                    recognizeTextFromBitmap(
//                        bitmap = bmpForOcr,
//                        onSuccess = { text ->
//                            recognized = text.ifBlank { "(인식된 텍스트 없음)" }
//                            loading = false
//                        },
//                        onError = { e ->
//                            recognized = "인식 실패: ${e.message}"
//                            loading = false
//                        }
//                    )
//                }
//            ) { Text(if (loading) "인식 중..." else "인식하기") }


            Button(
                enabled = !inkLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 14.dp, horizontal = 20.dp),
                onClick = {
                    inkLoading = true
                    val all = paths + listOf(current).filter { it.isNotEmpty() }
                    recognizeInkKorean(
                        paths = all,
                        onSuccess = { text ->
                            // 원하는 전처리(첫 줄만, 트림 등)
                            val t = text.trim().lines().firstOrNull().orEmpty()

                            recognized = t
                            inkLoading = false


                            if (t.isNotBlank()) {
                                confirmName = t
                                showConfirm = true
                            }
                        },
                        onError = { e ->
                            recognized = "인식 실패: ${e.message}"
                            inkLoading = false
                            showConfirm = false
                        }
                    )
                }

            ) { Text(if (inkLoading) "필기 인식 중..." else "확인",  fontSize = 18.sp) }



            OutlinedButton(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 14.dp, horizontal = 20.dp),
                onClick = {
                paths.clear(); current = emptyList(); preview = null; recognized = null
            }) { Text("지우기",  fontSize = 18.sp) }
        }


        if (showConfirm && !confirmName.isNullOrBlank()) {
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                title = {
                    Text("이름 확인",
                    fontSize = 32.sp,) },

                text = {
                    Text("작성하신 이름이 \"$nm\" $particle 맞나요?",
                    fontSize = 26.sp,
                    lineHeight = 28.sp) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDone(confirmName!!.trim())  // 결과 전달
                            showConfirm = false
                        }
                    ) { Text("그래", fontSize = 24.sp) }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirm = false }  // 그대로 수정/다시 쓰게 닫기
                    ) { Text("다시", fontSize = 24.sp) }
                }
            )
        }

        // 변환 결과 미리보기
        preview?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "preview",
                modifier = Modifier.fillMaxWidth().height(160.dp)
            )
        }

        recognized?.let { text ->
            Text(
                text = "인식 결과:\n$text",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}


@Composable
fun DrawingBoard(
    strokeWidth: Float,
    strokeColor: Color,
    paths: List<List<Offset>>,
    current: List<Offset>,
    onChangeCurrent: (List<Offset>) -> Unit,
    onCommitPath: (List<Offset>) -> Unit,
    onSizeChanged: (IntSize) -> Unit,
    modifier: Modifier = Modifier
) {
    // 최신 콜백/상태를 안전하게 참조
    val cur by rememberUpdatedState(current)
    val changeCur by rememberUpdatedState(onChangeCurrent)
    val commitPath by rememberUpdatedState(onCommitPath)

    Canvas(
        modifier = modifier
            .onSizeChanged { onSizeChanged(it) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { start ->
                        changeCur(listOf(start))
                    },
                    onDrag = { change, _ ->
                        // 드래그 중에는 현재 포인터의 "절대 위치"를 계속 추가
                        changeCur(cur + change.position)
                        // 필요하면: change.consume()
                    },
                    onDragEnd = {
                        if (cur.isNotEmpty()) commitPath(cur)
                        changeCur(emptyList())
                    },
                    onDragCancel = { changeCur(emptyList()) }
                )
            }
    ) {
        // 과거 선들
        for (pts in paths) {
            if (pts.size < 2) continue
            val p = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
            }
            drawPath(p, color = strokeColor, style = Stroke(width = strokeWidth))
        }
        // 현재 선
        if (current.size > 1) {
            val p = Path().apply {
                moveTo(current.first().x, current.first().y)
                for (i in 1 until current.size) lineTo(current[i].x, current[i].y)
            }
            drawPath(p, color = strokeColor, style = Stroke(width = strokeWidth))
        }
    }
}


