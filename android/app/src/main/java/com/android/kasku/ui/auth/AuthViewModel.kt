package com.android.kasku.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kasku.data.auth.AuthRepositoryImpl
import com.android.kasku.data.auth.AuthResult
import com.android.kasku.domain.auth.LoginUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

// Menggunakan konstruktor standar. Untuk Hilt, Anda akan menggunakan @HiltViewModel dan @Inject
// @HiltViewModel
// class AuthViewModel @Inject constructor(
//     private val loginUseCase: LoginUseCase,
//     private val registerUseCase: RegisterUseCase // Jika nanti Anda juga tambahkan register
// ) : ViewModel() {

class AuthViewModel : ViewModel() {
    // Inisialisasi manual untuk ViewModel tanpa DI (cocok untuk contoh ini)
    private val authRepository: AuthRepositoryImpl = AuthRepositoryImpl(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
    private val loginUseCase: LoginUseCase = LoginUseCase(authRepository)

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var loginSuccess by mutableStateOf(false)
        private set

    fun onEmailChange(newEmail: String) {
        email = newEmail
        errorMessage = null // Hapus pesan error saat input berubah
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        errorMessage = null // Hapus pesan error saat input berubah
    }

    fun login() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password cannot be empty."
            return
        }

        isLoading = true
        errorMessage = null
        loginSuccess = false

        viewModelScope.launch {
            when (val result = loginUseCase(email, password)) {
                is AuthResult.Success -> {
                    loginSuccess = true
                    // Navigasi akan ditangani di Composable
                }
                is AuthResult.Error -> {
                    errorMessage = result.message
                }
            }
            isLoading = false
        }
    }

    fun resetState() {
        isLoading = false
        errorMessage = null
        loginSuccess = false
        // email dan password tidak direset agar pengguna bisa edit jika salah
    }
}