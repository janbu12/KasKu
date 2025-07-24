package com.android.kasku.data.struct

import android.content.Context
import com.android.kasku.BuildConfig
import com.android.kasku.network.TokenInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

data class StructItem(
    val id: String,
    val merchant_name: String?,
    val transaction_date: String,
    val category_spending: String?,
    val final_total: Double
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
        finalTotal: Double
    ): StructResult<Unit>
}

class StructRepositoryImpl(
    private val onTokenExpired: () -> Unit
) : StructRepository {

    private val BASE_URL = BuildConfig.KASKU_BASE_URL

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor(onTokenExpired))
        .build()

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

                        val structItem = StructItem(
                            id = item.getString("id"),
                            merchant_name = item.optString("merchant_name", null),
                            transaction_date = item.getString("transaction_date"),
                            category_spending = item.optString("category_spending", null),
                            final_total = item.optDouble("final_total", 0.0)
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
                    val item = org.json.JSONObject(responseBody)
                    val structItem = StructItem(
                        id = item.getString("id"),
                        merchant_name = item.optString("merchant_name", null),
                        transaction_date = item.getString("transaction_date"),
                        category_spending = item.optString("category_spending", null),
                        final_total = item.optDouble("final_total", 0.0)
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
        finalTotal: Double
    ): StructResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val json = org.json.JSONObject().apply {
                    put("category_spending", categorySpending)
                    put("merchant_name", merchantName)
                    put("transaction_date", transactionDate)
                    put("final_total", finalTotal)
                }

                val body = okhttp3.RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    json.toString()
                )

                val request = Request.Builder()
                    .url("$BASE_URL/api/structs/$id")
                    .put(body)
                    .build()

                val response = httpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    StructResult.Success(Unit)
                } else {
                    StructResult.Error("Failed to update struct: ${response.code}")
                }
            } catch (e: Exception) {
                StructResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}