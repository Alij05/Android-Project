package com.example.sickimfy.features.profile.ui

data class ProfileUiState(
    val userName: String = "",
    val avatarUrl: String? = null,
    val isPremium: Boolean = false,
    val isLoading: Boolean = true,
    val isUpgrading: Boolean = false,
    val errorMessage: String? = null
)
