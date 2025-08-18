package com.example.dundun_hi.ui.guardianProfile

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.dundun_hi.R
import com.example.dundun_hi.network.ClovaSpeechClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * SeniorEditScreen: 가디언에서 어르신 정보 수정 화면
 */
@Composable
fun SeniorEditScreen(
    viewModel: GuardianProfileViewModel,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    // ViewModel에서 어르신 정보 가져오기
    val seniorId by remember { derivedStateOf { viewModel.seniorId } }
    val seniorTel by remember { derivedStateOf { viewModel.seniorTel } }
    val seniorAddress by remember { derivedStateOf { viewModel.seniorAddress } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }

    // 수정용 상태들
    var editName by remember { mutableStateOf("") }
    var editTel by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    // 08-17 은재 Speech 추가
    // --- 변수 추가 ---
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- Clova Speech 기능 관련 상태 변수 추가 ---
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

    // 권한 요청 launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordPermission.value = isGranted
        if (!isGranted) {
            Toast.makeText(context, "녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }
    // ------------------------------------

    // 초기값 설정
    LaunchedEffect(seniorId, seniorTel, seniorAddress) {
        if (editName.isEmpty()) editName = seniorId
        if (editTel.isEmpty()) editTel = seniorTel
        if (editAddress.isEmpty()) editAddress = seniorAddress
    }

    // --- Clova Speech 핵심 함수 추가 ---
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
//                    editAddress = response.body()!!.text // <-- 핵심 수정: 음성인식 결과를 주소(editAddress)에 반영
//                    Toast.makeText(context, "음성 인식이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    val newText = response.body()!!.text

                    // --- 이 부분이 핵심 수정 ---
                    // 기존 주소(editAddress)가 비어있지 않으면 뒤에 이어서 붙이고, 비어있으면 새로 채운다.
                    editAddress = if (editAddress.isBlank()) {
                        newText
                    } else {
                        "$editAddress $newText" // 기존 텍스트 + 한 칸 띄우고 + 새 텍스트
                    }
                    // --------------------------

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
    // -----------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F0FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── 상단 헤더 ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {


            Text(
                text = "연결된 계정 정보 수정",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(48.dp)) // IconButton과 균형 맞추기
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 수정 폼 카드 ─────────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "${seniorId}님",
                    fontSize = 30.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                // 이름 입력
//                Text(
//                    text = "이름",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black
//                )
//                Text(
//                    text = seniorId,
//                    fontSize = 30.sp,
//                    color = Color.Black,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp)
//                )

//                Spacer(modifier = Modifier.height(20.dp))
//
//                // 전화번호 입력
//                Text(
//                    text = "전화번호",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(
//                    value = editTel,
//                    onValueChange = {
//                        editTel = it
//                        saveError = null
//                    },
//                    modifier = Modifier.fillMaxWidth(),
//                    placeholder = { Text("전화번호를 입력하세요") },
//                    singleLine = true,
//                    shape = RoundedCornerShape(8.dp)
//                )
//
//                Spacer(modifier = Modifier.height(20.dp))

                // 주소 입력
                Text(
                    text = "집 주소",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))

                // --- 녹음 버튼 UI 추가 ---
                Row(horizontalArrangement = Arrangement.Start) {
                    Button(
                        onClick = {
                            if (!hasRecordPermission.value)
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            else if (!isRecording.value)
                                startRecording()
                                  },
                        enabled = !isRecording.value && !isTranscribing.value,
                        colors = ButtonDefaults.buttonColors(containerColor =
                            if (isRecording.value || isTranscribing.value)
                                Color.Gray else Color(0xFFFF6B6B))
                    ) {
                        Text("녹음 시작",
                            color = Color.White,
                            fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { stopRecording() },
                        enabled = isRecording.value,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording.value) Color(0xFF4CAF50) else Color.Gray)
                    ) {
                        Text(
                            "녹음 완료",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // --- 녹음 상태 표시 UI 추가 ---
                if (isRecording.value || isTranscribing.value) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4FB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            if (isTranscribing.value)
                                CircularProgressIndicator(Modifier.size(20.dp))
                            else
                                Icon(painterResource(R.drawable.ic_mic_wave),
                                    "녹음 중",
                                    tint = Color(0xFF4A90E2),
                                    modifier = Modifier.size(20.dp))

                            Spacer(Modifier.width(12.dp))

                            Text(
                                if (isTranscribing.value)
                                    "글자로 바꾸고 있어요..."
                                else
                                    "목소리를 듣고 있는 중에요!",
                                color = Color(0xFF4A90E2),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                if (finalizedRecordingPath.value != null && !isRecording.value && !isTranscribing.value) {
                    Card(
                        colors = CardDefaults
                            .cardColors(containerColor = Color(0xFFE8F5E8)),
                        modifier = Modifier
                            .fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "원하시는 문자가 입력되었나요?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp)
                            Text(
                                "내용이 다르다면 '녹음 시작' 버튼으로 다시 말해주세요.",
                                fontSize = 15.sp,
                                color = Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                // ---------------------------

                OutlinedTextField(
                    value = editAddress,
                    onValueChange = {
                        editAddress = it
                        saveError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    placeholder = { Text("상세한 주소를 입력하세요\n(예: 서울특별시 강남구 테헤란로 123)") },
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 주소 입력 도움말
                Text(
                    text = "※ 정확한 주소를 입력해야 위치 서비스가 제대로 작동합니다.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 에러 메시지
                if (saveError != null) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = saveError!!,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 저장 버튼
                Button(
                    onClick = {
                        // 입력 검증
                        when {
                            editName.isBlank() -> {
                                saveError = "이름을 입력해주세요"
                            }
                            editTel.isBlank() -> {
                                saveError = "전화번호를 입력해주세요"
                            }
                            editAddress.isBlank() -> {
                                saveError = "주소를 입력해주세요"
                            }
                            else -> {
                                isSaving = true
                                saveError = null

                                viewModel.updateSeniorProfile(
                                    newName = editName,
                                    newTel = editTel,
                                    newAddress = editAddress,
                                    onSuccess = {
                                        isSaving = false
                                        onSaveSuccess()
                                    },
                                    onError = { error ->
                                        isSaving = false
                                        saveError = error
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277)),
                    enabled = !isSaving && !isLoading
                ) {
                    if (isSaving) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("저장 중...", color = Color.White, fontSize = 16.sp)
                        }
                    } else {
                        Text(
                            text = "저장하기",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 취소 버튼
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    //border = ButtonStroke(1.dp, Color(0xFF1AB277)),
                    enabled = !isSaving
                ) {
                    Text(
                        text = "취소",
                        color = Color(0xFF1AB277),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 안내 메시지 카드 ─────────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_call),//icon바꿔ㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓ
//                        contentDescription = null,
//                        tint = Color(0xFF2196F3),
//                        modifier = Modifier.size(20.dp)
//                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "주소 입력 안내",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• 도로명 주소나 지번 주소 모두 가능합니다\n• 건물명이나 아파트 동/호수까지 입력하면 더 정확합니다\n• 주소가 정확하지 않으면 위치 서비스에 오류가 발생할 수 있습니다",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

