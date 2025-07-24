package com.android.kasku.data.profile

import android.content.Context
import android.util.Log
import com.android.kasku.BuildConfig
import com.android.kasku.utils.DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

// Data class to match the JSON structure from the API
data class UserProfile(
    val occupation: String,
    val income: Long,
    val financialGoals: String,
    val currency: String
)

data class UserData(
    val uid: String,
    val username: String,
    val email: String,
    val userProfile: UserProfile
)

// Result wrapper for the API call
sealed class ProfileResult<out T> {
    data class Success<out T>(val data: T) : ProfileResult<T>()
    data class Error(val message: String) : ProfileResult<Nothing>()
}

interface ProfileRepository {
    suspend fun getUserProfile(context: Context): ProfileResult<UserData>
}

class ProfileRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val httpClient: OkHttpClient,
) : ProfileRepository {

    private val BASE_URL = BuildConfig.KASKU_BASE_URL

    override suspend fun getUserProfile(context: Context): ProfileResult<UserData> {
        return withContext(Dispatchers.IO) {
            val currentUser = firebaseAuth.currentUser

            if (currentUser == null) {
                return@withContext ProfileResult.Error("No authenticated user found.")
            }


            // Get the token for authentication

            val token = try {
                DataStoreManager.getToken(context)
            } catch (e: Exception) {
                return@withContext ProfileResult.Error("Failed to get saved token: ${e.message}")
            }



            if (token == null) {
                return@withContext ProfileResult.Error("Auth token is null.")
            }

            val request = Request.Builder()
                .url("$BASE_URL/api/users/me")
                .header("Authorization", "Bearer $token") // Send token in the header
                .get()
                .build()

            try {
                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val userProfileJson = json.getJSONObject("userProfile")

                    val userData = UserData(
                        uid = json.getString("uid"),
                        username = json.getString("username"),
                        email = json.getString("email"),
                        userProfile = UserProfile(
                            occupation = userProfileJson.getString("occupation"),
                            income = userProfileJson.getLong("income"),
                            financialGoals = userProfileJson.getString("financialGoals"),
                            currency = userProfileJson.getString("currency")
                        )
                    )
                    ProfileResult.Success(userData)
                } else {
                    val errorMessage = responseBody ?: "Failed to fetch profile: ${response.code}"
                    ProfileResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                ProfileResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun updateUserProfile(
        occupation: String,
        income: Long,
        financialGoals: String,
        context: Context
    ): ProfileResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = try {
                    DataStoreManager.getToken(context)
                } catch (e: Exception) {
                    return@withContext ProfileResult.Error("Failed to get saved token: ${e.message}")
                }

                val requestBody = JSONObject().apply {
                    put("occupation", occupation)
                    put("income", income)
                    put("financialGoals", financialGoals)
                }.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("$BASE_URL/api/users/me/profile")
                    .put(requestBody)
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    ProfileResult.Success(Unit)
                } else {
                    ProfileResult.Error("Gagal update: ${response.message}")
                }
            } catch (e: Exception) {
                ProfileResult.Error(e.message ?: "Unknown error")
            }
        }
    }
}