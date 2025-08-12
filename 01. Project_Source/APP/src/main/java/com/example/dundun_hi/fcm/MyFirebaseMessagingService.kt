package com.example.dundun_hi.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.dundun_hi.MainActivity
import com.example.dundun_hi.R
import com.example.dundun_hi.data.FcmTokenRequest
import com.example.dundun_hi.network.RetrofitClient
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.notification?.let {
            sendNotification(it.title, it.body)
        }
    }

    override fun onNewToken(token: String) {
        android.util.Log.d("FCM", "onNewToken: $token")
        super.onNewToken(token)
        // user_num을 SharedPreferences에서 불러오기
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userNum = prefs.getString("user_num", null)?.toIntOrNull() ?: return

        // 서버로 토큰 전송 (코루틴 사용)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val req = FcmTokenRequest(user_num = userNum, fcm_token = token)
                val res = RetrofitClient.memberService.sendFcmToken(req)
                if (res.isSuccessful) {
                    android.util.Log.d("FCM", "onNewToken: 토큰 서버 전송 성공")
                } else {
                    android.util.Log.e("FCM", "onNewToken: 토큰 서버 전송 실패: ${res.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("FCM", "onNewToken: 토큰 서버 전송 예외: ${e.message}")
            }
        }
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title ?: "든든하이")
            .setContentText(messageBody ?: "")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "FCM Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
} 