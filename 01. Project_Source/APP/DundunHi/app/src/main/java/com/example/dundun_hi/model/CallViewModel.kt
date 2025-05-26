// CallViewModel.kt
package com.example.dundun_hi.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.ui.screen.CallShortcut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CallViewModel : ViewModel() {
    private val _shortcuts = MutableStateFlow<List<CallShortcut>>(emptyList())
    val shortcuts: StateFlow<List<CallShortcut>> = _shortcuts

    init {
        loadShortcuts()
    }

    fun loadShortcuts() {
        viewModelScope.launch {
            // TODO: 서버나 DB 호출 로직으로 교체
            _shortcuts.value = listOf(
                CallShortcut("첫째아들", "01012341234"),
                CallShortcut("남편",    "01056785678"),
                CallShortcut("막내",    "01099998888")
            )
        }
    }
}
