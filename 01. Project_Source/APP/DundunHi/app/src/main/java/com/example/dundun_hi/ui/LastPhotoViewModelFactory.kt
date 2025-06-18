package com.example.dundun_hi.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dundun_hi.data.TalkRepository

class LastPhotoViewModelFactory(
    private val context: Context,
    private val senderId: Int,
    private val receiverId: Int,
    private val viewerId: Int,
    private val repo: TalkRepository = TalkRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LastPhotoViewModel(context, repo, senderId, receiverId, viewerId) as T
}
