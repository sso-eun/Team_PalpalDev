package com.example.dundun_hi.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.dundun_hi.R
import com.example.dundun_hi.data.AlertItem
import com.example.dundun_hi.data.AlertRepository
import com.example.dundun_hi.network.ClovaSpeechClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*


// 25-8-14 UI 일관성 유지 및 Clova Speech 추가
@Composable
fun EditAlertScreen(navController: NavController, alertId: String) {
    val context = LocalContext.current
    val alertRepository = remember { AlertRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val calendar = Calendar.getInstance()

    val existingAlert = remember { alertRepository.getAlertById(alertId) }

    if (existingAlert == null) {
        LaunchedEffect(Unit) { navController.navigateUp() }
        return
    }

    val date = remember { mutableStateOf(existingAlert.date) }
    val time = remember { mutableStateOf(existingAlert.time) }
    val content = remember { mutableStateOf(existingAlert.content) }

    val isRecording = remember { mutableStateOf(false) }
    val mediaRecorderState = remember { mutableStateOf<MediaRecorder?>(null) }
    val currentRecordingPath = remember { mutableStateOf<String?>(null) }
    val isTranscribing = remember { mutableStateOf(false) }
    val finalizedRecordingPath = remember { mutableStateOf<String?>(null) }

    val hasRecordPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordPermission.value = isGranted
        if (!isGranted) {
            Toast.makeText(context, "녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun transcribeAudio(filePath: String) {
        coroutineScope.launch {
            isTranscribing.value = true
            try {
                val file = File(filePath)
                val paramsJson = """{"language":"ko-KR","completion":"sync"}"""
                val paramsRequestBody = paramsJson.toRequestBody("application/json".toMediaTypeOrNull())
                val paramsPart = MultipartBody.Part.createFormData("params", null, paramsRequestBody)
                val fileRequestBody = file.asRequestBody("audio/m4a".toMediaTypeOrNull())
                val mediaPart = MultipartBody.Part.createFormData("media", file.name, fileRequestBody)

                val response = ClovaSpeechClient.apiService.recognizeSpeech(ClovaSpeechClient.INVOKE_URL, ClovaSpeechClient.SECRET_KEY, paramsPart, mediaPart)
                if (response.isSuccessful && response.body() != null) {
                    content.value = response.body()!!.text
                    Toast.makeText(context, "음성 인식이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ClovaAPI", "API 호출 실패: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "음성 인식에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ClovaAPI", "API 호출 중 예외 발생", e)
            } finally {
                isTranscribing.value = false
            }
        }
    }

    fun stopRecording() {
        try {
            mediaRecorderState.value?.stop()
            mediaRecorderState.value?.release()
            mediaRecorderState.value = null
            isRecording.value = false
            currentRecordingPath.value?.let { path ->
                finalizedRecordingPath.value = path
                transcribeAudio(path)
            }
        } catch (e: Exception) {
            Log.e("Recording", "녹음 정지 실패", e)
        }
    }

    fun startRecording() {
        finalizedRecordingPath.value = null
        try {
            val recordingsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "DunDunHi_Recordings")
            if (!recordingsDir.exists()) recordingsDir.mkdirs()
            val audioFile = File(recordingsDir, "recording_${System.currentTimeMillis()}.m4a")
            currentRecordingPath.value = audioFile.absolutePath

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorderState.value = recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }
            isRecording.value = true
            Toast.makeText(context, "녹음을 시작합니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("Recording", "녹음 시작 실패", e)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        // --- Header: Add_Alert.kt와 완전히 동일하게 변경 ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("든든하이", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            // 홈 아이콘 클릭 시 뒤로 가도록 설정
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "홈",
                    tint = Color(0xFF4A90E2),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Page Title: Add_Alert.kt와 완전히 동일하게 변경 ---
        Surface(
            color = Color(0xFFE6F4FB),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "일정 수정",
                modifier = Modifier.padding(12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("날짜", fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().clickable {
            val dateParts = date.value.split("/").map { it.toInt() }
            DatePickerDialog(context, { _, y, m, d -> date.value = String.format("%04d/%02d/%02d", y, m + 1, d) }, dateParts[0], dateParts[1] - 1, dateParts[2]).show()
        }) {
            OutlinedTextField(
                value = date.value, onValueChange = {}, readOnly = true,
                leadingIcon = { Icon(painterResource(R.drawable.ic_calendar), null) },
                placeholder = { Text("날짜 선택하기...", fontSize = 22.sp) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("시간", fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().clickable {
            val timeParts = time.value.split(":").map { it.toInt() }
            TimePickerDialog(context, { _, h, m -> time.value = String.format("%02d:%02d", h, m) }, timeParts[0], timeParts[1], true).show()
        }) {
            OutlinedTextField(
                value = time.value, onValueChange = {}, readOnly = true,
                leadingIcon = { Icon(painterResource(R.drawable.ic_clock), null) },
                placeholder = { Text("시간 선택하기...", fontSize = 22.sp) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // --- Label: '제목작성'으로 통일 ---
        Text("제목작성", fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.Start) {
            Button(
                onClick = { if (!hasRecordPermission.value) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) else if (!isRecording.value) startRecording() },
                enabled = !isRecording.value && !isTranscribing.value,
                colors = ButtonDefaults.buttonColors(containerColor = if (isRecording.value || isTranscribing.value) Color.Gray else Color(0xFFFF6B6B))
            ) { Text("녹음 시작", color = Color.White, fontSize = 16.sp) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { stopRecording() }, enabled = isRecording.value,
                colors = ButtonDefaults.buttonColors(containerColor = if (isRecording.value) Color(0xFF4CAF50) else Color.Gray)
            ) { Text("녹음 완료", color = Color.White, fontSize = 16.sp) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (isRecording.value || isTranscribing.value) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4FB)), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isTranscribing.value) CircularProgressIndicator(Modifier.size(20.dp))
                    else Icon(painterResource(R.drawable.ic_mic_wave), "녹음 중", tint = Color(0xFF4A90E2), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(if (isTranscribing.value) "글자로 바꾸고 있어요..." else "목소리를 듣고 있는 중에요!", color = Color(0xFF4A90E2), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (finalizedRecordingPath.value != null && !isRecording.value && !isTranscribing.value) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("원하시는 문자가 입력되었나요?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("내용이 다르다면 '녹음 시작' 버튼으로 다시 말해주세요.", fontSize = 15.sp, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = content.value, onValueChange = { content.value = it },
            placeholder = { Text("일정 제목을 작성해주세요...", fontSize = 20.sp) },
            modifier = Modifier.fillMaxWidth().height(160.dp), shape = RoundedCornerShape(12.dp)
        )

        // --- Bottom Spacer: weight(1f) 대신 고정 값으로 변경 ---
        Spacer(modifier = Modifier.height(32.dp))

        // "수정 완료" 버튼
        Button(
            onClick = {
                coroutineScope.launch {
                    val updatedAlert = AlertItem(id = alertId, date = date.value, time = time.value, content = content.value)
                    val success = alertRepository.updateAlert(updatedAlert)

                    if (success) {
                        Toast.makeText(context, "일정이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                        navController.navigateUp()
                    } else {
                        Toast.makeText(context, "일정 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            },

            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("수정 완료", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // "취소" 버튼
        OutlinedButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("취소", fontSize = 18.sp)
        }
    }
}
