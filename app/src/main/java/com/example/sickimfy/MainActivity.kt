package com.example.sickimfy

import android.os.Bundle
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sickimfy.core.data.preferences.ThemeMode
import com.example.sickimfy.core.data.preferences.UserPreferences
import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import com.example.sickimfy.core.designsystem.MusicAppTheme
import com.example.sickimfy.core.navigation.SickimfyApp
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val preferences by userPreferences.preferences.collectAsStateWithLifecycle(
                UserPreferences()
            )

            val systemDark = isSystemInDarkTheme()

            val useDarkTheme = when (preferences.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val language = preferences.languageCode
            val baseContext = LocalContext.current
            val locale = remember(language) { Locale(language) }
            val localizedConfiguration = remember(language, baseContext) {
                Configuration(baseContext.resources.configuration).apply { setLocale(locale) }
            }
            val localizedContext = remember(localizedConfiguration, baseContext) {
                baseContext.createConfigurationContext(localizedConfiguration)
            }
            val layoutDirection = if (language == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr

            MusicAppTheme(
                darkTheme = useDarkTheme
            ) {
                CompositionLocalProvider(
                    LocalContext provides localizedContext,
                    LocalConfiguration provides localizedConfiguration,
                    LocalLayoutDirection provides layoutDirection
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
