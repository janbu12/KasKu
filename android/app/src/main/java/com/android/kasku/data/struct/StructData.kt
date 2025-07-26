package com.android.kasku.data.struct

import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("item_name") val itemName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Double, // Menggunakan Double untuk harga
    @SerializedName("total_price_item") val totalPriceItem: Double // Menggunakan Double untuk harga
)

data class StructData(
    @SerializedName("id") val id: String? = null, // ID opsional karena mungkin baru dibuat
    @SerializedName("merchant_name") val merchantName: String,
    @SerializedName("transaction_date") val transactionDate: String, // String YYYY-MM-DD
    @SerializedName("transaction_time") val transactionTime: String, // String HH:MM
    @SerializedName("items") val items: List<Item>,
    @SerializedName("subtotal") val subtotal: Double, // Menggunakan Double untuk subtotal
    @SerializedName("discount_amount") val discountAmount: Double? = null, // Optional, can be null
    @SerializedName("additional_charges") val additionalCharges: Double? = null, // Optional, can be null
    @SerializedName("tax_amount") val taxAmount: Double? = null, // Optional, can be null
    @SerializedName("final_total") val finalTotal: Double, // Menggunakan Double untuk total
    @SerializedName("tender_type") val tenderType: String? = null, // Optional, can be null
    @SerializedName("amount_paid") val amountPaid: Double? = null, // Optional, can be null
    @SerializedName("change_given") val changeGiven: Double? = null, // Optional, can be null
    @SerializedName("category_spending") val category_spending: String? = null // Add this for GET requests to match StructItem
)

data class StructRequestBody( // This class is for POST, currently not used in the context, but keeping it as is from original
    @SerializedName("merchant_name") val merchantName: String,
    @SerializedName("transaction_date") val transactionDate: String,
    @SerializedName("transaction_time") val transactionTime: String,
    @SerializedName("items") val items: List<Item>,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("discount_amount") val discountAmount: Double? = null,
    @SerializedName("additional_charges") val additionalCharges: Double? = null,
    @SerializedName("tax_amount") val taxAmount: Double? = null,
    @SerializedName("final_total") val finalTotal: Double,
    @SerializedName("tender_type") val tenderType: String? = null,
    @SerializedName("amount_paid") val amountPaid: Double? = null,
    @SerializedName("change_given") val changeGiven: Double? = null
)

data class UpdateStructRequestBody(
    @SerializedName("receipt") val receipt: ReceiptUpdateData
)

data class ReceiptUpdateData(
    @SerializedName("merchant_name") val merchantName: String,
    @SerializedName("transaction_date") val transactionDate: String,
    @SerializedName("transaction_time") val transactionTime: String, // Assuming this is part of the update
    @SerializedName("items") val items: List<Item>, // Assuming this is part of the update
    @SerializedName("subtotal") val subtotal: Double, // Assuming this is part of the update
    @SerializedName("discount_amount") val discountAmount: Double? = null,
    @SerializedName("additional_charges") val additionalCharges: Double? = null,
    @SerializedName("tax_amount") val taxAmount: Double? = null,
    @SerializedName("final_total") val finalTotal: Double,
    @SerializedName("tender_type") val tenderType: String? = null,
    @SerializedName("amount_paid") val amountPaid: Double? = null,
    @SerializedName("change_given") val changeGiven: Double? = null,
    @SerializedName("category_spending") val categorySpending: String? = null // Add this for PUT requests
)

// New data classes for image upload response
data class ItemUpload(
    @SerializedName("item_name") val itemName: String,
    @SerializedName("quantity") val quantity: Int? = null, // Can be null
    @SerializedName("unit_price") val unitPrice: Double? = null, // Can be null
    @SerializedName("total_price_item") val totalPriceItem: Double? = null // Can be null
)

data class StructuredData(
    @SerializedName("merchant_name") val merchantName: String,
    @SerializedName("transaction_date") val transactionDate: String,
    @SerializedName("transaction_time") val transactionTime: String,
    @SerializedName("items") val items: List<ItemUpload>,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("discount_amount") val discountAmount: Double? = null,
    @SerializedName("additional_charges") val additionalCharges: Double? = null,
    @SerializedName("tax_amount") val taxAmount: Double? = null,
    @SerializedName("final_total") val finalTotal: Double,
    @SerializedName("tender_type") val tenderType: String? = null,
    @SerializedName("amount_paid") val amountPaid: Double? = null,
    @SerializedName("change_given") val changeGiven: Double? = null
)

data class UploadStructResponse(
    @SerializedName("message") val message: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("structuredData") val structuredData: StructuredData,
    @SerializedName("rawGeminiText") val rawGeminiText: String? = null // Optional, if you want to keep the raw text
)