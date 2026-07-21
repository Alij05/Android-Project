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

            // The current release is intentionally English-only.
            val language = preferences.languageCode

            // Keep resource lookups and layout direction stable even when the device uses another locale.
            val context = androidx.compose.ui.platform.LocalContext.current
            androidx.compose.runtime.LaunchedEffect(language) {
                val locale = java.util.Locale(language)
                java.util.Locale.setDefault(locale)
                val resources = context.resources
                val config = resources.configuration
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)
            }

            val layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr

            MusicAppTheme(darkTheme = useDarkTheme) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection
                ) {
                    if (preferences.accessToken.isNullOrBlank()) {
                        com.example.sickimfy.features.auth.ui.AuthScreen()
                    } else {
                        SickimfyApp()
                    }
                }
            }
        }
    }
}
