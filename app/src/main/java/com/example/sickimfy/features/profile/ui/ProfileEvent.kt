package com.example.sickimfy.features.profile.ui

sealed interface ProfileEvent {
    object OnAvatarClick : ProfileEvent
    object OnUpgradePremiumClick : ProfileEvent
    object OnThemeSettingsClick : ProfileEvent
    object OnLanguageSettingsClick : ProfileEvent
}