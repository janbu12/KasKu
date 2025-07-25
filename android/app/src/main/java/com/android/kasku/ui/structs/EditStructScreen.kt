// ui/structs/EditStructScreen.kt
package com.android.kasku.ui.structs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.kasku.data.struct.StructRepositoryImpl
import com.android.kasku.data.struct.Item
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStructScreen(
    structId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel = remember {
        // StructRepositoryImpl juga perlu penyesuaian jika onTokenExpired-nya butuh Context
        StructViewModel(StructRepositoryImpl { /* onTokenExpired, perlu context */ }, context)
    }

    val struct by viewModel.structItem.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()

    val categories = listOf(
        "Food", "Sport", "Transportation", "Clothing", "Fuel", "Internet",
        "Entertainment", "Health", "Home", "Cigarettes"
    )

    val tenderTypes = listOf("TUNAI", "NON TUNAI")

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

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(structId) {
        viewModel.getStructById(structId)
    }

    LaunchedEffect(struct) {
        struct?.let {
            category = TextFieldValue(it.category_spending.orEmpty())
            merchant = TextFieldValue(it.merchant_name.orEmpty())
            date = TextFieldValue(it.transaction_date)
            time = TextFieldValue(it.transaction_time)
            discount = TextFieldValue(it.discount_amount?.toString() ?: "0.0")
            charges = TextFieldValue(it.additional_charges?.toString() ?: "0.0")
            tax = TextFieldValue(it.tax_amount?.toString() ?: "0.0")
            tenderType = TextFieldValue(it.tender_type.orEmpty())
            amountPaid = TextFieldValue(it.amount_paid?.toString() ?: "0.0")
            changeGiven = TextFieldValue(it.change_given?.toString() ?: "0.0")

            items.clear()
            items.addAll(it.items)

            val dateParts = it.transaction_date.split("-")
            if (dateParts.size == 3) {
                selectedYear = dateParts[0].toInt()
                selectedMonth = dateParts[1].toInt() - 1
                selectedDay = dateParts[2].toInt()
            }
            val timeParts = it.transaction_time.split(":")
            if (timeParts.size == 2) {
                selectedHour = timeParts[0].toInt()
                selectedMinute = timeParts[1].toInt()
            }
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Struct updated successfully", Toast.LENGTH_SHORT).show()
            viewModel.resetUpdateSuccess()
            navController.popBackStack()
        }
    }

    // --- MULAI PERUBAHAN DI SINI: Kembalikan Scaffold dan TopAppBar ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Struct") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background // Latar belakang putih
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp), // Padding internal untuk konten
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
                                errorContainerColor =  MaterialTheme.colorScheme.background
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.background)
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
                            onItemChange = { updatedItem ->
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
                            coroutineScope.launch {
                                viewModel.updateStruct(
                                    id = structId,
                                    categorySpending = category.text.ifBlank { null },
                                    merchantName = merchant.text.ifBlank { null },
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save")
                    }

                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
    // --- AKHIR PERUBAHAN DI SINI ---
}

@Composable
fun ItemEditRow(
    item: Item,
    onItemChange: (Item) -> Unit,
    onRemove: () -> Unit
) {
    var itemName by remember(item.itemName) { mutableStateOf(TextFieldValue(item.itemName)) }
    var quantity by remember(item.quantity) { mutableStateOf(TextFieldValue(item.quantity.toString())) }
    var unitPrice by remember(item.unitPrice) { mutableStateOf(TextFieldValue(item.unitPrice.toString())) }
    var currentTotalPriceItem by remember(item.totalPriceItem) { mutableStateOf(item.totalPriceItem.toString()) }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Item Detail", style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove Item", tint = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = itemName,
            onValueChange = {
                itemName = it
                onItemChange(item.copy(itemName = it.text))
            },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = quantity,
            onValueChange = {
                quantity = it
                val newQuantity = it.text.toIntOrNull() ?: 0
                val newUnitPrice = unitPrice.text.toDoubleOrNull() ?: 0.0
                val newTotalPrice = newQuantity * newUnitPrice
                onItemChange(item.copy(quantity = newQuantity, totalPriceItem = newTotalPrice))
            },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = unitPrice,
            onValueChange = {
                unitPrice = it
                val newQuantity = quantity.text.toIntOrNull() ?: 0
                val newUnitPrice = it.text.toDoubleOrNull() ?: 0.0
                val newTotalPrice = newQuantity * newUnitPrice
                onItemChange(item.copy(unitPrice = newUnitPrice, totalPriceItem = newTotalPrice))
            },
            label = { Text("Unit Price") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = currentTotalPriceItem,
            onValueChange = {
                // This field is now read-only, changes come from quantity/unitPrice
            },
            readOnly = true,
            label = { Text("Total Price Item (Calculated)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}