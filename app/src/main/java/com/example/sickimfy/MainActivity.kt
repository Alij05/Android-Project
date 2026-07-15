package com.example.sickimfy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sickimfy.core.designsystem.MusicAppTheme
import com.example.sickimfy.core.navigation.SickimfyApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicAppTheme {
                SickimfyApp()
            }
        }
    }
}
