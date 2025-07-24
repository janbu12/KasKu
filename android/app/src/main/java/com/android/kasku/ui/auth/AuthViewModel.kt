package com.android.kasku.ui.auth

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kasku.data.auth.AuthRepositoryImpl
import com.android.kasku.data.auth.AuthResult
import com.android.kasku.domain.auth.LoginUseCase
import com.android.kasku.domain.auth.RegisterUseCase
import com.android.kasku.utils.DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Menggunakan konstruktor standar. Untuk Hilt, Anda akan menggunakan @HiltViewModel dan @Inject
// @HiltViewModel
// class AuthViewModel @Inject constructor(
//     private val loginUseCase: LoginUseCase,
//     private val registerUseCase: RegisterUseCase // Jika nanti Anda juga tambahkan register
// ) : ViewModel() {

class AuthViewModel : ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val authRepository: AuthRepositoryImpl = AuthRepositoryImpl(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance(), onTokenExpired = {logout()})
    private val loginUseCase: LoginUseCase = LoginUseCase(authRepository)
    private val registerUseCase: RegisterUseCase = RegisterUseCase(authRepository)

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

    var registerUsername by mutableStateOf("")
        private set
    var registerEmail by mutableStateOf("")
        private set
    var registerPassword by mutableStateOf("")
        private set
    var registerConfirmPassword by mutableStateOf("")
        private set
    var registerSuccess by mutableStateOf(false)
        private set

    var isUserLoggedIn by mutableStateOf(false)
        private set
    var authCheckCompleted by mutableStateOf(false)
        private set

    init {
        checkUserLoggedIn()
    }

    fun onEmailChange(newEmail: String) {
        email = newEmail
        errorMessage = null // Hapus pesan error saat input berubah
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        errorMessage = null // Hapus pesan error saat input berubah
    }

    fun login(context: Context) {
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
                    isUserLoggedIn = true
                }
                is AuthResult.Error -> {
                    errorMessage = result.message
                }
            }
            isLoading = false
        }
    }

    fun resetLoginState() {
        isLoading = false
        errorMessage = null
        loginSuccess = false
        email = ""
        password = ""
    }

    fun onRegisterUsernameChange(newUsername: String) {
        registerUsername = newUsername
        errorMessage = null
    }

    fun onRegisterEmailChange(newEmail: String) {
        registerEmail = newEmail
        errorMessage = null
    }

    fun onRegisterPasswordChange(newPassword: String) {
        registerPassword = newPassword
        errorMessage = null
    }

    fun onRegisterConfirmPasswordChange(newConfirmPassword: String) {
        registerConfirmPassword = newConfirmPassword
        errorMessage = null
    }

    fun registerUser() { // Nama fungsi diubah dari registerUserWithBackend menjadi registerUser
        if (registerUsername.isBlank() || registerEmail.isBlank() || registerPassword.isBlank() || registerConfirmPassword.isBlank()) {
            errorMessage = "Username, email, password, and confirm password cannot be empty."
            return
        }

        if (registerPassword != registerConfirmPassword) {
            errorMessage = "Password and confirm password do not match."
            return
        }

        isLoading = true
        errorMessage = null
        registerSuccess = false

        viewModelScope.launch {
            when (val result = registerUseCase(registerUsername, registerEmail, registerPassword)) {
                is AuthResult.Success -> {
                    registerSuccess = true
                    // Opsional: Anda bisa juga langsung login user setelah register sukses
                    // loginSuccess = true
                }
                is AuthResult.Error -> {
                    errorMessage = result.message
                }
            }
            isLoading = false
        }
    }

    fun resetRegisterState() {
        isLoading = false
        errorMessage = null
        registerSuccess = false
        registerUsername = ""
        registerEmail = ""
        registerPassword = ""
    }

    fun checkUserLoggedIn() {
        viewModelScope.launch {
            val currentUser = firebaseAuth.currentUser
            isUserLoggedIn = (currentUser != null)
            if (currentUser != null) {
                Log.d("AuthVM", "User is logged in (client-side). UID: ${currentUser.uid}")
                // Coba ambil token untuk memastikan sesi aktif
                try {
                    val tokenCheckResult = currentUser.getIdToken(true).await()
                    if (tokenCheckResult?.token != null) {
                        Log.d("AuthVM", "checkUserLoggedIn: Token fetched successfully.")
                    } else {
                        Log.e("AuthVM", "checkUserLoggedIn: Token is NULL even after successful check! Error: ${tokenCheckResult}")
                        // Token null, mungkin sesi invalid, paksa logout lokal
                        firebaseAuth.signOut()
                        isUserLoggedIn = false
                        errorMessage = "Sesi Anda tidak valid. Silakan login kembali."
                    }
                } catch (e: Exception) {
                    Log.e("AuthVM", "checkUserLoggedIn: Error fetching token during check", e)
                    // Terjadi error saat mengambil token, anggap sesi tidak valid
                    firebaseAuth.signOut()
                    isUserLoggedIn = false
                    errorMessage = "Terjadi masalah autentikasi. Silakan login kembali."
                }
            } else {
                Log.d("AuthVM", "No user logged in (client-side).")
            }
            authCheckCompleted = true
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                firebaseAuth.signOut()
                isUserLoggedIn = false
                errorMessage = null
                loginSuccess = false
                resetLoginState()
                resetRegisterState()

                // Opsional: Hapus data lokal lainnya jika ada
            } catch (e: Exception) {
                errorMessage = "Failed to log out: ${e.localizedMessage}"
                e.printStackTrace()
            }
        }
    }
}