package com.android.kasku.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kasku.data.profile.ProfileRepositoryImpl
import com.android.kasku.data.profile.ProfileResult
import com.android.kasku.data.profile.UserData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

// Hapus parameter dari konstruktor
class ProfileViewModel : ViewModel() {

    // Buat dependencies di dalam ViewModel, sama seperti AuthViewModel
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val httpClient: OkHttpClient = OkHttpClient()
    private val profileRepository: ProfileRepositoryImpl = ProfileRepositoryImpl(firebaseAuth, httpClient)

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _updateInProgress = MutableStateFlow(false)
    val updateInProgress: StateFlow<Boolean> = _updateInProgress.asStateFlow()

    fun fetchUserProfile(context: Context) {
        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            when (val result = profileRepository.getUserProfile(context)) {
                is ProfileResult.Success -> {
                    _uiState.value = ProfileUiState.Success(result.data)
                }
                is ProfileResult.Error -> {
                    _uiState.value = ProfileUiState.Error(result.message)
                }
            }
        }
    }

    fun updateUserProfile(
        occupation: String,
        income: Long,
        financialGoals: String,
        context: Context,
        onResult: (Boolean, String?) -> Unit
    ) {
        _updateInProgress.value = true
        viewModelScope.launch {
            when (val result = profileRepository.updateUserProfile(
                occupation, income, financialGoals,
                context = context
            )) {
                is ProfileResult.Success -> {
                    // Refresh profile
                    fetchUserProfile(context)
                    onResult(true, null)
                }
                is ProfileResult.Error -> {
                    onResult(false, result.message)
                }
            }
            _updateInProgress.value = false
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}

// Jangan lupa definisikan ProfileUiState di file yang sama jika belum ada
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val userData: UserData) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}