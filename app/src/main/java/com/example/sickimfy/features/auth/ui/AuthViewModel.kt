package com.example.sickimfy.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.core.auth.SessionRepository
import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isLogin: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
//    val apiBaseUrl: String = "http://10.0.2.2:8080/"
    val apiBaseUrl: String = "http://127.0.0.1:8080/"
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val preferences: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.preferences.collect { prefs ->
                _uiState.update { it.copy(apiBaseUrl = prefs.apiBaseUrl) }
            }
        }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update { it.copy(displayName = value, error = null) }
    }

    fun onApiBaseUrlChanged(value: String) {
        _uiState.update { it.copy(apiBaseUrl = value, error = null) }
        viewModelScope.launch {
            preferences.setApiBaseUrl(value)
        }
    }

    fun toggleMode() {
        _uiState.update { it.copy(isLogin = !it.isLogin, error = null) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.email.isBlank() || !state.email.contains("@")) {
            _uiState.update { it.copy(error = "لطفاً یک ایمیل معتبر وارد کنید / Please enter a valid email") }
            return
        }
        if (state.password.length < 8) {
            _uiState.update { it.copy(error = "رمز عبور باید حداقل ۸ کاراکتر باشد / Password must be at least 8 characters") }
            return
        }
        if (!state.isLogin && state.displayName.isBlank()) {
            _uiState.update { it.copy(error = "نام نمایشی الزامی است / Display name is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                if (state.isLogin) {
                    sessionRepository.login(state.email, state.password)
                } else {
                    sessionRepository.register(state.email, state.password, state.displayName)
                }
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, error = exception.message ?: "خطایی رخ داد / An error occurred") }
            }
        }
    }
}
