package com.example.sickimfy.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.R
import com.example.sickimfy.core.auth.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isLogin: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val errorRes: Int? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, error = null, errorRes = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, error = null, errorRes = null) }
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update { it.copy(displayName = value, error = null, errorRes = null) }
    }

    fun toggleMode() {
        _uiState.update { it.copy(isLogin = !it.isLogin, error = null, errorRes = null) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.email.isBlank() || !state.email.contains("@")) {
            _uiState.update { it.copy(errorRes = R.string.auth_invalid_email_error) }
            return
        }
        if (state.password.length < 8) {
            _uiState.update { it.copy(errorRes = R.string.auth_invalid_password_error) }
            return
        }
        if (!state.isLogin && state.displayName.isBlank()) {
            _uiState.update { it.copy(errorRes = R.string.auth_display_name_required_error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, errorRes = null) }
            runCatching {
                if (state.isLogin) {
                    sessionRepository.login(state.email, state.password)
                } else {
                    sessionRepository.register(state.email, state.password, state.displayName)
                }
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(isLoading = false, error = exception.toAuthErrorMessage())
                }
            }
        }
    }

    private fun Throwable.toAuthErrorMessage(): String = when (this) {
        is IOException -> "Unable to reach the music server. Please start the backend and try again."
        is HttpException -> when (code()) {
            400 -> "Please check the information you entered."
            401 -> "Incorrect email address or password."
            409 -> "An account with this email address already exists."
            else -> "The server could not complete your request. Please try again."
        }
        else -> "Unable to complete your request. Please try again."
    }
}
