package com.example.enter_exit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.example.enter_exit.ui.theme.Enter_ExitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Enter_ExitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ActivityScreen()
                }
            }
        }
    }
}
