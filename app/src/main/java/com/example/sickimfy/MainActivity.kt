package com.example.sickimfy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sickimfy.core.data.preferences.ThemeMode
import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import com.example.sickimfy.core.data.preferences.AppLocaleManager
import com.example.sickimfy.core.designsystem.MusicAppTheme
import com.example.sickimfy.core.navigation.SickimfyApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferencesDataStore

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocaleManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST)
        }

        setContent {
            val preferences by userPreferences.preferences.collectAsStateWithLifecycle(
                initialValue = null
            )
            val loadedPreferences = preferences ?: return@setContent

            val systemDark = isSystemInDarkTheme()

            val useDarkTheme = when (loadedPreferences.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val language = loadedPreferences.languageCode
            val layoutDirection = if (language == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr
            val fontScale = loadedPreferences.fontScale

            LaunchedEffect(language) {
                if (resources.configuration.locales[0].language != language) {
                    AppLocaleManager.persist(this@MainActivity, language)
                    recreate()
                }
            }

            MusicAppTheme(
                darkTheme = useDarkTheme,
                fontScale = fontScale
            ) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides layoutDirection
                ) {
                    if (loadedPreferences.accessToken.isNullOrBlank()) {
                        com.example.sickimfy.features.auth.ui.AuthScreen()
                    } else {
                        SickimfyApp()
                    }
                }
            }
        }
    }

    private companion object {
        const val NOTIFICATION_PERMISSION_REQUEST = 1001
    }
}
