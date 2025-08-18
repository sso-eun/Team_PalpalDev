// app/src/main/java/com/example/dundun_hi/ui/LastPhotoViewModelFactory.kt
package com.example.dundun_hi.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dundun_hi.data.TalkRepository

class LastPhotoViewModelFactory(
    private val context: Context,
    private val senderId: Int,
    private val receiverId: Int,
    private val viewerId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LastPhotoViewModel::class.java)) {
            return LastPhotoViewModel(
                context = context,
                repo = TalkRepository(),
                senderId = senderId,
                receiverId = receiverId,
                viewerId = viewerId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}