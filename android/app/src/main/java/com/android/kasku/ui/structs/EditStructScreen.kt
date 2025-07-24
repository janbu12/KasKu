package com.android.kasku.ui.structs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.kasku.data.struct.StructRepositoryImpl
import kotlinx.coroutines.launch

@Composable
fun EditStructScreen(
    structId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel = remember {
        StructViewModel(StructRepositoryImpl { /* onTokenExpired */ }, context)
    }

    val struct by viewModel.structItem.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()

    var category by remember { mutableStateOf(TextFieldValue("")) }
    var merchant by remember { mutableStateOf(TextFieldValue("")) }
    var date by remember { mutableStateOf(TextFieldValue("")) }
    var total by remember { mutableStateOf(TextFieldValue("")) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(structId) {
        viewModel.getStructById(structId)
    }

    LaunchedEffect(struct) {
        struct?.let {
            category = TextFieldValue(it.category_spending.orEmpty())
            merchant = TextFieldValue(it.merchant_name.orEmpty())
            date = TextFieldValue(it.transaction_date)
            total = TextFieldValue(it.final_total.toString())
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Struct updated successfully", Toast.LENGTH_SHORT).show()
            viewModel.resetUpdateSuccess()
            navController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (loading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Edit Struct", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category Spending") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Transaction Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = total,
                    onValueChange = { total = it },
                    label = { Text("Final Total") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.updateStruct(
                                id = structId,
                                categorySpending = category.text,
                                merchantName = merchant.text,
                                transactionDate = date.text,
                                finalTotal = total.text.toDoubleOrNull() ?: 0.0
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
