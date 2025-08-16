package com.example.dundun_hi.ui
// Add_Alert.kt

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Environment
import android.widget.DatePicker
import android.widget.TimePicker
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.dundun_hi.network.ClovaSpeechClient // <-- 추가
import okhttp3.MediaType.Companion.toMediaTypeOrNull // <-- 추가
import okhttp3.MultipartBody // <-- 추가
import okhttp3.RequestBody.Companion.asRequestBody // <-- 추가
import okhttp3.RequestBody.Companion.toRequestBody // <-- 추가
import android.media.MediaMetadataRetriever
import java.io.File
import java.util.*
import android.util.Log

@Composable
fun AddAlarmScreen(navController: NavController) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val alertRepository = remember { AlertRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    // 녹음 관련 상태
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var currentRecordingPath by remember { mutableStateOf<String?>(null) }
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // API 통신 관련 상태 추가
    var isTranscribing by remember { mutableStateOf(false) }

    var finalizedRecordingPath by remember { mutableStateOf<String?>(null) }    // <-- 추가추가: 완료된 녹음 경로
    var recordingDuration by remember { mutableStateOf(0) }                     // <-- 추가추가: 녹음 길이 (초)

    // 권한 요청 launcher - 녹음 권한만 필요
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }

    // --- API 호출 함수 ---
    fun transcribeAudio(filePath: String) {
        coroutineScope.launch {
            isTranscribing = true
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    Toast.makeText(context, "녹음 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    isTranscribing = false
                    return@launch
                }

                // 1. 'params' 파트 생성 (JSON)
                val paramsJson = """
                {
                    "language": "ko-KR",
                    "completion": "sync",
                    "wordAlignment": true,
                    "fullText": true,
                    "noiseFiltering": true
                }
                """.trimIndent()
                val paramsRequestBody = paramsJson.toRequestBody("application/json".toMediaTypeOrNull())
                val paramsPart = MultipartBody.Part.createFormData("params", null, paramsRequestBody)

                // 2. 'media' 파트 생성 (음성 파일)
                val fileRequestBody = file.asRequestBody("audio/m4a".toMediaTypeOrNull())
                val mediaPart = MultipartBody.Part.createFormData("media", file.name, fileRequestBody)

                // 3. API 호출
                val response = ClovaSpeechClient.apiService.recognizeSpeech(
                    fullUrl = ClovaSpeechClient.INVOKE_URL,
                    secretKey = ClovaSpeechClient.SECRET_KEY,
                    params = paramsPart,
                    media = mediaPart
                )

                if (response.isSuccessful && response.body() != null) {
                    val speechResult = response.body()!!.text
                    contentText = speechResult // <-- API 결과를 텍스트 필드에 반영
                    Toast.makeText(context, "음성 인식이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ClovaAPI", "API 호출 실패: ${response.code()}, $errorBody")
                    Toast.makeText(context, "음성 인식에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("ClovaAPI", "API 호출 중 예외 발생", e)
                Toast.makeText(context, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isTranscribing = false
            }
        }
    }

    // 녹음 시작 함수
    fun startRecording() {

        // 녹음을 새로 시작할 때마다 이전 파일 정보 초기화 // <-- 추가
        finalizedRecordingPath = null
        recordingDuration = 0

        try {
            // 앱 전용 외부 저장소 사용 (권한 불필요)
            val recordingsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "DunDunHi_Recordings")

            // 디렉토리가 없으면 생성
            if (!recordingsDir.exists()) {
                recordingsDir.mkdirs()
            }

            // 파일명: 현재 날짜시간으로 생성
            val fileName = "recording_${System.currentTimeMillis()}.m4a"
            val audioFile = File(recordingsDir, fileName)
            currentRecordingPath = audioFile.absolutePath

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }
//            // 이건 최신 버전용임
//            mediaRecorder = MediaRecorder(context).apply {
//                setAudioSource(MediaRecorder.AudioSource.MIC)
//                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//                setOutputFile(audioFile.absolutePath)
//                prepare()
//                start()
//            }
            isRecording = true
            Toast.makeText(context, "녹음을 시작합니다.\n저장 위치: 앱 전용 폴더", Toast.LENGTH_LONG).show()
            Log.d("Recording", "녹음 시작 - 파일 경로: ${audioFile.absolutePath}")

        } catch (e: Exception) {
            Toast.makeText(context, "녹음 시작 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("Recording", "녹음 시작 실패", e)
        }
    }

    // 녹음 정지 함수 (녹음 종료)
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

//            currentRecordingPath?.let { path ->
//                val file = File(path)
//                if (file.exists()) {
//                    val fileSize = file.length() / 1024 // KB 단위
//                    Toast.makeText(context, "녹음이 완료되었습니다.\n파일 크기: ${fileSize}KB", Toast.LENGTH_LONG).show()
//                    android.util.Log.d("Recording", "녹음 완료 - 파일: $path, 크기: ${fileSize}KB")
//
//                    // 녹음 정지 후 API 호출
//                    transcribeAudio(path)
//                }
//            }

            currentRecordingPath?.let { path ->
                // --- 녹음 파일 정보 읽어오기 (길이 측정) --- // <-- 추가된 로직
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(path)
                    val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val durationMillis = durationStr?.toLongOrNull() ?: 0
                    recordingDuration = (durationMillis / 1000).toInt() // 밀리초를 초 단위로 변환
                } catch (e: Exception) {
                    Log.e("MetaData", "파일 길이 측정 실패", e)
                    recordingDuration = -1 // 에러 표시는 -1
                } finally {
                    retriever.release() // 항상 리소스 해제
                }
                // --- 여기까지 ---

                finalizedRecordingPath = path // <-- 완료된 경로를 상태에 저장

                val file = File(path)
                if (file.exists()) {
                    val fileSize = file.length() / 1024
                    Toast.makeText(context, "녹음 완료. 음성 인식을 시작합니다.", Toast.LENGTH_LONG).show()
                    Log.d("Recording", "녹음 완료 - 파일: $path, 크기: ${fileSize}KB, 길이: ${recordingDuration}초")
                    transcribeAudio(path)
                }
            }

        } catch (e: Exception) {
            Toast.makeText(context, "녹음 정지 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("Recording", "녹음 정지 실패", e)
        }
    }

