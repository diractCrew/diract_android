package com.baek.diract.presentation.common

//ui 상태 관리
sealed interface UiState<out T> {
    //아무 상태도 아닐때(초기상태)
    data object None : UiState<Nothing>

    data object Loading : UiState<Nothing>

    data class Success<out T>(val data: T) : UiState<T>

    data class Error(val message: String? = null, val throwable: Throwable? = null) :
        UiState<Nothing>
}
