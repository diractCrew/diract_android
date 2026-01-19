package com.baek.diract.presentation.login

// 인증 상태
sealed interface AuthState {
    data object None : AuthState
    data object Loading : AuthState
    data class LoggedIn(val email: String) : AuthState
    data object LoggedOut : AuthState
    data class Error(val message: String) : AuthState
}