//    // 가디언인 경우 시니어 번호 찾기
//    LaunchedEffect(userNum, userType) {
//        if (userType == 1) { // 가디언인 경우
//            try {
//                val certResponse = com.example.dundun_hi.network.RetrofitClient.memberService.getCertList(
//                    page = 1,
//                    limit = 10
//                )
//                if (certResponse.isSuccessful && certResponse.body() != null) {
//                    val certList = certResponse.body()!!.results
//                    val seniorCert = certList.find { it.guardian_no == userNum }
//                    if (seniorCert != null) {
//                        seniorUserNum = seniorCert.senior_num
//                    }
//                }
//            } catch (e: Exception) {
//                // 에러 처리
//            }
//        }
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("든든하이", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "홈",
                tint = Color(0xFF4A90E2),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = Color(0xFFE6F4FB),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "일정 추가",
                modifier = Modifier.padding(12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("날짜", fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                DatePickerDialog(
                    context,
                    { _: DatePicker, y, m, d ->
                        selectedDate = String.format("%04d/%02d/%02d", y, m + 1, d)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        ) {

            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null)
                },
                placeholder = { Text("날짜 선택하기...", fontSize = 22.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("시간", fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                TimePickerDialog(
                    context,
                    { _: TimePicker, h, m ->
                        selectedTime = String.format("%02d:%02d", h, m)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }
        ) {
            OutlinedTextField(
                value = selectedTime,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_clock), contentDescription = null)
                },
                placeholder = { Text("시간 선택하기...", fontSize = 22.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 25-08-13 은재 UI/UX 개선
        // 내용작성 섹션에 녹음 기능 추가
        Column {
            // <-- 1. Row를 Column으로 변경하여 세로로 배치
            Text("제목작성", fontWeight = FontWeight.Bold, fontSize = 26.sp)
            Spacer(modifier = Modifier.height(8.dp)) // <-- 제목과 버튼 사이 간격

            // 녹음 버튼들을 담을 새로운 Row, 왼쪽 정렬
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start // <-- 버튼들을 오른쪽으로 정렬
            ) {
                Button(
                    onClick = {
                        if (!hasRecordPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else if (!isRecording) {
                            startRecording()
                        }
                    },
                    enabled = !isRecording && !isTranscribing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording || isTranscribing) Color.Gray else Color(0xFFFF6B6B)
                    )
                ) {
                    Text("녹음 시작", color = Color.White, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 녹음 완료 버튼
                Button(
                    onClick = { stopRecording() },
                    enabled = isRecording,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color(0xFF4CAF50) else Color.Gray
                    )
                ) {
                    Text("녹음 완료", color = Color.White, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 녹음 상태 표시 - 수정 --------------------------------
        if (isRecording || isTranscribing) { // <-- 음성 인식 중일 때도 표시되도록 통합
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4FB)), // <-- 부드러운 파란색 배경
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 음성 인식 중일 때는 로딩 아이콘 표시
                    if (isTranscribing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        // 녹음 중일 때는 귀여운 아이콘 또는 색상 원 표시
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mic_wave), // <-- 마이크 아이콘 (예시)
                            contentDescription = "녹음 중",
                            tint = Color(0xFF4A90E2),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        // 상태에 따라 다른 문구 표시
                        text = if(isTranscribing) "글자로 바꾸고 있어요..." else "목소리를 듣고 있는 중에요!",
                        color = Color(0xFF4A90E2), // <-- 부드러운 파란색 글씨
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 녹음 파일 정보 표시 UI
        if (finalizedRecordingPath != null && !isRecording && !isTranscribing) { // <-- 표시 조건 명확화
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)), // <-- 부드러운 녹색 배경
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "원하시는 문자가 입력되었나요?", // <-- 제안해주신 문구
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF333333)
                    )
                    Text(
                        "내용이 다르다면 '녹음 시작' 버튼으로 다시 말해주세요.", // <-- 수정된 안내 문구
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

//        // 녹음 상태 표시
//        if (isRecording) {
//            Card(
//                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Row(
//                    modifier = Modifier.padding(12.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // 녹음 중 표시 (빨간 원)
//                    Box(
//                        modifier = Modifier
//                            .size(12.dp)
//                            .background(Color.Red, shape = androidx.compose.foundation.shape.CircleShape)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        "녹음 중...",
//                        color = Color.Red,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//        }
//        // 녹음 파일 정보 표시 UI 수정
//        finalizedRecordingPath?.let { path ->
//            Card(
//                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                // 2. Card 안의 내용을 새로운 안내 문구로 교체
//                Column(
//                    modifier = Modifier.padding(16.dp), // 패딩을 좀 더 줘서 보기 좋게
//                    verticalArrangement = Arrangement.spacedBy(4.dp) // 텍스트 사이 간격
//                ) {
//                    Text(
//                        "음성 녹음이 잘 되었어요!", // <-- 시니어 친화적인 멘트
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 16.sp,
//                        color = Color(0xFF333333)
//                    )
//                    Text(
//                        "녹음을 다시 하고 싶으면 '녹음 시작' 버튼을 눌러주세요.", // <-- 안내 메시지
//                        fontSize = 14.sp,
//                        color = Color.Gray
//                    )
//                }
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//        }

        // OutlinedTextField를 추가하여 컴포저블을 올바르게 호출합니다.
        OutlinedTextField(
            value = contentText,
            onValueChange = { contentText = it },
            placeholder = { Text("일정 제목을 작성해주세요...", fontSize = 20.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (selectedDate.isNotBlank() && selectedTime.isNotBlank() && contentText.isNotBlank()) {
                    coroutineScope.launch {
                        val dateTime = "$selectedDate $selectedTime"
                        val isoDateTime = try {
                            val dateParts = selectedDate.split("/")
                            val timeParts = selectedTime.split(":")
                            "${dateParts[0]}-${dateParts[1]}-${dateParts[2]}T${timeParts[0]}:${timeParts[1]}:00"
                        } catch (e: Exception) {
                            "${selectedDate}T${selectedTime}:00"
                        }

                        // userNum 파라미터 없이 단순하게 호출
                        val success = alertRepository.addAlertToServerAndLocal(
                            title = contentText,
                            dateTime = dateTime,
                            dateInfo = isoDateTime
                        )

                        if (success) {
                            Toast.makeText(context, "일정이 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "일정 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !isTranscribing,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB277)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("추가하기", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}