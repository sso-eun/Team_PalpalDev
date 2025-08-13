//package com.example.dundun_hi.model
//
//import androidx.lifecycle.ViewModel
//import com.example.dundun_hi.R              // ← 리소스 import
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//
//class PhotoViewModel : ViewModel() {
//
//    private val _photos = MutableStateFlow(
//        listOf(
//            SharedPhoto(R.drawable.img1,   fromMe = true),
//            SharedPhoto(R.drawable.img2, fromMe = false),
//            SharedPhoto(R.drawable.img3,    fromMe = true)
//        )
//    )
//    val photos: StateFlow<List<SharedPhoto>> = _photos
//
//    /** + 버튼 눌렀을 때 임시 사진 추가 (테스트용) */
//    fun addPhoto() {
//        // 순환 추가 예시
//        val next = when ((_photos.value.size) % 3) {
//            0 -> R.drawable.img1
//            1 -> R.drawable.img2
//            else -> R.drawable.img3
//        }
//        _photos.value = _photos.value + SharedPhoto(next, fromMe = true)
//    }
//}
