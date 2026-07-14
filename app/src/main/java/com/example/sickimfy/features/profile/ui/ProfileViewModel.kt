package com.example.sickimfy.features.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.OnAvatarClick -> {
                // Logic to open image picker can be triggered here via a SharedFlow side-effect
            }
            ProfileEvent.OnUpgradePremiumClick -> {
                simulatePremiumUpgrade()
            }
            ProfileEvent.OnThemeSettingsClick -> {
                // Navigate to Theme Settings
            }
            ProfileEvent.OnLanguageSettingsClick -> {
                // Navigate to Language Settings
            }
        }
    }

    private fun simulatePremiumUpgrade() {
        // Prevent multiple clicks while upgrading or if already premium
        if (_uiState.value.isUpgrading || _uiState.value.isPremium) return

        viewModelScope.launch {
            // 1. Show loading indicator
            _uiState.update { it.copy(isUpgrading = true) }

            // 2. Simulate network request to payment gateway / server
            delay(2000)

            // 3. Complete transaction and grant premium status
            _uiState.update {
                it.copy(
                    isUpgrading = false,
                    isPremium = true
                )
            }
        }
    }
}