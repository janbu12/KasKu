package com.android.kasku.ui.structs

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Delete // Import Delete icon
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit // Pencil icon
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.android.kasku.data.struct.StructItem
import com.android.kasku.data.struct.StructRepositoryImpl
import com.android.kasku.data.struct.Item

@Composable
fun StructScreen(
    navToEdit: (String) -> Unit,
) {
    val context = LocalContext.current
    val viewModel = remember { StructViewModel(StructRepositoryImpl { /* onTokenExpired, perlu context */ }, context) }
    val uiState by viewModel.uiState.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState() // NEW
    val deleteError by viewModel.deleteError.collectAsState()     // NEW

    LaunchedEffect(Unit) {
        viewModel.loadStructs(context)
    }

    // NEW: Handle delete success
    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            Toast.makeText(context, "Struk berhasil dihapus.", Toast.LENGTH_SHORT).show()
            viewModel.resetDeleteStructState()
        }
    }

    // NEW: Handle delete error
    LaunchedEffect(deleteError) {
        deleteError?.let { message ->
            Toast.makeText(context, "Gagal menghapus struk: $message", Toast.LENGTH_LONG).show()
            viewModel.resetDeleteStructState()
        }
    }


    Surface (
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(structs) { struct ->
                                StructCard(
                                    struct = struct,
                                    onEdit = { navToEdit(struct.id) },
                                    onDelete = { viewModel.deleteStruct(struct.id) } // Pass delete action
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun StructCard(struct: StructItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    val categoryIcon: ImageVector = remember(struct.category_spending) {
        when (struct.category_spending?.lowercase()) {
            "food" -> Icons.Default.Fastfood
            "sport" -> Icons.Default.FitnessCenter
            "transportation" -> Icons.Default.Commute
            "clothing" -> Icons.Default.Checkroom
            "fuel" -> Icons.Default.LocalGasStation
            "internet" -> Icons.Default.Wifi
            "entertainment" -> Icons.Default.SportsEsports
            "health" -> Icons.Default.LocalHospital
            "home" -> Icons.Default.Home
            "cigarettes" -> Icons.Default.SmokingRooms
            else -> Icons.AutoMirrored.Filled.ReceiptLong
        }
    }

    val iconBackgroundColor: Color = remember(struct.category_spending) {
        when (struct.category_spending?.lowercase()) {
            "food" -> Color(0xFF4CAF50)
            "transportation" -> Color(0xFF2196F3)
            "clothing" -> Color(0xFFFF9800)
            "sport" -> Color(0xFF9C27B0)
            "fuel" -> Color(0xFF607D8B)
            "internet" -> Color(0xFF795548)
            "entertainment" -> Color(0xFFE91E63)
            "health" -> Color(0xFF00BCD4)
            "home" -> Color(0xFFCDDC39)
            "cigarettes" -> Color(0xFFF44336)
            else -> Color.LightGray
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Icon and Category/Merchant/Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconBackgroundColor,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            categoryIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = struct.category_spending?.ifBlank { "-" }?.replaceFirstChar { it.uppercase() } ?: "-",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = struct.merchant_name ?: "-",
                        fontSize = 14.sp,
                        color =  Color.Gray
                    )
                    Text(
                        text = struct.transaction_date,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Right side: Action Buttons and Total
            Column(horizontalAlignment = Alignment.End) {

                // Total Expense
                val formattedTotal = if (struct.final_total < 0) {
                    "Rp${struct.final_total.toInt()}"
                } else {
                    "-Rp${struct.final_total.toInt()}"
                }
                Text(
                    text = formattedTotal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error // Tetap merah untuk pengeluaran
                )

                Spacer(Modifier.height(8.dp)) // Spasi antara tombol dan total pengeluaran

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // NEW: Edit Button (Icon Only)
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp) // Ukuran ikon tombol
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary // Warna ikon edit
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp)) // Spasi antara tombol Edit dan Delete

                    // Delete Button (Icon Only)
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(24.dp) // Ukuran ikon tombol
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Struk") },
            text = { Text("Apakah Anda yakin ingin menghapus struk ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            },
            containerColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Preview(showBackground = true)
@Composable
fun StructScreenPreview() {
    val dummyStructs = listOf(
        StructItem(
            id = "1",
            merchant_name = "Indomaret",
            transaction_date = "2025-07-23",
            transaction_time = "10:30",
            items = listOf(
                Item("Roti Tawar", 1, 15000.0, 15000.0)
            ),
            subtotal = 15000.0,
            discount_amount = 0.0,
            additional_charges = 0.0,
            tax_amount = 0.0,
            final_total = 15000.0,
            tender_type = "Tunai",
            amount_paid = 20000.0,
            change_given = 5000.0,
            category_spending = "Food"
        ),
        StructItem(
            id = "2",
            merchant_name = "GoRide",
            transaction_date = "2025-07-22",
            transaction_time = "14:15",
            items = listOf(
                Item("Perjalanan", 1, 22000.0, 22000.0)
            ),
            subtotal = 22000.0,
            discount_amount = 2000.0,
            additional_charges = 0.0,
            tax_amount = 0.0,
            final_total = 20000.0,
            tender_type = "OVO",
            amount_paid = 20000.0,
            change_given = 0.0,
            category_spending = "Transportation"
        ),
        StructItem(
            id = "3",
            merchant_name = "Zara",
            transaction_date = "2025-07-21",
            transaction_time = "18:00",
            items = listOf(
                Item("Baju Kemeja", 1, 300000.0, 300000.0)
            ),
            subtotal = 300000.0,
            discount_amount = 50000.0,
            additional_charges = 0.0,
            tax_amount = 0.0,
            final_total = 250000.0,
            tender_type = "Debit",
            amount_paid = 250000.0,
            change_given = 0.0,
            category_spending = "Clothing"
        )
    )
    KasKuTheme {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dummyStructs) {
                StructCard(struct = it, onEdit = {}, onDelete = {})
            }
        }
    }
}