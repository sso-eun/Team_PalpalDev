package com.example.dundun_hi.ui.guardianProfile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dundun_hi.data.UserRepository

class GuardianProfileViewModelFactory(
    private val repository: UserRepository,
    private val guardianUserNum: Int,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GuardianProfileViewModel(repository, guardianUserNum, context) as T
    }
}
