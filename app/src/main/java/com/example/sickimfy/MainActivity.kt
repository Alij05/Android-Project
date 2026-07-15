package com.example.sickimfy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import com.example.sickimfy.core.designsystem.MusicAppTheme
import com.example.sickimfy.core.data.preferences.ThemeMode
import com.example.sickimfy.core.data.preferences.UserPreferences
import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import com.example.sickimfy.core.navigation.SickimfyApp
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var userPreferences: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val preferences by userPreferences.preferences.collectAsStateWithLifecycle(UserPreferences())
            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = when (preferences.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            MusicAppTheme(darkTheme = useDarkTheme) {
                SickimfyApp()
            }
        }
    }
}
