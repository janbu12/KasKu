// ui/structs/StructViewModel.kt
package com.android.kasku.ui.structs

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kasku.data.struct.StructItem
import com.android.kasku.data.struct.StructRepository
import com.android.kasku.data.struct.StructResult
import com.android.kasku.data.struct.Item
import com.android.kasku.data.struct.UploadStructResponse
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

    // New StateFlows for image upload
    private val _uploadedStructuredData = MutableStateFlow<UploadStructResponse?>(null)
    val uploadedStructuredData: StateFlow<UploadStructResponse?> = _uploadedStructuredData.asStateFlow()

    private val _uploadingImage = MutableStateFlow(false)
    val uploadingImage: StateFlow<Boolean> = _uploadingImage.asStateFlow()

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError.asStateFlow()

    private val _createSuccess = MutableStateFlow<Boolean>(false)
    val createSuccess: StateFlow<Boolean> = _createSuccess.asStateFlow()

    private val _createError = MutableStateFlow<String?>(null)
    val createError: StateFlow<String?> = _createError.asStateFlow()

    private val _deleteSuccess = MutableStateFlow<Boolean>(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()

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
        transactionTime: String,
        items: List<Item>,
        subtotal: Double,
        discountAmount: Double?,
        additionalCharges: Double?,
        taxAmount: Double?,
        finalTotal: Double,
        tenderType: String?,
        amountPaid: Double?,
        changeGiven: Double?
    ) {
        _loading.value = true
        viewModelScope.launch {
            val result = structRepository.updateStruct(
                context = context,
                id = id,
                categorySpending = categorySpending,
                merchantName = merchantName,
                transactionDate = transactionDate,
                transactionTime = transactionTime,
                items = items,
                subtotal = subtotal,
                discountAmount = discountAmount,
                additionalCharges = additionalCharges,
                taxAmount = taxAmount,
                finalTotal = finalTotal,
                tenderType = tenderType,
                amountPaid = amountPaid,
                changeGiven = changeGiven
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

    fun uploadImageForStruct(imageUri: Uri) {
        _uploadingImage.value = true
        _uploadError.value = null
        _uploadedStructuredData.value = null // Clear previous data
        viewModelScope.launch {
            when (val result = structRepository.uploadStructImage(context, imageUri)) {
                is StructResult.Success -> {
                    _uploadedStructuredData.value = result.data
                }
                is StructResult.Error -> {
                    _uploadError.value = result.message
                }
            }
            _uploadingImage.value = false
        }
    }

    fun createStruct(
        categorySpending: String?,
        merchantName: String,
        transactionDate: String,
        transactionTime: String,
        items: List<Item>,
        subtotal: Double,
        discountAmount: Double?,
        additionalCharges: Double?,
        taxAmount: Double?,
        finalTotal: Double,
        tenderType: String?,
        amountPaid: Double?,
        changeGiven: Double?
    ) {
        _loading.value = true // Use general loading for form submission
        _createSuccess.value = false
        _createError.value = null
        viewModelScope.launch {
            val result = structRepository.createStruct(
                context = context,
                categorySpending = categorySpending,
                merchantName = merchantName,
                transactionDate = transactionDate,
                transactionTime = transactionTime,
                items = items,
                subtotal = subtotal,
                discountAmount = discountAmount,
                additionalCharges = additionalCharges,
                taxAmount = taxAmount,
                finalTotal = finalTotal,
                tenderType = tenderType,
                amountPaid = amountPaid,
                changeGiven = changeGiven
            )
            if (result is StructResult.Success) {
                _createSuccess.value = true
            } else if (result is StructResult.Error) {
                _createError.value = result.message
            }
            _loading.value = false
        }
    }

    fun deleteStruct(id: String) {
        _loading.value = true // Use general loading state
        _deleteSuccess.value = false
        _deleteError.value = null
        viewModelScope.launch {
            when (val result = structRepository.deleteStruct(context, id)) {
                is StructResult.Success -> {
                    _deleteSuccess.value = true
                    // After successful deletion, reload the list of structs
                    loadStructs(context)
                }
                is StructResult.Error -> {
                    _deleteError.value = result.message
                }
            }
            _loading.value = false
        }
    }

    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }

    fun resetUploadState() {
        _uploadedStructuredData.value = null
        _uploadError.value = null
        _uploadingImage.value = false
    }

    fun resetCreateStructState() {
        _createSuccess.value = false
        _createError.value = null
        _loading.value = false // Ensure loading is reset
    }

    fun resetDeleteStructState() {
        _deleteSuccess.value = false
        _deleteError.value = null
    }

}

sealed class StructUiState {
    object Loading : StructUiState()
    data class Success(val data: List<StructItem>) : StructUiState()
    data class Error(val message: String) : StructUiState()
}