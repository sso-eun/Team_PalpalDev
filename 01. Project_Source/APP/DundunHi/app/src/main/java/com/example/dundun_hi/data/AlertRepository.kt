package com.example.dundun_hi.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.dundun_hi.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
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
                INSTANCE ?: AlertRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val _alertList = mutableStateListOf<AlertItem>()
    val alertList: List<AlertItem> = _alertList

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

                    return@withContext true
                }
            } catch (e: Exception) {
                Log.e("AlertRepository", "서버 저장 중 오류: ${e.message}", e)
                false
            }
        }
    }

    /**
     * 서버에서 사용자의 일정 목록을 가져오는 함수
     */
    suspend fun loadAlertsFromServer(userNum: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AlertRepository", "서버에서 일정 목록 로드 시작 - userNum: $userNum")

                // MemberService의 getDateList 사용
                val response = RetrofitClient.memberService.getDateList(userNum)

                if (response.isSuccessful) {
                    val dateListResponse = response.body()
                    Log.d("AlertRepository", "서버에서 일정 로드 성공: $dateListResponse")

                    // 응답 데이터가 있으면 로컬에 추가
                    dateListResponse?.results?.let { dateList ->
                        withContext(Dispatchers.Main) {
                            // 기존 데이터 초기화 후 새 데이터 추가
                            clearAlerts()
                            dateList.forEach { dateItem ->
                                val alertItem = AlertItem(
                                    date = dateItem.user_date_time.split(" ")[0], // 날짜 부분만
                                    time = dateItem.user_date_time.split(" ")[1], // 시간 부분만
                                    content = dateItem.user_date_title
                                )
                                addAlert(alertItem)
                            }
                        }
                    }

                    true
                } else {
                    Log.e("AlertRepository", "서버에서 일정 로드 실패: ${response.code()} - ${response.message()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("AlertRepository", "서버에서 일정 로드 중 오류: ${e.message}", e)
                false
            }
        }
    }
}