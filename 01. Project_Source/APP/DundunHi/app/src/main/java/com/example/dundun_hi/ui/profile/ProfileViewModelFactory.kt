package com.example.dundun_hi.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dundun_hi.data.UserRepository

/**
 * ProfileViewModel을 생성할 때, UserRepository와 userNum을 주입해 주는 Factory
 */
class ProfileViewModelFactory(
    private val repository: UserRepository,
    private val userNum: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(repository, userNum) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
