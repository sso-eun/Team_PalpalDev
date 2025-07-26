//AlertRepository
package com.example.dundun_hi.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID
import com.example.dundun_hi.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AlertItem(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val time: String,
    val content: String
)

data class SetDateRequest(
    val user_num: Int,
    val user_date_title: String,
    val user_date_time: String,
    val user_date_info: String
)

class AlertRepository private constructor(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    val alertList = mutableStateListOf<AlertItem>()

    init {
        // 앱 시작 시 저장된 알림 데이터 로드
        loadAlerts()
    }

    fun addAlert(alert: AlertItem) {
        alertList.add(alert)
        saveAlerts()
    }

    fun deleteAlert(alert: AlertItem) {
        alertList.remove(alert)
        saveAlerts()
    }

    fun updateAlert(alert: AlertItem) {
        val index = alertList.indexOfFirst { it.id == alert.id }
        if (index != -1) {
            alertList[index] = alert
            saveAlerts()
        }
    }

    fun getAlertById(id: String): AlertItem? {
        return alertList.find { it.id == id }
    }

    private fun saveAlerts() {
        val alertsJson = gson.toJson(alertList.toList())
        sharedPreferences.edit().putString(KEY_ALERTS, alertsJson).apply()
    }

    private fun loadAlerts() {
        val alertsJson = sharedPreferences.getString(KEY_ALERTS, null)
        if (alertsJson != null) {
            val type = object : TypeToken<List<AlertItem>>() {}.type
            val loadedAlerts = gson.fromJson<List<AlertItem>>(alertsJson, type)
            alertList.clear()
            alertList.addAll(loadedAlerts)
        }
    }

    // 서버에 일정 추가
    suspend fun addAlertToServer(userNum: Int, title: String, time: String, info: String): Boolean = withContext(Dispatchers.IO) {
        val body = SetDateRequest(
            user_num = userNum,
            user_date_title = title,
            user_date_time = time,
            user_date_info = info
        )
        val res = RetrofitClient.memberService.setDate(body)
        res.rsCode == 200
    }

    companion object {
        private const val PREF_NAME = "alerts_preferences"
        private const val KEY_ALERTS = "saved_alerts"
        
        @Volatile
        private var INSTANCE: AlertRepository? = null
        
        fun getInstance(context: Context): AlertRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AlertRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
