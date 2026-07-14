package com.example.sickimfy.features.profile.ui

data class ProfileUiState(
    val userName: String = "Sickimfy User",
    val avatarUrl: String? = null,
    val isPremium: Boolean = false,
    val isUpgrading: Boolean = false // Used for the mock upgrade loading process
)