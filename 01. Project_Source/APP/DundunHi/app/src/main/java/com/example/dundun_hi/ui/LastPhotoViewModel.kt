package com.example.dundun_hi.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.TalkRepository
import com.example.dundun_hi.data.RealUserRepository
import com.example.dundun_hi.model.SharedPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LastPhotoViewModel(
    private val context: Context,
    private val repo: TalkRepository,
    private val senderId: Int,
    private val receiverId: Int,
    private val viewerId: Int
) : ViewModel() {

    private val _photos = MutableStateFlow<List<SharedPhoto>>(emptyList())
    val photos: StateFlow<List<SharedPhoto>> = _photos

    // 연결된 사용자 이름
    private val _receiverName = MutableStateFlow<String?>(null)
    val receiverName: StateFlow<String?> = _receiverName

    // 발신자 타입 캐시
    private var _senderType: Int? = null

    init {
        refresh()
        fetchReceiverInfo()
        fetchSenderType()
    }

    // 발신자 타입 조회 및 캐시
    private fun fetchSenderType() = viewModelScope.launch {
        try {
            val userRepository = RealUserRepository()
            val memberData = userRepository.getUserByNum(senderId)
            _senderType = memberData.userType ?: 0
            Log.d("LastPhotoVM", "발신자 타입: ${_senderType} (0=시니어, 1=보호자)")
        } catch (e: Exception) {
            Log.e("LastPhotoVM", "발신자 타입 조회 실패, 기본값 사용", e)
            _senderType = 0 // 기본값: 시니어
        }
    }

    // 상대방 정보 조회
    private fun fetchReceiverInfo() = viewModelScope.launch {
        try {
            Log.d("LastPhotoVM", "상대방 정보 조회: receiverId=$receiverId")
            val userRepository = RealUserRepository()
            val memberData = userRepository.getUserByNum(receiverId)
            _receiverName.value = memberData.userId
            Log.d("LastPhotoVM", "상대방 이름: ${_receiverName.value}")
        } catch (e: Exception) {
            Log.e("LastPhotoVM", "상대방 정보 조회 중 오류", e)
            _receiverName.value = "가족"
        }
    }

    fun refresh() = viewModelScope.launch {
        Log.d("LastPhotoVM", "refresh() 호출 – viewerId=$viewerId")
        runCatching {
            val list = repo.fetchTalkList(viewerId)
            Log.d("LastPhotoVM", "서버에서 ${list.size} 개 받음")
            _photos.value = list
        }.onFailure {
            Log.e("LastPhotoVM", "refresh 실패", it)
        }
    }

    // LastPhotoViewModel.kt의 onPhotoPicked 함수 수정
    fun onPhotoPicked(uri: Uri) = viewModelScope.launch {
        Log.d("LastPhotoVM", "📷 사진 선택됨: $uri")

        val temp = SharedPhoto(fromMe = true, localUri = uri)
        _photos.update { it + temp }

        runCatching {
            Log.d("LastPhotoVM", "🔄 업로드 시작: senderId=$senderId, receiverId=$receiverId, senderType=${_senderType}")

            repo.uploadAndSend(
                context = context,
                senderId = senderId,
                receiverId = receiverId,
                localUri = uri,
                senderType = _senderType ?: 0
            )

            Log.d("LastPhotoVM", "✅ 업로드 성공!")
            refresh()

        }.onFailure { error ->
            _photos.update { it - temp }
            Log.e("LastPhotoVM", "❌ 사진 업로드/전송 실패: ${error.message}", error)

            // 에러 타입별 상세 로그
            when (error) {
                is java.net.ConnectException -> Log.e("LastPhotoVM", "🌐 네트워크 연결 실패")
                is java.net.SocketTimeoutException -> Log.e("LastPhotoVM", "⏰ 네트워크 타임아웃")
                is IllegalArgumentException -> Log.e("LastPhotoVM", "📝 잘못된 파라미터: ${error.message}")
                else -> Log.e("LastPhotoVM", "🔥 알 수 없는 에러: ${error.javaClass.simpleName}")
            }
        }
    }
}