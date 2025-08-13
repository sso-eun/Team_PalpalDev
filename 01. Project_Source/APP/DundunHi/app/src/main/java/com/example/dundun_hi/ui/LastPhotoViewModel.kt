package com.example.dundun_hi.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.TalkRepository
import com.example.dundun_hi.model.SharedPhoto
import com.example.dundun_hi.network.RetrofitClient // RetrofitClient를 사용하기 위해 임포트
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

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        Log.d("LastPhotoVM", "refresh() 호출 – viewerId=$viewerId")
        runCatching {
            // 1. 서버로부터 원본 데이터를 가져옵니다.
            val originalList = repo.fetchTalkList(viewerId)
            Log.d("LastPhotoVM", "서버에서 ${originalList.size} 개 받음")

            // 2. 화면에 표시하기 전에 데이터를 가공합니다.
            val processedList = originalList.map { photo ->
                // API 응답의 remoteUrl (예: /down/talk/16)을 완전한 주소로 만듭니다.
                val fullUrl = photo.remoteUrl?.let { remotePath ->
                    if (remotePath.startsWith("http")) {
                        remotePath // 이미 완전한 주소면 그대로 사용
                    } else {
                        // 아니라면 RetrofitClient의 기본 URL과 합쳐줍니다.
                        RetrofitClient.apiBaseUrl + remotePath.removePrefix("/")
                    }
                }

                // fromMe 값에 따라 작성자 이름을 정해줍니다.
                // TODO: "우리딸" 부분은 필요하다면 실제 상대방 이름으로 바꿔주세요.
                val author = if (photo.fromMe) "나" else "우리딸"

                // 3. fullUrl과 authorName을 포함한 새로운 SharedPhoto 객체를 만듭니다.
                photo.copy(
                    remoteUrl = fullUrl,
                    authorName = author
                )
            }

            // 4. 가공이 완료된 리스트로 화면 상태를 업데이트합니다.
            _photos.value = processedList

        }.onFailure {
            Log.e("LastPhotoVM", "refresh 실패", it)
        }
    }


    fun onPhotoPicked(uri: Uri) = viewModelScope.launch {
        // 사용자가 갤러리에서 사진을 고르면, 우선 '나'의 이름으로 임시 객체를 만듭니다.
        val temp = SharedPhoto(fromMe = true, localUri = uri, authorName = "나")
        _photos.update { listOf(temp) + it } // 기존 목록의 맨 앞에 추가해서 바로 보여줍니다.

        runCatching {
            // 사진을 서버에 업로드합니다.
            repo.uploadAndSend(context, senderId, receiverId, uri)
            // 업로드가 성공하면, 서버의 최신 목록으로 다시 새로고침합니다.
            refresh()
        }.onFailure {
            // 업로드에 실패하면, 보여줬던 임시 객체를 목록에서 다시 제거합니다.
            _photos.update { list -> list.filter { it.id != temp.id } }
        }
    }
}