package com.android.kasku.data.auth

import com.google.firebase.auth.FirebaseUser


sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : AuthResult<Nothing>()
}

interface AuthRepository {
    suspend fun loginUser(email: String, password: String): AuthResult<FirebaseUser>
    suspend fun registerUser(username: String, email: String, password: String): AuthResult<FirebaseUser>
}