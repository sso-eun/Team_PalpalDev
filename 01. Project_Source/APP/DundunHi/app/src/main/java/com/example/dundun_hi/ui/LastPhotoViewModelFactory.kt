package com.example.dundun_hi.ui


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dundun_hi.data.TalkRepository

class LastPhotoViewModelFactory(
    private val context: Context,
    private val userNum: Int,
    private val guardianId: Int,
    private val repo: TalkRepository = TalkRepository()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LastPhotoViewModel(context, repo, userNum, guardianId) as T
}
