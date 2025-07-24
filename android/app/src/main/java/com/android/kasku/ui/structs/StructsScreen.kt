package com.android.kasku.ui.structs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.kasku.ui.theme.KasKuTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.android.kasku.data.struct.StructItem
import com.android.kasku.data.struct.StructRepositoryImpl

@Composable
fun StructScreen(
    navToEdit: (String) -> Unit,
) {
    val context = LocalContext.current
    val viewModel = remember { StructViewModel(StructRepositoryImpl {}, context) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStructs(context)
    }

    Surface (
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ){
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState) {
                is StructUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is StructUiState.Error -> {
                    val message = (uiState as StructUiState.Error).message
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is StructUiState.Success -> {
                    val structs = (uiState as StructUiState.Success).data
                    if (structs.isEmpty()) {
                        Text(
                            text = "Tidak ada struct tersedia.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(structs) { struct ->
                                StructCard(struct = struct, onEdit = { navToEdit(struct.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun StructCard(struct: StructItem, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF4CAF50))
                Spacer(Modifier.width(8.dp))
                struct.category_spending?.ifBlank { "-" }?.replaceFirstChar { it.uppercase() }?.let {
                    Text(
                        text = it,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text("Merchant: ${struct.merchant_name ?: { "-" }}", fontSize = 14.sp, color = Color.Gray)
            Text("Tanggal: ${struct.transaction_date}", fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Total: Rp${struct.final_total.toInt()}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF4CAF50)
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onEdit,
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StructScreenPreview() {
    val dummyStructs = listOf(
        StructItem("1", "Makanan", "Indomaret", "2025-07-23", 15000.0),
        StructItem("2", "Transportasi", "Grab", "2025-07-22", 22000.0)
    )
    KasKuTheme {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dummyStructs) {
                StructCard(struct = it, onEdit = {})
            }
        }
    }
}
