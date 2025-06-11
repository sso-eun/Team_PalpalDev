package com.example.dundun_hi.ui


import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.model.SharedPhoto
import com.example.dundun_hi.data.TalkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LastPhotoViewModel(
    private val context: Context,
    private val repo: TalkRepository,
    private val userNum: Int,
    private val guardianId: Int
) : ViewModel() {

    private val _photos = MutableStateFlow<List<SharedPhoto>>(emptyList())
    val photos: StateFlow<List<SharedPhoto>> = _photos.asStateFlow()

    fun onPhotoPicked(uri: Uri) = viewModelScope.launch {
        val temp = SharedPhoto(fromMe = true, localUri = uri)
        _photos.update { it + temp }

        runCatching {
            val url = repo.uploadAndSend(context, userNum, guardianId, uri)
            _photos.update { list ->
                list.map { if (it.id == temp.id) it.copy(remoteUrl = url) else it }
            }
        }.onFailure {
            _photos.update { it - temp }            // 실패 시 항목 제거
        }
    }
}
