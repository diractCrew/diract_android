package com.baek.diract.domain.repository

import com.baek.diract.domain.common.DataResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    // 로그인 상태 Flow (전역에서 관찰 가능)
    val isLoggedIn: StateFlow<Boolean>

    // 현재 로그인된 사용자 조회
    fun getCurrentUser(): FirebaseUser?

    // 이메일/비밀번호 로그인
    suspend fun login(email: String, password: String): DataResult<FirebaseUser>

    // 로그아웃
    fun logout()
}
