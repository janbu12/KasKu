// app/src/main/java/com/android/kasku/network/AuthInterceptor.kt
package com.android.kasku.network

import android.content.Context
import android.util.Log
import com.android.kasku.utils.DataStoreManager // Import DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.Response

// Interceptor untuk menambahkan token ke request dan menangani 401/403
class AuthInterceptor(private val context: Context, private val firebaseAuth: FirebaseAuth) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Mendapatkan token secara sinkron (blocking) karena ini adalah interceptor
        // HATI-HATI: runBlocking hanya boleh digunakan di interceptor karena ini memblokir thread.
        // Pastikan ini tidak menyebabkan ANR.
        val token: String? = runBlocking {
            try {
                // Memaksa refresh token jika sudah kadaluarsa
                firebaseAuth.currentUser?.getIdToken(true)?.await()?.token
            } catch (e: Exception) {
                Log.e("AuthInterceptor", "Failed to get ID token for request: ${e.message}")
                null
            }
        }

        val requestBuilder = chain.request().newBuilder()
        token?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }

        val response = chain.proceed(requestBuilder.build())

        // --- Penanganan Respons 401/403 ---
        if (response.code == 401 || response.code == 403) {
            Log.e("AuthInterceptor", "Unauthorized/Forbidden response received: ${response.code}. Logging out user.")
            // Lakukan logout di background thread
            runBlocking {
                withContext(Dispatchers.Main) { // Kembali ke Main thread untuk signOut dan update UI
                    firebaseAuth.signOut() // Logout dari Firebase Client SDK
                    // Hapus token dari DataStore
                    DataStoreManager.saveToken(context, null) // Hapus token yang tersimpan
                    // Anda perlu mekanisme untuk memicu navigasi ke LoginScreen dari sini.
                    // Ini bisa dilakukan melalui SharedFlow di ViewModel atau BroadcastReceiver.
                    // Untuk kesederhanaan, kita akan mengandalkan AuthViewModel untuk mendeteksi perubahan currentUser.
                }
            }
        }

        return response
    }
}