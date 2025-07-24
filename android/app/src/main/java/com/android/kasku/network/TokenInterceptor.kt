package com.android.kasku.network

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(
    private val onTokenExpired: () -> Unit
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val user = FirebaseAuth.getInstance().currentUser
        val token = runBlocking {
            user?.getIdToken(false)?.await()?.token
        }

        val request = chain.request().newBuilder()
            .apply {
                token?.let {
                    addHeader("Authorization", "Bearer $it")
                }
            }
            .build()

        val response = chain.proceed(request)

        if (response.code == 401) {
            // Token expired or invalid
            onTokenExpired()
        }

        return response
    }
}
