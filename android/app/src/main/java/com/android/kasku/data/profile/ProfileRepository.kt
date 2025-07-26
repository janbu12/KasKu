package com.android.kasku.data.profile

import android.content.Context
import android.util.Log
import com.android.kasku.BuildConfig
import com.android.kasku.network.AuthInterceptor
import com.android.kasku.network.TokenInterceptor
import com.android.kasku.utils.DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
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
    private val onTokenExpired: () -> Unit
) : ProfileRepository {

    private val BASE_URL = BuildConfig.PROD_BASE_URL

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor(onTokenExpired))
        .build()

    private val gson = Gson()

    override suspend fun getUserProfile(context: Context): ProfileResult<UserData> {
        return withContext(Dispatchers.IO) {
            val currentUser = firebaseAuth.currentUser

            if (currentUser == null) {
                return@withContext ProfileResult.Error("No authenticated user found.")
            }

            val request = Request.Builder()
                .url("$BASE_URL/api/users/me")
                .get()
                .build()

            try {
                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    // Pengecekan null atau ketiadaan 'userProfile'
                    val userProfileJson = json.optJSONObject("userProfile")

                    val userProfile = if (userProfileJson != null) {
                        UserProfile(
                            occupation = userProfileJson.optString("occupation", ""), // Gunakan optString dengan default
                            income = userProfileJson.optLong("income", 0L), // Gunakan optLong dengan default
                            financialGoals = userProfileJson.optString("financialGoals", ""), // Gunakan optString dengan default
                            currency = userProfileJson.optString("currency", "IDR") // Default currency
                        )
                    } else {
                        // Nilai default jika userProfile tidak ada atau null
                        UserProfile(
                            occupation = "Belum Terisi",
                            income = 0L,
                            financialGoals = "Belum Terisi",
                            currency = "IDR"
                        )
                    }

                    val userData = UserData(
                        uid = json.getString("uid"),
                        username = json.getString("username"),
                        email = json.getString("email"),
                        userProfile = userProfile // Gunakan objek userProfile yang sudah aman
                    )
                    ProfileResult.Success(userData)
                } else {
                    val errorMessage = responseBody ?: "Failed to fetch profile: ${response.code}"
                    ProfileResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("ProfileRepo", "Network error or JSON parsing issue: ${e.message}", e)
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

                val requestBody = JSONObject().apply {
                    put("occupation", occupation)
                    put("income", income)
                    put("financialGoals", financialGoals)
                }.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("$BASE_URL/api/users/me/profile")
                    .put(requestBody)
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