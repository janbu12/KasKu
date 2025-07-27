package com.android.kasku.ui.dashboard

import android.content.Context
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.kasku.data.dashboard.DashboardData
import com.android.kasku.data.dashboard.DashboardRepositoryImpl
import com.android.kasku.ui.auth.AuthViewModel
import com.android.kasku.ui.theme.KasKuTheme
import kotlin.math.max

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.kasku.data.dashboard.Insights
import com.android.kasku.data.dashboard.LineChartItem
import com.android.kasku.data.dashboard.MonthReceipt
import com.android.kasku.data.dashboard.OverspentCategory
import com.android.kasku.data.dashboard.PieChartItem
import com.android.kasku.data.struct.StructItem
import com.android.kasku.ui.structs.StructCard
import com.android.kasku.ui.theme.Orange40
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.Pie
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.sample.compose.rememberMarker
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(
                    DashboardRepositoryImpl { AuthViewModel().logout() },
                    context
                ) as T
            }
        }
    )
    val uiState by dashboardViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        dashboardViewModel.fetchDashboardData()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState) {
                is DashboardUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is DashboardUiState.Error -> Text(
                    text = "Error: ${(uiState as DashboardUiState.Error).message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                is DashboardUiState.Success -> DashboardContent((uiState as DashboardUiState.Success).data, dashboardViewModel)
            }
        }
    }
}

@Composable
fun DashboardContent(data: DashboardData, dashboardViewModel: DashboardViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Text("Summary", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
            SummaryCard(data)
        }

        item {
            Spacer(modifier = Modifier.size(8.dp))
        }

        item {
            Text("AI Analize", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
            AIAnalaze(dashboardData = data, dashboardViewModel = dashboardViewModel)
        }

        item {
            Spacer(modifier = Modifier.size(8.dp))
        }

        item {
            Text("Grafik Pengeluaran & Pendapatan Harian", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
            LineChartCard(data.lineChartData)
        }

        if (data.overspentCategoriesMonthly.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.size(8.dp))
            }

            item {
                Text("Kategori Pengeluaran Berlebihan (Bulanan)", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(data.overspentCategoriesMonthly) {
                        OverspendCard(it, modifier = Modifier.width(300.dp)) // Give a fixed width for scrollability
                    }
                }
            }
        }

        if (data.overspentCategoriesDaily.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.size(8.dp))
            }

            item {
                Text("Kategori Pengeluaran Berlebihan (Harian)", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(data.overspentCategoriesDaily) {
                        OverspendCard(it, modifier = Modifier.width(300.dp)) // Give a fixed width for scrollability
                    }
                }
            }
        }

        if (data.monthReceipts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.size(8.dp))
            }

            item {
                Text("Struk Bulan Ini", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
            }
            items(data.monthReceipts) {
                ReceiptCard(it)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun SummaryCard(data: DashboardData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryItem("Income Monthly", data.income, true, Modifier.weight(1f))
            SummaryItem("Today Spending", data.totalTodaySpending, false, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryItem("Month Spending", data.totalMonthSpending, false, Modifier.weight(1f))
            SummaryItem("Year Spending", data.totalYearSpending, false, Modifier.weight(1f))
        }
    }
}

@Composable
fun SummaryItem(title: String, amount: Double, isIncome: Boolean, modifier: Modifier = Modifier) {
    val textColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val amountText = if (isIncome) "+${formatRp(amount)}" else "-${formatRp(amount)}"

    Card(
        modifier = modifier
            .height(90.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = amountText,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun LineChartCard(data: List<LineChartItem>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error
    val Y_DIVISOR = 1000
    val YDecimalFormat = DecimalFormat("#.##K")
    val StartAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        YDecimalFormat.format(value / Y_DIVISOR)
    }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            val days = data.map { it.day.toDouble() }
            val incomes = data.map { it.income }
            val expenses = data.map { it.expense }

            lineSeries { series(days, incomes) }
            lineSeries { series(days, expenses) }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(incomeColor)),
                        areaFill =
                            LineCartesianLayer.AreaFill.single(
                                fill(
                                    ShaderProvider.verticalGradient(
                                        arrayOf(incomeColor.copy(alpha = 0.4f), Color.Transparent)
                                    )
                                )
                            )
                    )
                )
            ),
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(expenseColor)),
                        areaFill =
                            LineCartesianLayer.AreaFill.single(
                                fill(
                                    ShaderProvider.verticalGradient(
                                        arrayOf(expenseColor.copy(alpha = 0.4f), Color.Transparent)
                                    )
                                )
                            )
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(valueFormatter = StartAxisValueFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(),
            marker = rememberMarker(),
        ),
        modelProducer = modelProducer,
        modifier = Modifier.height(300.dp),
        scrollState = rememberVicoScrollState(true)
    )
}

