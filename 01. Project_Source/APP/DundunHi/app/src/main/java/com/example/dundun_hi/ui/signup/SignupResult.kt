package com.example.dundun_hi.ui.signup

sealed class SignupResult {
    object Idle : SignupResult()
    data class Success(val userId: String, val userNum: String) : SignupResult()
    data class Error(val reason: String) : SignupResult()
}