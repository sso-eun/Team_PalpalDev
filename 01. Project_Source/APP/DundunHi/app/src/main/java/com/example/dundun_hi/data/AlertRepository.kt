package com.example.dundun_hi.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.dundun_hi.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AlertItem(
    val id: String = java.util.UUID.randomUUID().toString(), // 고유 ID 추가
    val date: String,
    val time: String,
    val content: String
)

class AlertRepository private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: AlertRepository? = null

        fun getInstance(context: Context): AlertRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AlertRepository(context.applicationContext).also { instance ->
                    INSTANCE = instance
                    // ✅ 인스턴스 생성 시 자동으로 서버에서 데이터 로드
                    instance.initializeData()
                }
            }
        }
    }

    private val _alertList = mutableStateListOf<AlertItem>()
    val alertList: List<AlertItem> = _alertList
    
    // ✅ 데이터 로딩 상태 추적
    private val _isLoading = mutableStateListOf<Boolean>()
    val isLoading: Boolean get() = _isLoading.isNotEmpty() && _isLoading.last()
    
    // ✅ 데이터 로딩 완료 콜백
    private val _onDataLoadedCallbacks = mutableListOf<() -> Unit>()
    
    // ✅ 데이터 로딩 완료 콜백 등록
    fun onDataLoaded(callback: () -> Unit) {
        _onDataLoadedCallbacks.add(callback)
    }
    
    // ✅ 데이터 로딩 완료 콜백 제거
    fun removeDataLoadedCallback(callback: () -> Unit) {
        _onDataLoadedCallbacks.remove(callback)
    }

    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // ✅ 초기 데이터 로딩
    private fun initializeData() {
        CoroutineScope(Dispatchers.IO).launch {
            val userNum = getUserNum()
            Log.d("AlertRepository", "=== 초기 데이터 로딩 시작 ===")
            Log.d("AlertRepository", "가져온 userNum: $userNum")
            
            if (userNum > 0) {
                Log.d("AlertRepository", "앱 시작 시 자동 데이터 로딩 - userNum: $userNum")
                val success = loadAlertsFromServer(userNum)
                Log.d("AlertRepository", "초기 데이터 로딩 완료 - 성공: $success, 알림 개수: ${_alertList.size}")
                
                // 초기 로딩 후에도 콜백 호출
                withContext(Dispatchers.Main) {
                    Log.d("AlertRepository", "초기 로딩 후 콜백 호출 (${_onDataLoadedCallbacks.size}개)")
                    _onDataLoadedCallbacks.forEach { callback ->
                        try {
                            callback()
                        } catch (e: Exception) {
                            Log.e("AlertRepository", "초기 로딩 콜백 실행 중 오류", e)
                        }
                    }
                }
            } else {
                Log.e("AlertRepository", "❌ 유효하지 않은 userNum으로 데이터 로딩 건너뜀: $userNum")
                Log.e("AlertRepository", "SharedPreferences에 user_num이 저장되지 않았거나 0입니다.")
            }
        }
    }

    // ✅ userNum 가져오기 함수
    private fun getUserNum(): Int {
        val fromPrefs = sharedPreferences.getString("user_num", null)?.toIntOrNull()

        // 디버깅을 위한 로그
        val prefsUserNum = sharedPreferences.getString("user_num", "null")
        val prefsUserType = sharedPreferences.getString("user_type", "null")
        val prefsUserId = sharedPreferences.getString("user_id", "null")

        Log.d("AlertRepository", "=== SharedPreferences 확인 ===")
        Log.d("AlertRepository", "user_num: $prefsUserNum")
        Log.d("AlertRepository", "user_type: $prefsUserType")
        Log.d("AlertRepository", "user_id: $prefsUserId")

        return fromPrefs ?: 0
    }

    // ✅ 공개 메서드로 수동 새로고침 지원
    fun refreshFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            val userNum = getUserNum()
            if (userNum > 0) {
                Log.d("AlertRepository", "수동 새로고침 - userNum: $userNum")
                val success = loadAlertsFromServer(userNum)
                Log.d("AlertRepository", "새로고침 완료 - 성공: $success, 알림 개수: ${_alertList.size}")
            } else {
                Log.w("AlertRepository", "유효하지 않은 userNum으로 새로고침 건너뜀: $userNum")
            }
        }
    }

    // ✅ 강제 데이터 로딩 (콜백 보장)
    fun forceRefreshFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            val userNum = getUserNum()
            Log.d("AlertRepository", "=== 강제 새로고침 시작 ===")
            Log.d("AlertRepository", "userNum: $userNum")
            
            if (userNum > 0) {
                Log.d("AlertRepository", "강제 새로고침 - userNum: $userNum")
                val success = loadAlertsFromServer(userNum)
                Log.d("AlertRepository", "강제 새로고침 완료 - 성공: $success, 알림 개수: ${_alertList.size}")
                
                // 강제 새로고침 후에도 콜백 호출
                withContext(Dispatchers.Main) {
                    Log.d("AlertRepository", "강제 새로고침 후 콜백 호출 (${_onDataLoadedCallbacks.size}개)")
                    _onDataLoadedCallbacks.forEach { callback ->
                        try {
                            callback()
                        } catch (e: Exception) {
                            Log.e("AlertRepository", "강제 새로고침 콜백 실행 중 오류", e)
                        }
                    }
                }
            } else {
                Log.e("AlertRepository", "❌ 유효하지 않은 userNum으로 강제 새로고침 건너뜀: $userNum")
                Log.e("AlertRepository", "SharedPreferences에 user_num이 저장되지 않았거나 0입니다.")
                
                // userNum이 없어도 콜백은 호출하여 UI가 업데이트되도록 함
                withContext(Dispatchers.Main) {
                    Log.d("AlertRepository", "userNum 없음으로 인한 콜백 호출 (${_onDataLoadedCallbacks.size}개)")
                    _onDataLoadedCallbacks.forEach { callback ->
                        try {
                            callback()
                        } catch (e: Exception) {
                            Log.e("AlertRepository", "userNum 없음 콜백 실행 중 오류", e)
                        }
                    }
                }
            }
        }
    }

    fun addAlert(alert: AlertItem) {
        _alertList.add(alert)
    }

    fun removeAlert(alert: AlertItem) {
        _alertList.remove(alert)
    }

    fun deleteAlert(alert: AlertItem) {
        _alertList.remove(alert)
    }

    fun getAlertById(id: String): AlertItem? {
        return _alertList.find { it.id == id }
    }

    fun updateAlert(updatedAlert: AlertItem) {
        val index = _alertList.indexOfFirst { it.id == updatedAlert.id }
        if (index != -1) {
            _alertList[index] = updatedAlert
        }
    }

    fun clearAlerts() {
        _alertList.clear()
    }

    /**
     * 서버에 일정을 저장하고 성공 시 로컬에도 추가하는 함수
     * Map 방식으로 API 호출 (서버 호환성을 위해)
     */
    suspend fun addAlertToServerAndLocal(
        userNum: Int,
        title: String,
        dateTime: String,
        dateInfo: String  // DATETIME 형식 (ISO 8601)
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AlertRepository", "서버 저장 시작 - userNum: $userNum, title: $title, dateTime: $dateTime, dateInfo: $dateInfo")

                // Map 방식으로 데이터 준비 (서버 호환성을 위해)
                val requestData = mapOf(
                    "user_num" to userNum,           // int (필수)
                    "user_date_title" to title,      // String (필수) - 일정 제목
                    "user_date_time" to dateTime,    // String (필수) - 시간 정보 "2025/08/11 17:52"
                    "user_date_info" to dateInfo     // DATETIME (필수) - ISO 8601 형식 "2025-08-11T17:52:00"
                )

                Log.d("AlertRepository", "요청 데이터 (Map): $requestData")

                // 직접 Retrofit 호출 (Map 방식)
                val response = RetrofitClient.memberService.updateProfilePartial(userNum, requestData)

                // 만약 위 방법이 안 되면 SetDateRequest 방식으로 시도
                if (!response.isSuccessful) {
                    Log.w("AlertRepository", "Map 방식 실패, SetDateRequest 방식으로 재시도")

                    val setDateRequest = com.example.dundun_hi.data.SetDateRequest(
                        user_num = userNum,
                        user_date_title = title,
                        user_date_time = dateTime,
                        user_date_info = dateInfo
                    )

                    Log.d("AlertRepository", "요청 데이터 (SetDateRequest): $setDateRequest")
                    val response2 = RetrofitClient.memberService.setDate(setDateRequest)

                    if (response2.isSuccessful) {
                        val responseBody = response2.body()
                        Log.d("AlertRepository", "서버 저장 성공 (SetDateRequest): $responseBody")

                        // 서버 저장 성공 시 로컬에도 추가
                        val alertItem = AlertItem(
                            date = dateTime.split(" ")[0],  // 날짜 부분만 (2025/08/11)
                            time = dateTime.split(" ")[1],  // 시간 부분만 (17:52)
                            content = title
                        )

                        withContext(Dispatchers.Main) {
                            addAlert(alertItem)
                        }

                        // ✅ 서버에서 최신 데이터 다시 로드하여 동기화
                        loadAlertsFromServer(userNum)

                        return@withContext true
                    } else {
                        val errorBody = response2.errorBody()?.string()
                        Log.e("AlertRepository", "서버 저장 실패 (SetDateRequest): ${response2.code()} - ${response2.message()}")
                        Log.e("AlertRepository", "에러 응답: $errorBody")
                        return@withContext false
                    }
                } else {
                    Log.d("AlertRepository", "서버 저장 성공 (Map): ${response.body()}")

                    // 서버 저장 성공 시 로컬에도 추가
                    val alertItem = AlertItem(
                        date = dateTime.split(" ")[0],
                        time = dateTime.split(" ")[1],
                        content = title
                    )

                    withContext(Dispatchers.Main) {
                        addAlert(alertItem)
                    }

                    // ✅ 서버에서 최신 데이터 다시 로드하여 동기화
                    loadAlertsFromServer(userNum)

                    return@withContext true
                }
            } catch (e: Exception) {
                Log.e("AlertRepository", "서버 저장 중 오류: ${e.message}", e)
                false
            }
        }
    }

    /**
     * ✅ 강화된 디버깅이 포함된 서버에서 사용자의 일정 목록을 가져오는 함수
     */
    suspend fun loadAlertsFromServer(userNum: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // ✅ 로딩 상태 시작
                withContext(Dispatchers.Main) {
                    _isLoading.add(true)
                }
                
                Log.d("AlertRepository", "=== 서버에서 일정 목록 로드 시작 ===")
                Log.d("AlertRepository", "요청 userNum: $userNum")

                // ✅ 1차 시도: getDateList(userNum) - 기존 방식
                Log.d("AlertRepository", "1차 시도: getDateList(userNum) 호출")
                val response = RetrofitClient.memberService.getDateList(userNum)

                Log.d("AlertRepository", "응답 코드: ${response.code()}")
                Log.d("AlertRepository", "응답 성공 여부: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val dateListResponse = response.body()
                    Log.d("AlertRepository", "응답 body: $dateListResponse")

                    if (dateListResponse != null) {
                        Log.d("AlertRepository", "results 크기: ${dateListResponse.results?.size}")

                        // 응답 데이터가 있으면 로컬에 추가
                        dateListResponse.results?.let { dateList ->
                            Log.d("AlertRepository", "서버에서 받은 일정 목록:")
                            dateList.forEachIndexed { index, dateItem ->
                                Log.d("AlertRepository", "[$index] user_date_time: ${getFieldValue(dateItem, "user_date_time")}")
                                Log.d("AlertRepository", "[$index] user_date_title: ${getFieldValue(dateItem, "user_date_title")}")
                                Log.d("AlertRepository", "[$index] user_date_no: ${getFieldValue(dateItem, "user_date_no")}")
                                Log.d("AlertRepository", "[$index] user_date_info: ${getFieldValue(dateItem, "user_date_info")}")
                            }

                            withContext(Dispatchers.Main) {
                                // 기존 데이터 초기화 후 새 데이터 추가
                                val previousSize = _alertList.size
                                clearAlerts()
                                Log.d("AlertRepository", "기존 알림 ${previousSize}개 삭제됨")

                                dateList.forEach { dateItem ->
                                    try {
                                        // ✅ 날짜/시간 파싱 개선
                                        val (date, time) = parseDateTimeFromServer(dateItem)

                                        val alertItem = AlertItem(
                                            id = getFieldValue(dateItem, "user_date_no")?.toString() ?: java.util.UUID.randomUUID().toString(),
                                            date = date,
                                            time = time,
                                            content = getFieldValue(dateItem, "user_date_title")?.toString() ?: ""
                                        )
                                        addAlert(alertItem)
                                        Log.d("AlertRepository", "✅ 알림 추가됨: ${alertItem.content} - ${alertItem.date} ${alertItem.time}")
                                    } catch (e: Exception) {
                                        Log.e("AlertRepository", "날짜 파싱 실패", e)
                                        // ✅ 파싱 실패 시에도 기본값으로 알림 추가
                                        val now = java.util.Calendar.getInstance()
                                        val dateFormat = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
                                        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                        
                                        val alertItem = AlertItem(
                                            id = getFieldValue(dateItem, "user_date_no")?.toString() ?: java.util.UUID.randomUUID().toString(),
                                            date = dateFormat.format(now.time),
                                            time = timeFormat.format(now.time),
                                            content = getFieldValue(dateItem, "user_date_title")?.toString() ?: ""
                                        )
                                        addAlert(alertItem)
                                        Log.d("AlertRepository", "✅ 파싱 실패로 기본값으로 알림 추가됨: ${alertItem.content}")
                                    }
                                }
                                Log.d("AlertRepository", "총 로드된 알림 개수: ${_alertList.size}")
                            }
                        } ?: run {
                            Log.w("AlertRepository", "results가 null입니다")
                        }
                    } else {
                        Log.w("AlertRepository", "응답 body가 null입니다")
                    }

                    // ✅ 로딩 상태 해제
                    withContext(Dispatchers.Main) {
                        if (_isLoading.isNotEmpty()) {
                            _isLoading.removeAt(_isLoading.size - 1)
                        }
                    }
                    
                    // ✅ 데이터 로딩 완료 콜백 호출
                    withContext(Dispatchers.Main) {
                        Log.d("AlertRepository", "데이터 로딩 완료 - 콜백 호출 시작 (${_onDataLoadedCallbacks.size}개)")
                        _onDataLoadedCallbacks.forEach { callback ->
                            try {
                                callback()
                            } catch (e: Exception) {
                                Log.e("AlertRepository", "콜백 실행 중 오류", e)
                            }
                        }
                    }
                    
                    true
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AlertRepository", "서버에서 일정 로드 실패: ${response.code()}")
                    Log.e("AlertRepository", "에러 메시지: ${response.message()}")
                    Log.e("AlertRepository", "에러 body: $errorBody")

                    // ✅ 현재 API 구조에서는 2차 시도 불가
                    Log.d("AlertRepository", "현재 API에서는 다른 방식이 없으므로 1차 시도 결과를 사용합니다.")

                    // ✅ 로딩 상태 해제
                    withContext(Dispatchers.Main) {
                        if (_isLoading.isNotEmpty()) {
                            _isLoading.removeAt(_isLoading.size - 1)
                        }
                    }
                    
                    false
                }
            } catch (e: Exception) {
                Log.e("AlertRepository", "=== 서버에서 일정 로드 중 전체 오류 ===", e)
                Log.e("AlertRepository", "오류 메시지: ${e.message}")
                Log.e("AlertRepository", "스택 트레이스: ${e.stackTraceToString()}")
                
                // ✅ 로딩 상태 해제
                withContext(Dispatchers.Main) {
                    if (_isLoading.isNotEmpty()) {
                        _isLoading.removeAt(_isLoading.size - 1)
                    }
                }
                
                false
            }
        }
    }

    // ✅ 안전하게 필드 값을 가져오는 헬퍼 함수
    private fun getFieldValue(obj: Any, fieldName: String): Any? {
        return try {
            when (obj) {
                is Map<*, *> -> obj[fieldName]
                else -> {
                    val field = obj.javaClass.getDeclaredField(fieldName)
                    field.isAccessible = true
                    field.get(obj)
                }
            }
        } catch (e: Exception) {
            Log.w("AlertRepository", "필드 '$fieldName' 접근 실패: ${e.message}")
            null
        }
    }

    // ✅ 서버에서 받은 날짜/시간 데이터를 파싱하는 헬퍼 함수
    private fun parseDateTimeFromServer(dateItem: Any): Pair<String, String> {
        return try {
            // user_date_time 필드에서 파싱
            val userDateTime = getFieldValue(dateItem, "user_date_time")?.toString() ?: ""

            Log.d("AlertRepository", "파싱할 날짜/시간: $userDateTime")

            // ISO 8601 형식 (2025-08-14T00:01:00.000Z) 파싱
            if (userDateTime.contains("T") && userDateTime.contains("Z")) {
                val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val date = isoFormat.parse(userDateTime)
                
                if (date != null) {
                    // ✅ 한국 시간대로 변환
                    val koreaTimeZone = java.util.TimeZone.getTimeZone("Asia/Seoul")
                    val dateFormat = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
                    val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    dateFormat.timeZone = koreaTimeZone
                    timeFormat.timeZone = koreaTimeZone
                    
                    val koreaDate = dateFormat.format(date)
                    val koreaTime = timeFormat.format(date)
                    
                    Log.d("AlertRepository", "UTC -> KST 변환: $userDateTime -> $koreaDate $koreaTime")
                    Pair(koreaDate, koreaTime)
                } else {
                    throw IllegalArgumentException("날짜 파싱 실패: $userDateTime")
                }
            } else {
                // 기존 "2024/08/13 14:30" 형식에서 파싱
                val parts = userDateTime.split(" ")
                if (parts.size >= 2) {
                    Pair(parts[0], parts[1]) // "2024/08/13", "14:30"
                } else {
                    throw IllegalArgumentException("지원하지 않는 형식: $userDateTime")
                }
            }
        } catch (e: Exception) {
            Log.e("AlertRepository", "날짜 파싱 실패", e)
            // 기본값 반환
            val now = java.util.Calendar.getInstance()
            val dateFormat = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
            val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            Pair(dateFormat.format(now.time), timeFormat.format(now.time))
        }
    }
}