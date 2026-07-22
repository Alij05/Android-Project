package com.example.sickimfy.core.data.preferences

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object AppLocaleManager {
    private const val PREFS_NAME = "app_locale"
    private const val LANGUAGE_KEY = "language_code"

    fun wrap(context: Context): Context {
        val language = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_KEY, "en")
            .takeIf { it == "fa" || it == "en" }
            ?: "en"
        val locale = Locale(language)
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        return context.createConfigurationContext(configuration)
    }

    fun persist(context: Context, language: String) {
        require(language == "fa" || language == "en")
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_KEY, language)
            .commit()
    }
}
