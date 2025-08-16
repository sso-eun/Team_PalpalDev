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

                //소은수정
                if (INSTANCE != null) {
                    Log.d("AlertRepository", "⚠ 기존 인스턴스 재사용")
                } else {
                    Log.d("AlertRepository", "✅ 새 인스턴스 생성 & initializeData 실행 예정")
                }

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
            val targetUserNum = getTargetUserNum()
            Log.d("AlertRepository", "=== 초기 데이터 로딩 시작 ===")
            Log.d("AlertRepository", "가져온 targetUserNum: $targetUserNum")
            
            if (targetUserNum > 0) {
                Log.d("AlertRepository", "앱 시작 시 자동 데이터 로딩 - targetUserNum: $targetUserNum")
                val success = loadAlertsFromServer(targetUserNum)
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
                Log.e("AlertRepository", "❌ 유효하지 않은 targetUserNum으로 데이터 로딩 건너뜀: $targetUserNum")
                Log.e("AlertRepository", "SharedPreferences에 user_num이 저장되지 않았거나 0입니다.")
            }
        }
    }

    // --- 08-16 은재 getTargetUserNum() 함수 신설---
    private suspend fun getTargetUserNum(): Int {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUserNum = sharedPreferences.getString("user_num", "0")?.toIntOrNull() ?: 0

        if (currentUserNum <= 0) {
            Log.e("AlertRepository", "현재 userNum을 찾을 수 없음, 0 반환")
            return 0
        }

        Log.d("AlertRepository", "역할 확인 시작 - 현재 사용자: $currentUserNum")
        try {
            // 현재 사용자가 가디언이라는 가정 하에, 연동된 시니어 정보 조회를 시도
            val response = RetrofitClient.memberService.getAuthStatusByGuardianNo(currentUserNum)

            // API 호출이 성공하고, 유효한 seniorNum을 받았다면? -> 이 사용자는 '가디언'
            if (response.isSuccessful && response.body() != null && response.body()!!.seniorNum > 0) {
                val seniorNum = response.body()!!.seniorNum
                Log.d("AlertRepository", "✅ API 응답 성공: 현재 사용자는 '가디언'. 조회할 시니어 번호는 $seniorNum 입니다.")
                return seniorNum
            }
            // API 호출이 실패했거나, seniorNum이 없다면? -> 이 사용자는 '시니어'
            else {
                Log.d("AlertRepository", "API 응답 실패 또는 시니어 정보 없음: 현재 사용자를 '시니어'로 간주. 본인 번호($currentUserNum)를 사용합니다.")
                return currentUserNum
            }
        } catch (e: Exception) {
            Log.e("AlertRepository", "역할 확인 중 예외 발생. 현재 사용자를 '시니어'로 간주.", e)
            return currentUserNum // 예외 발생 시 안전하게 본인 번호 사용
        }
    }
    // ✅ userNum 가져오기 함수
