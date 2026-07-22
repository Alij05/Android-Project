package com.example.sickimfy.core.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageCode: String = "en",
    val fontScale: Float = 1f,
    val isPremium: Boolean = false,
    val accessToken: String? = null,
//    val apiBaseUrl: String = "http://10.0.2.2:8080/"
    val apiBaseUrl: String = "http://127.0.0.1:8080/"
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val preferences: Flow<UserPreferences> = context.userPreferencesDataStore.data.map(::mapPreferences)

    suspend fun setThemeMode(mode: ThemeMode) = update(ThemeModeKey, mode.name)
    suspend fun setLanguageCode(languageCode: String) = update(LanguageKey, languageCode)
    suspend fun setFontScale(scale: Float) = update(FontScaleKey, scale.coerceIn(0.85f, 1.3f))
    suspend fun setPremium(isPremium: Boolean) = update(PremiumKey, isPremium)
    suspend fun setApiBaseUrl(url: String) = update(ApiBaseUrlKey, url)
    suspend fun setAccessToken(token: String?) {
        context.userPreferencesDataStore.edit { values ->
            if (token.isNullOrBlank()) values.remove(AccessTokenKey) else values[AccessTokenKey] = token
        }
    }

    suspend fun accessToken(): String? = preferences.first().accessToken

    private suspend fun <T> update(key: Preferences.Key<T>, value: T) {
        context.userPreferencesDataStore.edit { it[key] = value }
    }

    private fun mapPreferences(values: Preferences) = UserPreferences(
        themeMode = values[ThemeModeKey]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
        languageCode = values[LanguageKey].takeIf { it == "fa" || it == "en" } ?: "en",
        fontScale = values[FontScaleKey] ?: 1f,
        isPremium = values[PremiumKey] ?: false,
        accessToken = values[AccessTokenKey],
//        apiBaseUrl = values[ApiBaseUrlKey] ?: "http://10.0.2.2:8080/"
        apiBaseUrl = values[ApiBaseUrlKey] ?: "http://127.0.0.1:8080/"
    )

    private companion object {
        val ThemeModeKey = stringPreferencesKey("theme_mode")
        val LanguageKey = stringPreferencesKey("language_code")
        val FontScaleKey = floatPreferencesKey("font_scale")
        val PremiumKey = booleanPreferencesKey("is_premium")
        val AccessTokenKey = stringPreferencesKey("access_token")
        val ApiBaseUrlKey = stringPreferencesKey("api_base_url")
    }
}
