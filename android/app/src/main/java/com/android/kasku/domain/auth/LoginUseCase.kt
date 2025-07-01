package com.android.kasku.domain.auth

import com.android.kasku.data.auth.AuthRepository
import com.android.kasku.data.auth.AuthResult
import com.google.firebase.auth.FirebaseUser

// Jika menggunakan Hilt
// import javax.inject.Inject

// class LoginUseCase @Inject constructor(private val authRepository: AuthRepository) {
class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthResult<FirebaseUser> {
        return authRepository.loginUser(email, password)
    }
}