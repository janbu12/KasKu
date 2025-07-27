package com.android.kasku.data.dashboard

import com.google.gson.annotations.SerializedName

data class LineChartItem(
    @SerializedName("day") val day: Int,
    @SerializedName("income") val income: Double,
    @SerializedName("expense") val expense: Double
)

data class PieChartItem(
    @SerializedName("category") val category: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("percentage") val percentage: Double
)

data class OverspentCategory(
    @SerializedName("category") val category: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("exceededBy") val exceededBy: Double,
    @SerializedName("percentage") val percentage: String
)

data class MonthReceipt(
    @SerializedName("id") val id: String,
    @SerializedName("merchant_name") val merchantName: String?,
    @SerializedName("transaction_date") val transactionDate: String,
    @SerializedName("transaction_time") val transactionTime: String,
    @SerializedName("items") val items: List<com.android.kasku.data.struct.Item>, // Re-use Item from struct package
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("discount_amount") val discountAmount: Double?,
    @SerializedName("additional_charges") val additionalCharges: Double?,
    @SerializedName("tax_amount") val taxAmount: Double?,
    @SerializedName("final_total") val finalTotal: Double,
    @SerializedName("tender_type") val tenderType: String?,
    @SerializedName("amount_paid") val amountPaid: Double?,
    @SerializedName("change_given") val changeGiven: Double?,
    @SerializedName("category_spending") val categorySpending: String?
)


data class DashboardData(
    @SerializedName("lineChartData") val lineChartData: List<LineChartItem>,
    @SerializedName("pieChartData") val pieChartData: List<PieChartItem>,
    @SerializedName("totalMonthSpending") val totalMonthSpending: Double,
    @SerializedName("totalYearSpending") val totalYearSpending: Double,
    @SerializedName("totalTodaySpending") val totalTodaySpending: Double,
    @SerializedName("income") val income: Double,
    @SerializedName("overspentCategoriesMonthly") val overspentCategoriesMonthly: List<OverspentCategory>,
    @SerializedName("overspentCategoriesDaily") val overspentCategoriesDaily: List<OverspentCategory>,
    @SerializedName("monthReceipts") val monthReceipts: List<MonthReceipt>
)

// A sealed class for handling result states (Success, Error)
sealed class DashboardResult<out T> {
    data class Success<out T>(val data: T) : DashboardResult<T>()
    data class Error(val message: String) : DashboardResult<Nothing>()
}

data class AIInsightsRequest(
    @SerializedName("income") val income: Double,
    @SerializedName("monthReceipts") val monthReceipts: List<MonthReceipt>
)

data class AIInsightsResponse(
    @SerializedName("message") val message: String,
    @SerializedName("insights") val insights: Insights?
)

data class Insights(
    @SerializedName("saran") val saran: String?,
    @SerializedName("peringatan") val peringatan: String?,
    @SerializedName("rekomendasi_aksi") val rekomendasiAksi: List<String>?
)