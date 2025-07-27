package com.android.kasku.data.dashboard

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

interface DashboardRepository {
    suspend fun getDashboardData(context: Context): DashboardResult<DashboardData>
    suspend fun getAIInsights(request: AIInsightsRequest): DashboardResult<AIInsightsResponse>
}

class DashboardRepositoryImpl(
    private val onTokenExpired: () -> Unit // Re-use the token expired callback
) : DashboardRepository {

    private val BASE_URL = BuildConfig.PROD_BASE_URL
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor(onTokenExpired))
        .build()
    private val gson = Gson()

    override suspend fun getDashboardData(context: Context): DashboardResult<DashboardData> {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$BASE_URL/api/dashboard") // Your dashboard API endpoint
                .get()
                .build()

            try {
                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val dashboardData = gson.fromJson(responseBody, DashboardData::class.java)
                    DashboardResult.Success(dashboardData)
                } else {
                    val errorMessage = responseBody ?: "Failed to fetch dashboard data: ${response.code}"
                    DashboardResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                DashboardResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun getAIInsights(request: AIInsightsRequest): DashboardResult<AIInsightsResponse> {
        return withContext(Dispatchers.IO) {
            val requestBody = gson.toJson(request)
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val httpRequest = Request.Builder()
                .url("$BASE_URL/api/dashboard/insights")
                .post(requestBody)
                .build()

            try {
                val response = httpClient.newCall(httpRequest).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val aiInsightsResponse =
                        gson.fromJson(responseBody, AIInsightsResponse::class.java)
                    DashboardResult.Success(aiInsightsResponse)
                } else {
                    val errorMessage = responseBody ?: "Failed to get AI insights: ${response.code}"
                    DashboardResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                DashboardResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}