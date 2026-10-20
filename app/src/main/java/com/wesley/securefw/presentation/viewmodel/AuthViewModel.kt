package com.wesley.securefw.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wesley.securefw.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val userEmail: String? = null,
    val userId: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        checkAuthentication()
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(error = "Preencha todos os campos")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = authRepository.signUp(email, password)
            result.onSuccess { user ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    userEmail = user.email,
                    userId = user.uid
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message ?: "Erro ao cadastrar"
                )
            }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(error = "Preencha todos os campos")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = authRepository.signIn(email, password)
            result.onSuccess { user ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    userEmail = user.email,
                    userId = user.uid
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message ?: "Erro ao fazer login"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _state.value = AuthState()
        }
    }

    private fun checkAuthentication() {
        viewModelScope.launch {
            val isLogged = authRepository.isUserLoggedIn()
            val user = authRepository.getCurrentUser()
            if (isLogged && user != null) {
                _state.value = _state.value.copy(
                    isAuthenticated = true,
                    userEmail = user.email,
                    userId = user.uid
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

