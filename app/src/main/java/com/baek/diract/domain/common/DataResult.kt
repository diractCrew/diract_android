package com.baek.diract.domain.common

//서버 호출 결과 관리
sealed interface DataResult<out T> {

    data class Success<out T>(val data: T) : DataResult<T>

    data class Error(val throwable: Throwable) :
        DataResult<Nothing>
}
