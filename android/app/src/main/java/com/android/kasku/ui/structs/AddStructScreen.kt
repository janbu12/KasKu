package com.android.kasku.ui.structs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.android.kasku.data.struct.Item
import com.android.kasku.data.struct.StructRepositoryImpl
import com.android.kasku.ui.common.CameraCaptureScreen
import com.android.kasku.ui.common.ImagePreviewScreen
import com.android.kasku.ui.common.getOutputDirectory
import java.io.File
import java.util.Calendar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlin.math.max
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.android.kasku.navigation.AppRoutes
import com.android.kasku.navigation.BottomNavItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStructScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val structViewModel: StructViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return StructViewModel(StructRepositoryImpl { /* onTokenExpired, need context */ }, context) as T
            }
        }
    )

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showCamera by remember { mutableStateOf(true) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val outputDirectory: File = remember { getOutputDirectory(context) }

    val uploadedStructuredData by structViewModel.uploadedStructuredData.collectAsState()
    val uploadingImage by structViewModel.uploadingImage.collectAsState()
    val uploadError by structViewModel.uploadError.collectAsState()

    val createSuccess by structViewModel.createSuccess.collectAsState() // NEW
    val createError by structViewModel.createError.collectAsState()     // NEW
    val isSaving by structViewModel.loading.collectAsState()            // NEW: Using general loading for saving


    // Form states
    var category by remember { mutableStateOf(TextFieldValue("")) }
    var merchant by remember { mutableStateOf(TextFieldValue("")) }
    var date by remember { mutableStateOf(TextFieldValue("")) }
    var time by remember { mutableStateOf(TextFieldValue("")) }

    var discount by remember { mutableStateOf(TextFieldValue("0.0")) }
    var charges by remember { mutableStateOf(TextFieldValue("0.0")) }
    var tax by remember { mutableStateOf(TextFieldValue("0.0")) }

    var tenderType by remember { mutableStateOf(TextFieldValue("")) }
    var amountPaid by remember { mutableStateOf(TextFieldValue("0.0")) }
    var changeGiven by remember { mutableStateOf(TextFieldValue("0.0")) }

    val items = remember { mutableStateListOf<Item>() }

    val categories = listOf(
        "Food", "Sport", "Transportation", "Clothing", "Fuel", "Internet",
        "Entertainment", "Health", "Home", "Cigarettes"
    )
    val tenderTypes = listOf("TUNAI", "NON TUNAI")

    var categoryExpanded by remember { mutableStateOf(false) }
    var tenderTypeExpanded by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            selectedYear = year
            selectedMonth = month
            selectedDay = dayOfMonth
            date = TextFieldValue(String.format("%d-%02d-%02d", year, month + 1, dayOfMonth))
        }, selectedYear, selectedMonth, selectedDay
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay: Int, minute: Int ->
            selectedHour = hourOfDay
            selectedMinute = minute
            time = TextFieldValue(String.format("%02d:%02d", hourOfDay, minute))
        }, selectedHour, selectedMinute, true
    )

    val calculatedSubtotal: Double by remember(items.toList()) {
        derivedStateOf {
            items.sumOf { it.totalPriceItem }
        }
    }

    val calculatedFinalTotal: Double by remember(
        calculatedSubtotal,
        discount.text,
        charges.text,
        tax.text
    ) {
        derivedStateOf {
            val disc = discount.text.toDoubleOrNull() ?: 0.0
            val addCharges = charges.text.toDoubleOrNull() ?: 0.0
            val taxAmount = tax.text.toDoubleOrNull() ?: 0.0
            max(0.0, calculatedSubtotal - disc + addCharges + taxAmount)
        }
    }

    val coroutineScope = rememberCoroutineScope() // To launch suspend functions


    // Launcher untuk memilih gambar dari galeri
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            capturedImageUri = uri
            showCamera = false // Tampilkan pratinjau
        } else {
            Toast.makeText(context, "Tidak ada gambar dipilih dari galeri.", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Effect to handle uploaded structured data
    LaunchedEffect(uploadedStructuredData) {
        uploadedStructuredData?.let { data ->
            // Pre-fill form fields with data from the API response
            merchant = TextFieldValue(data.structuredData.merchantName.orEmpty())
            date = TextFieldValue(data.structuredData.transactionDate)
            time = TextFieldValue(data.structuredData.transactionTime)
            discount = TextFieldValue(data.structuredData.discountAmount?.toString() ?: "0.0")
            charges = TextFieldValue(data.structuredData.additionalCharges?.toString() ?: "0.0")
            tax = TextFieldValue(data.structuredData.taxAmount?.toString() ?: "0.0")
            tenderType = TextFieldValue(data.structuredData.tenderType.orEmpty())
            amountPaid = TextFieldValue(data.structuredData.amountPaid?.toString() ?: "0.0")
            changeGiven = TextFieldValue(data.structuredData.changeGiven?.toString() ?: "0.0")

            items.clear()
            items.addAll(data.structuredData.items.map { itemUpload ->
                Item(
                    itemName = itemUpload.itemName,
                    quantity = itemUpload.quantity ?: 0,
                    unitPrice = itemUpload.unitPrice ?: 0.0,
                    totalPriceItem = itemUpload.totalPriceItem ?: (itemUpload.quantity ?: 0) * (itemUpload.unitPrice ?: 0.0)
                )
            })

            // Update date and time pickers
            val dateParts = data.structuredData.transactionDate.split("-")
            if (dateParts.size == 3) {
                selectedYear = dateParts[0].toInt()
                selectedMonth = dateParts[1].toInt() - 1
                selectedDay = dateParts[2].toInt()
            }
            val timeParts = data.structuredData.transactionTime.split(":")
            if (timeParts.size == 2) {
                selectedHour = timeParts[0].toInt()
                selectedMinute = timeParts[1].toInt()
            }
            showCamera = false // Ensure we are on the form screen
        }
    }

    // Effect to handle upload errors
    LaunchedEffect(uploadError) {
        uploadError?.let { message ->
            Toast.makeText(context, "Error uploading image: $message", Toast.LENGTH_LONG).show()
            structViewModel.resetUploadState() // Reset state to allow re-upload or retake
            capturedImageUri = null
            showCamera = true // Go back to camera
        }
    }

    // NEW: Effect to handle struct creation success
    LaunchedEffect(createSuccess) {
        if (createSuccess) {
            Toast.makeText(context, "Struct berhasil disimpan!", Toast.LENGTH_SHORT).show()
            structViewModel.resetCreateStructState()
            structViewModel.resetUploadState()
            capturedImageUri = null
            showCamera = true
            navController.navigate(AppRoutes.appGraphRouteWithTab(BottomNavItem.Structs.route)) {
                // Pop up to the root of the MainScreen's host to ensure clean state
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true // Save state of the bottom nav if it exists
                }
                launchSingleTop = true // Prevents multiple instances
                restoreState = true // Restores previous state of MainScreen
            }
        }
    }

    // NEW: Effect to handle struct creation error
    LaunchedEffect(createError) {
        createError?.let { message ->
            Toast.makeText(context, "Gagal menyimpan struct: $message", Toast.LENGTH_LONG).show()
            structViewModel.resetCreateStructState()
        }
    }


    // Handle system back button press
    BackHandler(enabled = true) {
        when {
            uploadedStructuredData != null -> { // If on the form screen
                structViewModel.resetUploadState()
                // Keep capturedImageUri as is, so it goes back to ImagePreview
                showCamera = false
            }
            !showCamera && capturedImageUri != null -> { // If on ImagePreviewScreen
                capturedImageUri = null
                showCamera = true // Go back to Camera
            }
            else -> { // If on CameraCaptureScreen
                navController.popBackStack() // Exit AddStructScreen entirely
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Struct") },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            uploadedStructuredData != null -> { // If on the form screen
                                structViewModel.resetUploadState()
                                showCamera = false // Go back to ImagePreview
                            }
                            !showCamera && capturedImageUri != null -> { // If on ImagePreviewScreen
                                capturedImageUri = null
                                showCamera = true // Go back to Camera
                            }
                            else -> { // If on CameraCaptureScreen
                                navController.popBackStack() // Exit AddStructScreen entirely
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uploadingImage || isSaving) { // Combine loading states
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (uploadedStructuredData != null) {
                // Display the form with pre-filled data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Spending Dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { /* Do nothing, value is set by dropdown */ },
                            readOnly = true,
                            label = { Text("Category Spending") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                disabledContainerColor = MaterialTheme.colorScheme.background,
                                errorContainerColor = MaterialTheme.colorScheme.background
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.background,)
                        ) {
                            categories.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        category = TextFieldValue(selectionOption)
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = merchant,
                        onValueChange = { merchant = it },
                        label = { Text("Merchant Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Transaction Date Picker
                    OutlinedTextField(
                        value = date,
                        onValueChange = { /* Read-only, set by picker */ },
                        readOnly = true,
                        label = { Text("Transaction Date (YYYY-MM-DD)") },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Transaction Time Picker
                    OutlinedTextField(
                        value = time,
                        onValueChange = { /* Read-only, set by picker */ },
                        readOnly = true,
                        label = { Text("Transaction Time (HH:MM)") },
                        trailingIcon = {
                            IconButton(onClick = { timePickerDialog.show() }) {
                                Icon(Icons.Default.Schedule, contentDescription = "Select Time")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Items Section
                    Text("Items:", style = MaterialTheme.typography.titleMedium)
                    items.forEachIndexed { index, item ->
                        ItemEditRow(
                            item = item,
                            onItemChange = { updatedItem: Item -> // Explicitly set type to Item
                                items[index] = updatedItem
                                val newQuantity = updatedItem.quantity
                                val newUnitPrice = updatedItem.unitPrice
                                val newTotalPriceItem = newQuantity * newUnitPrice
                                items[index] = updatedItem.copy(totalPriceItem = newTotalPriceItem)
                            },
                            onRemove = {
                                items.removeAt(index)
                            }
                        )
                    }

                    Button(
                        onClick = {
                            items.add(Item(itemName = "", quantity = 0, unitPrice = 0.0, totalPriceItem = 0.0))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Item")
                    }

                    // Subtotal Display (Read-only)
                    OutlinedTextField(
                        value = calculatedSubtotal.toString(),
                        onValueChange = { /* Read-only */ },
                        readOnly = true,
                        label = { Text("Subtotal") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = discount,
                        onValueChange = { discount = it },
                        label = { Text("Discount Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = charges,
                        onValueChange = { charges = it },
                        label = { Text("Additional Charges") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tax,
                        onValueChange = { tax = it },
                        label = { Text("Tax Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Final Total Display (Read-only)
                    OutlinedTextField(
                        value = calculatedFinalTotal.toString(),
                        onValueChange = { /* Read-only */ },
                        readOnly = true,
                        label = { Text("Final Total") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tender Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = tenderTypeExpanded,
                        onExpandedChange = { tenderTypeExpanded = !tenderTypeExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = tenderType,
                            onValueChange = { /* Do nothing, value is set by dropdown */ },
                            readOnly = true,
                            label = { Text("Tender Type") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = tenderTypeExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                disabledContainerColor = MaterialTheme.colorScheme.background,
                                errorContainerColor = MaterialTheme.colorScheme.background
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = tenderTypeExpanded,
                            onDismissRequest = { tenderTypeExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.background,)
                        ) {
                            tenderTypes.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        tenderType = TextFieldValue(selectionOption)
                                        tenderTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountPaid,
                        onValueChange = { amountPaid = it },
                        label = { Text("Amount Paid") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = changeGiven,
                        onValueChange = { changeGiven = it },
                        label = { Text("Change Given") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            // Validate required fields before saving
                            if (merchant.text.isBlank() || date.text.isBlank() || time.text.isBlank() || items.isEmpty()) {
                                Toast.makeText(context, "Merchant, Date, Time, and Items cannot be empty.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Trigger struct creation
                            coroutineScope.launch {
                                structViewModel.createStruct(
                                    categorySpending = category.text.ifBlank { null },
                                    merchantName = merchant.text,
                                    transactionDate = date.text,
                                    transactionTime = time.text,
                                    items = items.toList(),
                                    subtotal = calculatedSubtotal,
                                    discountAmount = discount.text.toDoubleOrNull(),
                                    additionalCharges = charges.text.toDoubleOrNull(),
                                    taxAmount = tax.text.toDoubleOrNull(),
                                    finalTotal = calculatedFinalTotal,
                                    tenderType = tenderType.text.ifBlank { null },
                                    amountPaid = amountPaid.text.toDoubleOrNull(),
                                    changeGiven = changeGiven.text.toDoubleOrNull()
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving // Disable button while saving
                    ) {
                        Text("Save New Struct")
                    }
                }
            } else if (showCamera) {
                CameraCaptureScreen(
                    outputDirectory = outputDirectory,
                    executor = cameraExecutor,
                    onImageCaptured = { uri ->
                        capturedImageUri = uri
                        showCamera = false
                    },
                    onError = { exception ->
                        Toast.makeText(context, "Error kamera: ${exception.message}", Toast.LENGTH_SHORT).show()
                    },
                    onPickFromGallery = {
                        pickImageLauncher.launch("image/*")
                    }
                )
            } else if (capturedImageUri != null) {
                ImagePreviewScreen(
                    imageUri = capturedImageUri!!,
                    onRetakePhoto = {
                        capturedImageUri = null
                        showCamera = true
                    },
                    onConfirmPhoto = { uri ->
                        // Lakukan kompresi sebelum mengirim ke ViewModel
                        val compressedUri = compressImage(context, uri)
                        if (compressedUri != null) {
                            structViewModel.uploadImageForStruct(compressedUri)
                        } else {
                            Toast.makeText(context, "Gagal mengompres gambar.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Loading...", fontSize = 24.sp)
                }
            }
        }
    }
}

private fun compressImage(context: Context, imageUri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (bitmap == null) {
            Log.e("CompressImage", "Bitmap decoding failed for URI: $imageUri")
            return null
        }

        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val maxDimension = 1024

        var scaledBitmap = bitmap

        if (originalWidth > maxDimension || originalHeight > maxDimension) {
            val ratio = Math.min(maxDimension.toFloat() / originalWidth, maxDimension.toFloat() / originalHeight)
            val newWidth = (originalWidth * ratio).toInt()
            val newHeight = (originalHeight * ratio).toInt()
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            Log.d("CompressImage", "Resized from ${originalWidth}x${originalHeight} to ${newWidth}x${newHeight}")
        } else {
            Log.d("CompressImage", "No resizing needed, original dimensions: ${originalWidth}x${originalHeight}")
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)

        val compressedFile = File(context.cacheDir, "compressed_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(compressedFile).use { fos ->
            fos.write(outputStream.toByteArray())
        }

        Log.d("CompressImage", "Original file size: ${String.format("%.2f", (context.contentResolver.openAssetFileDescriptor(imageUri, "r")?.length ?: 0) / 1024.0 / 1024.0)} MB")
        Log.d("CompressImage", "Compressed file size: ${String.format("%.2f", compressedFile.length() / 1024.0 / 1024.0)} MB")

        if (bitmap != scaledBitmap) {
            bitmap.recycle()
        }
        scaledBitmap.recycle()

        return Uri.fromFile(compressedFile)
    } catch (e: Exception) {
        Log.e("CompressImage", "Error compressing image: ${e.message}", e)
        e.printStackTrace()
        null
    }
}