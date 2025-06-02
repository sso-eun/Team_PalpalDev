package com.example.dundun_hi.data

import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

data class AlertItem(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val time: String,
    val content: String
)

object AlertRepository {
    val alertList = mutableStateListOf<AlertItem>()

    fun addAlert(alert: AlertItem) {
        alertList.add(alert)
    }

    fun deleteAlert(alert: AlertItem) {
        alertList.remove(alert)
    }

    fun updateAlert(alert: AlertItem) {
        val index = alertList.indexOfFirst { it.id == alert.id }
        if (index != -1) {
            alertList[index] = alert
        }
    }

    fun getAlertById(id: String): AlertItem? {
        return alertList.find { it.id == id }
    }
}
