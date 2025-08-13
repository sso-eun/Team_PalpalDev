package com.example.dundun_hi.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object GetBaseTimeUtil {
    private val issuance = listOf("0200","0500","0800","1100","1400","1700","2000","2300")
    fun getBase(now: Date = Date()): Pair<String, String> {
        val cal = Calendar.getInstance().apply { time = now }
        // ① readyList 생성
        val readyList = issuance.map { t ->
            val h = t.substring(0,2).toInt()
            val m = t.substring(2).toInt() + 10
            val d = Calendar.getInstance().apply {
                time = now; set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m)
                set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
            }
            t to d.time
        }
        // ② 후보 필터링
        val candidates = readyList.filter { now >= it.second }
        val baseTime = if (candidates.isNotEmpty()) candidates.last().first else "2300"
        // ③ baseDate 계산
        if (candidates.isEmpty()) cal.add(Calendar.DAY_OF_MONTH, -1)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        return sdf.format(cal.time) to baseTime
    }
}
