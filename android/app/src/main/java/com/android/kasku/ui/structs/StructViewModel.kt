package com.android.kasku.ui.structs

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kasku.data.struct.StructItem
import com.android.kasku.data.struct.StructRepository
import com.android.kasku.data.struct.StructResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StructViewModel(
    private val structRepository: StructRepository,
    private val context: Context
) : ViewModel() {

    private val _structs = MutableStateFlow<List<StructItem>>(emptyList())
    val structs: StateFlow<List<StructItem>> get() = _structs

    private val _structItem = MutableStateFlow<StructItem?>(null)
    val structItem: StateFlow<StructItem?> = _structItem

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _updateSuccess = MutableStateFlow<Boolean>(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    private val _uiState = MutableStateFlow<StructUiState>(StructUiState.Loading)
    val uiState: StateFlow<StructUiState> = _uiState.asStateFlow()



    fun loadStructs(context: Context) {
        _uiState.value = StructUiState.Loading
        viewModelScope.launch {
            when (val result = structRepository.getStructList(context)) {
                is StructResult.Success -> {
                    _uiState.value = StructUiState.Success(result.data)
                }
                is StructResult.Error -> {
                    _uiState.value = StructUiState.Error(result.message)
                }
            }
        }
    }

    fun getStructById(id: String) {
        _loading.value = true
        viewModelScope.launch {
            when (val result = structRepository.getStructById(context, id)) {
                is StructResult.Success -> {
                    _structItem.value = result.data
                    _error.value = null
                }
                is StructResult.Error -> {
                    _error.value = result.message
                }
            }
            _loading.value = false
        }
    }

    fun updateStruct(
        id: String,
        categorySpending: String?,
        merchantName: String?,
        transactionDate: String,
        finalTotal: Double
    ) {
        _loading.value = true
        viewModelScope.launch {
            val result = structRepository.updateStruct(
                context = context,
                id = id,
                categorySpending = categorySpending,
                merchantName = merchantName,
                transactionDate = transactionDate,
                finalTotal = finalTotal
            )
            if (result is StructResult.Success) {
                _updateSuccess.value = true
                _error.value = null
            } else if (result is StructResult.Error) {
                _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }

}

sealed class StructUiState {
    object Loading : StructUiState()
    data class Success(val data: List<StructItem>) : StructUiState()
    data class Error(val message: String) : StructUiState()
}
