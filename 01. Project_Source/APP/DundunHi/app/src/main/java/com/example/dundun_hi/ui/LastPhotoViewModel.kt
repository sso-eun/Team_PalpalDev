package com.example.dundun_hi.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.TalkRepository
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

    init { refresh() }

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


    fun onPhotoPicked(uri: Uri) = viewModelScope.launch {
        val temp = SharedPhoto(fromMe = true, localUri = uri)
        _photos.update { it + temp }

        runCatching {
            // 기존 uploadAndSend는 이미 2단계로 구현되어 있음
            repo.uploadAndSend(
                context = context,
                userNum = senderId,      // senderId를 userNum으로 전달
                guardianId = receiverId, // receiverId를 guardianId로 전달
                localUri = uri
            )
            refresh()
        }.onFailure {
            _photos.update { it - temp }
            Log.e("LastPhotoVM", "사진 업로드/전송 실패", it)
        }
    }
}
