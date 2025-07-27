package com.android.kasku.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kasku.data.dashboard.AIInsightsRequest
import com.android.kasku.data.dashboard.DashboardData
import com.android.kasku.data.dashboard.DashboardRepository
import com.android.kasku.data.dashboard.DashboardRepositoryImpl
import com.android.kasku.data.dashboard.DashboardResult
import com.android.kasku.data.dashboard.Insights
import com.android.kasku.data.dashboard.MonthReceipt
import com.android.kasku.ui.auth.AuthViewModel // Assuming AuthViewModel for logout callback
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Define UI states for DashboardScreen
sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val data: DashboardData) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

sealed class AIAnalysisUiState {
    object Initial : AIAnalysisUiState()
    object Loading : AIAnalysisUiState()
    data class Success(val insights: Insights) : AIAnalysisUiState()
    data class Error(val message: String) : AIAnalysisUiState()
}

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val context: Context // Context is needed for repository initialization
) : ViewModel() {

    // Assuming AuthViewModel is instantiated somewhere accessible or passed via Hilt/etc.
    // For simplicity, we'll instantiate it here similar to ProfileViewModel.
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val authViewModel: AuthViewModel = AuthViewModel() // Re-instantiate or pass properly
    // The dashboardRepository parameter should ideally be injected, but for now we use the constructor context.

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Automatically fetch data when ViewModel is created
        fetchDashboardData()
    }

    private val _aiAnalysisUiState = MutableStateFlow<AIAnalysisUiState>(AIAnalysisUiState.Initial)
    val aiAnalysisUiState: StateFlow<AIAnalysisUiState> = _aiAnalysisUiState.asStateFlow()

    fun getAIInsights(income: Double, monthReceipts: List<MonthReceipt>) {
        _aiAnalysisUiState.value = AIAnalysisUiState.Loading
        viewModelScope.launch {
            val request = AIInsightsRequest(income = income, monthReceipts = monthReceipts)
            when (val result = dashboardRepository.getAIInsights(request)) {
                is DashboardResult.Success -> {
                    result.data.insights?.let {
                        _aiAnalysisUiState.value = AIAnalysisUiState.Success(it)
                    } ?: run {
                        _aiAnalysisUiState.value = AIAnalysisUiState.Error("No insights returned.")
                    }
                }
                is DashboardResult.Error -> {
                    _aiAnalysisUiState.value = AIAnalysisUiState.Error(result.message)
                }
            }
        }
    }

    fun fetchDashboardData() {
        _uiState.value = DashboardUiState.Loading
        viewModelScope.launch {
            when (val result = dashboardRepository.getDashboardData(context)) {
                is DashboardResult.Success -> {
                    _uiState.value = DashboardUiState.Success(result.data)
                }
                is DashboardResult.Error -> {
                    _uiState.value = DashboardUiState.Error(result.message)
                }
            }
        }
    }


}