//    private fun getUserNum(): Int {
//        val fromPrefs = sharedPreferences.getString("user_num", null)?.toIntOrNull()
//
//        // 디버깅을 위한 로그
//        val prefsUserNum = sharedPreferences.getString("user_num", "null")
//        val prefsUserType = sharedPreferences.getString("user_type", "null")
//        val prefsUserId = sharedPreferences.getString("user_id", "null")
//
//        Log.d("AlertRepository", "=== SharedPreferences 확인 ===")
//        Log.d("AlertRepository", "user_num: $prefsUserNum")
//        Log.d("AlertRepository", "user_type: $prefsUserType")
//        Log.d("AlertRepository", "user_id: $prefsUserId")
//
//        return fromPrefs ?: 0
//    }

    // ✅ 공개 메서드로 수동 새로고침 지원
    fun refreshFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            val targetUserNum = getTargetUserNum()
            if (targetUserNum > 0) {
                Log.d("AlertRepository", "수동 새로고침 - targetUserNum: $targetUserNum")
                val success = loadAlertsFromServer(targetUserNum)
                Log.d("AlertRepository", "새로고침 완료 - 성공: $success, 알림 개수: ${_alertList.size}")
            } else {
                Log.w("AlertRepository", "유효하지 않은 targetUserNum으로 새로고침 건너뜀: $targetUserNum")
            }
        }
    }

    // ✅ 강제 데이터 로딩 (콜백 보장)
    fun forceRefreshFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            val targetUserNum = getTargetUserNum()
            Log.d("AlertRepository", "=== 강제 새로고침 시작 ===")
            Log.d("AlertRepository", "targetUserNum: $targetUserNum")
            
            if (targetUserNum > 0) {
                Log.d("AlertRepository", "강제 새로고침 - userNum: $targetUserNum")
                val success = loadAlertsFromServer(targetUserNum)
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
                Log.e("AlertRepository", "❌ 유효하지 않은 userNum으로 강제 새로고침 건너뜀: $targetUserNum")
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
    
    // 08-15 일정 수정 - update_date 관련 수정
    // suspend 함수로 변경하여 비동기 네트워크 통신을 지원
    suspend fun updateAlert(updatedAlert: AlertItem): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // API 명세서에 맞는 데이터 형식으로 변환
                val dateTime = "${updatedAlert.date} ${updatedAlert.time}"
                val isoDateTime = try {
                    val dateParts = updatedAlert.date.split("/")
                    val timeParts = updatedAlert.time.split(":")
                    "${dateParts[0]}-${dateParts[1]}-${dateParts[2]}T${timeParts[0]}:${timeParts[1]}:00"
                } catch (e: Exception) {
                    "${updatedAlert.date}T${updatedAlert.time}:00"
                }

                // 2단계에서 만든 Data Class를 사용하여 요청 본문 생성
                val request = UpdateAlertRequest(
                    title = updatedAlert.content,
                    dateTime = dateTime,
                    dateInfo = isoDateTime
                )

                Log.d("AlertRepository", "일정 수정 요청: ID=${updatedAlert.id}, Body=$request")

                // 1단계에서 만든 Retrofit 함수 호출
                val response = RetrofitClient.memberService.updateAlert(updatedAlert.id, request)

                if (response.isSuccessful) {
                    Log.d("AlertRepository", "일정 수정 성공. 서버 데이터 새로고침")
                    // 수정 성공 후, 서버로부터 최신 데이터를 다시 불러와 로컬 목록을 동기화
                    refreshFromServer()
                    true
                } else {
                    Log.e("AlertRepository", "일정 수정 실패: ${response.errorBody()?.string()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("AlertRepository", "일정 수정 중 예외 발생", e)
                false
            }
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
        title: String,
        dateTime: String,
        dateInfo: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. 함수 내부에서 직접 올바른 userNum을 조회합니다.
                val targetUserNum = getTargetUserNum()
                if (targetUserNum <= 0) {
                    Log.e("AlertRepository", "유효하지 않은 targetUserNum ($targetUserNum), 일정 추가 중단.")
                    return@withContext false
                }

                Log.d("AlertRepository", "서버 저장 시작 - targetUserNum: $targetUserNum, title: $title, dateTime: $dateTime, dateInfo: $dateInfo")

                val setDateRequest = com.example.dundun_hi.data.SetDateRequest(
                    user_num = targetUserNum,
                    user_date_title = title,
                    user_date_time = dateTime,
                    user_date_info = dateInfo
                )

                val response = RetrofitClient.memberService.setDate(setDateRequest)

                if (response.isSuccessful) {
                    Log.d("AlertRepository", "서버 저장 성공 (SetDateRequest): ${response.body()}")
                    // 서버 저장 성공 후, 최신 데이터로 전체 목록을 새로고침합니다.
                    loadAlertsFromServer(targetUserNum)
                    return@withContext true
                } else {
                    Log.e("AlertRepository", "서버 저장 실패 (SetDateRequest): ${response.errorBody()?.string()}")
                    return@withContext false
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

//                                        val alertItem = AlertItem(
//                                            id = getFieldValue(dateItem, "user_date_no")?.toString() ?: java.util.UUID.randomUUID().toString(),
//                                            date = date,
//                                            time = time,
//                                            content = getFieldValue(dateItem, "user_date_title")?.toString() ?: ""
//                                        )
                                        val alertItem = AlertItem(
                                            id = dateItem.user_date_no.toString(), // 직접 접근
                                            date = date,
                                            time = time,
                                            content = dateItem.user_date_title // 직접 접근
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