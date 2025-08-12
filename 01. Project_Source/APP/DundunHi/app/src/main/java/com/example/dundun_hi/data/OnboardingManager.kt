package com.example.dundun_hi.data

import android.content.Context

object OnboardingManager {
    private const val PREFS_NAME = "onboarding_prefs"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

    // '온보딩 완료' 상태를 true로 저장하는 함수
    fun setOnboardingCompleted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }

    // '온보딩 완료' 상태를 읽어오는 함수 (기본값은 false)
    fun hasCompletedOnboarding(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
}