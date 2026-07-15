package com.example.sickimfy.features.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.sickimfy.core.data.preferences.ThemeMode
import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import com.example.sickimfy.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val preferences: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.OnAvatarClick -> {
                Unit
            }
            ProfileEvent.OnUpgradePremiumClick -> {
                upgradePremium()
            }
            ProfileEvent.OnThemeSettingsClick -> {
                toggleTheme()
            }
            ProfileEvent.OnLanguageSettingsClick -> {
                toggleLanguage()
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getProfile() }
                .onSuccess { profile ->
                    _uiState.value = ProfileUiState(
                        userName = profile.displayName,
                        avatarUrl = profile.avatarUrl,
                        isPremium = profile.isPremium,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun upgradePremium() {
        if (_uiState.value.isUpgrading || _uiState.value.isPremium) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpgrading = true, errorMessage = null) }
            runCatching { repository.upgrade() }
                .onSuccess { profile -> _uiState.update { it.copy(isUpgrading = false, isPremium = profile.isPremium) } }
                .onFailure { error -> _uiState.update { it.copy(isUpgrading = false, errorMessage = error.message) } }
        }
    }

    private fun toggleTheme() = viewModelScope.launch {
        val current = preferences.preferences.first().themeMode
        preferences.setThemeMode(if (current == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK)
    }

    private fun toggleLanguage() = viewModelScope.launch {
        val current = preferences.preferences.first().languageCode
        preferences.setLanguageCode(if (current == "fa") "en" else "fa")
    }
}