@Composable
fun OverspendCard(data: OverspentCategory, modifier: Modifier = Modifier) {
    val categoryIcon: ImageVector = when (data.category.lowercase()) {
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

    val iconBackgroundColor: Color = when (data.category.lowercase()) {
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

    // Pastikan percentage dikonversi ke float dari string
    val progress = (data.percentage.removeSuffix("%").toFloatOrNull() ?: 0f) / 100f
    val cappedProgress = progress.coerceAtMost(1f)

    Card(
        modifier = modifier // Use the passed modifier here
            .fillMaxHeight(), // Allow card to fill height if needed by LazyRow's item
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the card's dimensions
                .padding(16.dp),
            verticalArrangement = Arrangement.Center, // Center content vertically
            horizontalAlignment = Alignment.Start
        ) {
            // Icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBackgroundColor,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Changed to height for vertical spacing

            // Detail Info
            Text(
                text = data.category.replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Jumlah: ${formatRp(data.amount)}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Melebihi Batas: ${formatRp(data.exceededBy)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = cappedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (progress > 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = Color.LightGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Persentase: ${data.percentage}",
                style = MaterialTheme.typography.labelSmall,
                color = if (progress > 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}


@Composable
fun ReceiptCard(struct: MonthReceipt) {
    val categoryIcon: ImageVector = remember(struct.categorySpending) {
        when (struct.categorySpending?.lowercase()) {
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

    val iconBackgroundColor: Color = remember(struct.categorySpending) {
        when (struct.categorySpending?.lowercase()) {
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

    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
                        text = struct.categorySpending?.ifBlank { "-" }?.replaceFirstChar { it.uppercase() } ?: "-",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = struct.merchantName ?: "-",
                        fontSize = 14.sp,
                        color =  Color.Gray
                    )
                    Text(
                        text = struct.transactionDate,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Right side: Action Buttons and Total
            Column(horizontalAlignment = Alignment.End) {

                // Total Expense
                val formattedTotal = if (struct.finalTotal < 0) {
                    "Rp${struct.finalTotal.toInt()}"
                } else {
                    "-Rp${struct.finalTotal.toInt()}"
                }
                Text(
                    text = formattedTotal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error // Tetap merah untuk pengeluaran
                )
            }
        }
    }
}

@Composable
fun AIAnalaze(
    dashboardData: DashboardData, // Pass DashboardData to AIAnalaze
    dashboardViewModel: DashboardViewModel
) {
    var hasAnalyzed by remember { mutableStateOf(false) }
    val aiAnalysisUiState by dashboardViewModel.aiAnalysisUiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!hasAnalyzed) {
            Button(
                onClick = {
                    hasAnalyzed = true
                    dashboardViewModel.getAIInsights(
                        income = dashboardData.income,
                        monthReceipts = dashboardData.monthReceipts
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Analyze Your Spending")
            }
        } else {
            when (aiAnalysisUiState) {
                is AIAnalysisUiState.Loading -> {
                    CircularProgressIndicator()
                    Text("Analyzing your spending...")
                }
                is AIAnalysisUiState.Success -> {
                    val insights = (aiAnalysisUiState as AIAnalysisUiState.Success).insights
                    InsightsCard(insights)
                }
                is AIAnalysisUiState.Error -> {
                    Text(
                        text = "Error: ${(aiAnalysisUiState as AIAnalysisUiState.Error).message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                AIAnalysisUiState.Initial -> {
                    // Should not happen if hasAnalyzed is true, but for completeness
                    Button(
                        onClick = {
                            hasAnalyzed = true
                            dashboardViewModel.getAIInsights(
                                income = dashboardData.income,
                                monthReceipts = dashboardData.monthReceipts
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Analyze Your Spending")
                    }
                }
            }
        }
    }
}

@Composable
fun InsightsCard(insights: Insights) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("AI Insights", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)

            insights.saran?.let {
                Text("Saran:", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
            }

            insights.peringatan?.let {
                Text("Peringatan:", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
            }

            insights.rekomendasiAksi?.let { actions ->
                if (actions.isNotEmpty()) {
                    Text("Rekomendasi Aksi:", fontWeight = FontWeight.SemiBold, color = Orange40)
                    Column {
                        actions.forEachIndexed { index, action ->
                            Text("${index + 1}. $action", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
        }
    }
}

fun formatRp(value: Double): String = when {
    value >= 1_000_000 -> "Rp${(value / 1_000_000).toInt()}jt"
    value >= 1_000 -> "Rp${(value / 1_000).toInt()}rb"
    else -> "Rp${value.toInt()}"
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    KasKuTheme {
        DashboardScreen()
    }
}