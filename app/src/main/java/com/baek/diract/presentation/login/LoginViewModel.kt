package com.baek.diract.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baek.diract.domain.common.DataResult
import com.baek.diract.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.None)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Repository의 전역 로그인 상태를 직접 노출
    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn

    init {
        checkLoginStatus()
    }

    // 로그인 상태 확인
    private fun checkLoginStatus() {
        if (authRepository.isLoggedIn.value) {
            _authState.value = AuthState.LoggedIn(
                email = authRepository.getCurrentUser()?.email ?: ""
            )
        }
    }

    // 테스트용 로그인
    fun login() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.login(TEST_EMAIL, TEST_PASSWORD)) {
                is DataResult.Success -> {
                    // isLoggedIn은 AuthStateListener가 자동 업데이트
                    _authState.value = AuthState.LoggedIn(
                        email = result.data.email ?: ""
                    )
                }

                is DataResult.Error -> {
                    _authState.value = AuthState.Error(
                        message = result.throwable.message ?: "로그인에 실패했습니다."
                    )
                }
            }
        }
    }

    // 로그아웃
    fun logout() {
        authRepository.logout()
        // isLoggedIn은 AuthStateListener가 자동 업데이트
        _authState.value = AuthState.LoggedOut
    }

    companion object {
        // 테스트용 계정 정보
        private const val TEST_EMAIL = "android1234@android.com"
        private const val TEST_PASSWORD = "android1234"
    }
}
