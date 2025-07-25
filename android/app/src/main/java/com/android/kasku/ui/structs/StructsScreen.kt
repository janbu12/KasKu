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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Commute
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
import com.android.kasku.data.struct.Item // Ensure Item is imported if StructItem uses it

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

    // Determine icon background color based on the category (similar to the image)
    val iconBackgroundColor: Color = remember(struct.category_spending) {
        when (struct.category_spending?.lowercase()) {
            "food" -> Color(0xFF4CAF50) // Green from image
            "transportation" -> Color(0xFF2196F3) // Blue (using a common blue)
            "clothing" -> Color(0xFFFF9800) // Orange (using a common orange)
            "sport" -> Color(0xFF9C27B0) // Purple
            "fuel" -> Color(0xFF607D8B) // Blue Grey
            "internet" -> Color(0xFF795548) // Brown
            "entertainment" -> Color(0xFFE91E63) // Pink
            "health" -> Color(0xFF00BCD4) // Cyan
            "home" -> Color(0xFFCDDC39) // Lime
            "cigarettes" -> Color(0xFFF44336) // Red
            else -> Color.LightGray // Default gray
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // Set card background to white
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Distribute content
        ) {
            // Left side: Icon and Category/Merchant/Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp), // Rounded square background for icon
                    color = iconBackgroundColor,
                    modifier = Modifier.size(48.dp) // Size of the icon container
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            categoryIcon,
                            contentDescription = null,
                            tint = Color.White, // Icon color inside the colored square
                            modifier = Modifier.size(28.dp) // Size of the icon itself
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = struct.category_spending?.ifBlank { "-" }?.replaceFirstChar { it.uppercase() } ?: "-",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground // Consistent text color
                    )
                    Text(
                        text = struct.merchant_name ?: "-",
                        fontSize = 14.sp,
                        color =  Color.Gray
                    )
                    // You might want to format the date string
                    Text(
                        text = struct.transaction_date, // Format date as desired (e.g., "24/8/2025")
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Right side: Total and Edit Button
            Column(horizontalAlignment = Alignment.End) {
                // Ensure total is displayed as negative for expenses, assuming all structs are expenses
                val formattedTotal = if (struct.final_total < 0) {
                    "Rp${struct.final_total.toInt()}" // Already negative
                } else {
                    "-Rp${struct.final_total.toInt()}" // Add minus for positive expenses
                }
                Text(
                    text = formattedTotal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error // For expenses, typically red
                )

                Spacer(Modifier.height(8.dp)) // Space between total and edit button

                Button(
                    onClick = onEdit,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Green color for the button
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), // Smaller padding for a compact button
                    modifier = Modifier.height(32.dp) // Smaller height
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp) // Smaller text
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StructScreenPreview() {
    val dummyStructs = listOf(
        StructItem(
            id = "1",
            merchant_name = "Indomaret",
            transaction_date = "2025-07-23", // Using YYYY-MM-DD for consistency
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
                StructCard(struct = it, onEdit = {})
            }
        }
    }
}