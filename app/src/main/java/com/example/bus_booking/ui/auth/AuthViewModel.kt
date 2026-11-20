package com.example.bus_booking.ui.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bus_booking.data.model.UserDto
import com.example.bus_booking.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: UserDto? = null,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeToken()
    }

    private fun observeToken() {
        viewModelScope.launch {
            authRepository.tokenFlow.collect { token ->
                val loggedIn = !token.isNullOrBlank()
                _uiState.update { it.copy(isAuthenticated = loggedIn) }
                if (loggedIn && _uiState.value.user == null) {
                    fetchCurrentUser()
                }
            }
        }
    }

    fun fetchCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.update { it.copy(user = user) }
                }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _uiState.update {
                it.copy(errorMessage = "Enter a valid email and at least 6-character password.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.login(email, password)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            user = user,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to sign in"
                        )
                    }
                }
        }
    }

    fun register(name: String, email: String, phone: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.length < 6) {
            _uiState.update {
                it.copy(errorMessage = "Name, email, and a 6+ character password are required.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.register(name, email, phone, password)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            user = user,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Registration failed"
                        )
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                AuthUiState(isAuthenticated = false, user = null)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}