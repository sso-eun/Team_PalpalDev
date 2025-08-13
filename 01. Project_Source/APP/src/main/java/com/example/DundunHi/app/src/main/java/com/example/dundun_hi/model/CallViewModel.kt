// CallViewModel.kt
package com.example.dundun_hi.model

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class CallViewModel(application: Application) : AndroidViewModel(application) {
    private val _shortcuts = MutableStateFlow<List<CallShortcut>>(emptyList())
    val shortcuts: StateFlow<List<CallShortcut>> = _shortcuts

    private val prefs = application.getSharedPreferences("call_shortcuts", Context.MODE_PRIVATE)

    init {
        loadShortcuts()
    }

    fun loadShortcuts() {
        viewModelScope.launch {
            val shortcutsJson = prefs.getString("shortcuts", "[]")
            val jsonArray = JSONArray(shortcutsJson)
            val loadedShortcuts = mutableListOf<CallShortcut>()
            
            for (i in 0 until jsonArray.length()) {
                val shortcutObj = jsonArray.getJSONObject(i)
                loadedShortcuts.add(
                    CallShortcut(
                        label = shortcutObj.getString("label"),
                        phoneNumber = shortcutObj.getString("phoneNumber")
                    )
                )
            }
            
            _shortcuts.value = loadedShortcuts
        }
    }

    fun saveShortcut(index: Int, label: String, phoneNumber: String) {
        viewModelScope.launch {
            val currentShortcuts = _shortcuts.value.toMutableList()
            
            // Ensure the list has enough capacity
            while (currentShortcuts.size <= index) {
                currentShortcuts.add(CallShortcut("", ""))
            }
            
            currentShortcuts[index] = CallShortcut(label, phoneNumber)
            _shortcuts.value = currentShortcuts

            // Save to SharedPreferences
            val jsonArray = JSONArray()
            currentShortcuts.forEach { shortcut ->
                val shortcutObj = JSONObject().apply {
                    put("label", shortcut.label)
                    put("phoneNumber", shortcut.phoneNumber)
                }
                jsonArray.put(shortcutObj)
            }
            
            prefs.edit()
                .putString("shortcuts", jsonArray.toString())
                .apply()
        }
    }
}
