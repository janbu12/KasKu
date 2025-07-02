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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

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

    var registerUsername by mutableStateOf("")
        private set
    var registerEmail by mutableStateOf("")
        private set
    var registerPassword by mutableStateOf("")
        private set
    var registerSuccess by mutableStateOf(false)
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

    fun registerUser() {
        if (registerUsername.isBlank() || registerEmail.isBlank() || registerPassword.isBlank()) {
            errorMessage = "Username, email, and password cannot be empty"
            return
        }

        isLoading = true
        errorMessage = null
        registerSuccess = false

        viewModelScope.launch {
            val  client = OkHttpClient()
            val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

            val jsonObject = JSONObject().apply {
                put("username", registerUsername)
                put("email", registerEmail)
                put("password", registerPassword)
            }
            val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)

            val url = "http://192.168.1.12:1234/auth/register"

            val request = okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    registerSuccess = true
                    // Opsional: parse responseBody jika backend mengembalikan data yang relevan
                    // val responseJson = JSONObject(responseBody)
                    // val userId = responseJson.getString("userId")
                    // Log.d("AuthViewModel", "User registered: $userId")
                } else {
                    val errorJson = responseBody?.let { JSONObject(it) }
                    errorMessage = errorJson?.getString("message") ?: "Registration failed: ${response.code}"
                }
            } catch (e: Exception) {
                errorMessage = "Network error or server unreachable: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
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
}