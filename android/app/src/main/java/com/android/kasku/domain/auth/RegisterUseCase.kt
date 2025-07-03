package com.android.kasku.domain.auth

import com.android.kasku.data.auth.AuthRepository
import com.android.kasku.data.auth.AuthResult
import com.google.firebase.auth.FirebaseUser

// Jika menggunakan Hilt, tambahkan @Inject
// import javax.inject.Inject
// class RegisterUseCase @Inject constructor(private val authRepository: AuthRepository) {
class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(username: String, email: String, password: String): AuthResult<FirebaseUser> {
        // Use case ini memanggil fungsi registerUser dari repository
        return authRepository.registerUser(username, email, password)
    }
}