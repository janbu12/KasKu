package com.android.kasku.data.struct

import android.content.Context
import com.android.kasku.BuildConfig
import com.android.kasku.network.TokenInterceptor
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class StructItem(
    val id: String,
    val merchant_name: String?,
    val transaction_date: String,
    val transaction_time: String, // Add transaction_time to StructItem
    val items: List<Item>, // Add items to StructItem
    val subtotal: Double, // Add subtotal to StructItem
    val discount_amount: Double?, // Add discount_amount to StructItem
    val additional_charges: Double?, // Add additional_charges to StructItem
    val tax_amount: Double?, // Add tax_amount to StructItem
    val final_total: Double,
    val tender_type: String?, // Add tender_type to StructItem
    val amount_paid: Double?, // Add amount_paid to StructItem
    val change_given: Double?, // Add change_given to StructItem
    val category_spending: String?
)

sealed class StructResult<out T> {
    data class Success<out T>(val data: T) : StructResult<T>()
    data class Error(val message: String) : StructResult<Nothing>()
}

interface StructRepository {
    suspend fun getStructList(context: Context): StructResult<List<StructItem>>
    suspend fun getStructById(context: Context, id: String): StructResult<StructItem>
    suspend fun updateStruct(
        context: Context,
        id: String,
        categorySpending: String?,
        merchantName: String?,
        transactionDate: String,
        transactionTime: String, // Add transaction_time to updateStruct
        items: List<Item>, // Add items to updateStruct
        subtotal: Double, // Add subtotal to updateStruct
        discountAmount: Double?, // Add discount_amount to updateStruct
        additionalCharges: Double?, // Add additional_charges to updateStruct
        taxAmount: Double?, // Add tax_amount to updateStruct
        finalTotal: Double,
        tenderType: String?, // Add tender_type to updateStruct
        amountPaid: Double?, // Add amount_paid to updateStruct
        changeGiven: Double? // Add change_given to updateStruct
    ): StructResult<Unit>
}

class StructRepositoryImpl(
    private val onTokenExpired: () -> Unit
) : StructRepository {

    private val BASE_URL = BuildConfig.KASKU_BASE_URL
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor(onTokenExpired))
        .build()
    private val gson = Gson()

    override suspend fun getStructList(context: Context): StructResult<List<StructItem>> {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$BASE_URL/api/structs")
                .get()
                .build()

            try {
                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val jsonArray = JSONArray(responseBody)
                    val structs = mutableListOf<StructItem>()

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val itemsArray = item.optJSONArray("items")
                        val itemsList = mutableListOf<Item>()
                        itemsArray?.let {
                            for (j in 0 until it.length()) {
                                val jsonItem = it.getJSONObject(j)
                                itemsList.add(
                                    Item(
                                        itemName = jsonItem.getString("item_name"),
                                        quantity = jsonItem.getInt("quantity"),
                                        unitPrice = jsonItem.getDouble("unit_price"),
                                        totalPriceItem = jsonItem.getDouble("total_price_item")
                                    )
                                )
                            }
                        }

                        val structItem = StructItem(
                            id = item.getString("id"),
                            merchant_name = item.optString("merchant_name", null),
                            transaction_date = item.getString("transaction_date"),
                            transaction_time = item.getString("transaction_time"),
                            items = itemsList,
                            subtotal = item.optDouble("subtotal", 0.0),
                            discount_amount = item.optDouble("discount_amount", 0.0).takeIf { item.has("discount_amount") },
                            additional_charges = item.optDouble("additional_charges", 0.0).takeIf { item.has("additional_charges") },
                            tax_amount = item.optDouble("tax_amount", 0.0).takeIf { item.has("tax_amount") },
                            final_total = item.optDouble("final_total", 0.0),
                            tender_type = item.optString("tender_type", null),
                            amount_paid = item.optDouble("amount_paid", 0.0).takeIf { item.has("amount_paid") },
                            change_given = item.optDouble("change_given", 0.0).takeIf { item.has("change_given") },
                            category_spending = item.optString("category_spending", null)
                        )

                        structs.add(structItem)
                    }

                    StructResult.Success(structs)
                } else {
                    val errorMessage = responseBody ?: "Failed to fetch structs: ${response.code}"
                    StructResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                StructResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun getStructById(context: Context, id: String): StructResult<StructItem> {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$BASE_URL/api/structs/$id")
                .get()
                .build()

            try {
                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val item = JSONObject(responseBody)
                    val itemsArray = item.optJSONArray("items")
                    val itemsList = mutableListOf<Item>()
                    itemsArray?.let {
                        for (j in 0 until it.length()) {
                            val jsonItem = it.getJSONObject(j)
                            itemsList.add(
                                Item(
                                    itemName = jsonItem.getString("item_name"),
                                    quantity = jsonItem.getInt("quantity"),
                                    unitPrice = jsonItem.getDouble("unit_price"),
                                    totalPriceItem = jsonItem.getDouble("total_price_item")
                                )
                            )
                        }
                    }

                    val structItem = StructItem(
                        id = item.getString("id"),
                        merchant_name = item.optString("merchant_name", null),
                        transaction_date = item.getString("transaction_date"),
                        transaction_time = item.getString("transaction_time"),
                        items = itemsList,
                        subtotal = item.optDouble("subtotal", 0.0),
                        discount_amount = item.optDouble("discount_amount", 0.0).takeIf { item.has("discount_amount") },
                        additional_charges = item.optDouble("additional_charges", 0.0).takeIf { item.has("additional_charges") },
                        tax_amount = item.optDouble("tax_amount", 0.0).takeIf { item.has("tax_amount") },
                        final_total = item.optDouble("final_total", 0.0),
                        tender_type = item.optString("tender_type", null),
                        amount_paid = item.optDouble("amount_paid", 0.0).takeIf { item.has("amount_paid") },
                        change_given = item.optDouble("change_given", 0.0).takeIf { item.has("change_given") },
                        category_spending = item.optString("category_spending", null)
                    )
                    StructResult.Success(structItem)
                } else {
                    StructResult.Error("Failed to fetch struct: ${response.code}")
                }
            } catch (e: Exception) {
                StructResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun updateStruct(
        context: Context,
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
    ): StructResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val receiptData = ReceiptUpdateData(
                    merchantName = merchantName ?: "",
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
                    changeGiven = changeGiven,
                    categorySpending = categorySpending
                )
                val requestBody = UpdateStructRequestBody(receipt = receiptData)

                val body = gson.toJson(requestBody).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("$BASE_URL/api/structs/$id")
                    .put(body)
                    .build()

                val response = httpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    StructResult.Success(Unit)
                } else {
                    val errorBody = response.body?.string()
                    StructResult.Error("Failed to update struct: ${response.code} - ${errorBody}")
                }
            } catch (e: Exception) {
                StructResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}