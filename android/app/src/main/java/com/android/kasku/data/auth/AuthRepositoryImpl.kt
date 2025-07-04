package com.android.kasku.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.android.kasku.BuildConfig

// import com.google.firebase.auth.FirebaseUser
// import com.google.firebase.firestore.FirebaseFirestore // Untuk menyimpan username saat register

// Jika Anda menggunakan Dependency Injection (DI) seperti Hilt, Anda akan menggunakan @Inject constructor
// class AuthRepositoryImpl @Inject constructor(
//     private val firebaseAuth: FirebaseAuth,
//     private val firestore: FirebaseFirestore
// ) : AuthRepository {

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val httpClient: OkHttpClient = OkHttpClient()

    private val BASE_URL = BuildConfig.KASKU_BASE_URL
    private val PROD_BASE_URL = BuildConfig.PROD_BASE_URL

    override suspend fun loginUser(email: String, password: String): AuthResult<com.google.firebase.auth.FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Login failed: User is null.")
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Error("Email not registered or invalid.", e)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Invalid password.", e)
        } catch (e: Exception) {
            AuthResult.Error("Login error: ${e.localizedMessage}", e)
        }
    }

    override suspend fun registerUser(username: String, email: String, password: String): AuthResult<com.google.firebase.auth.FirebaseUser> {
        return withContext(Dispatchers.IO) { // Pastikan operasi jaringan di Dispatchers.IO
            try {
                val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

                val jsonObject = JSONObject().apply {
                    put("username", username)
                    put("email", email)
                    put("password", password)
                }
                val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)

                // PENTING: Ganti dengan IP host Anda jika di perangkat fisik, atau 10.0.2.2 untuk emulator
                val url = "${BASE_URL}/auth/register" // URL backend Anda

                val request = okhttp3.Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = httpClient.newCall(request).execute() // Gunakan httpClient yang sudah diinisialisasi

                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    // Jika backend register sukses, kita asumsikan user sudah terbuat di Firebase Auth juga
                    // Anda bisa mengambil UID dari response body jika backend mengembalikannya,
                    // atau cukup login user setelah register sukses.
                    // Untuk kesederhanaan, kita bisa membuat FirebaseUser dummy atau mengambil user dari Auth
                    // setelah register sukses.
                    // Namun, karena backend yang membuat user di Firebase Auth, kita bisa langsung ambil user
                    // dari firebaseAuth.currentUser atau mencoba login ulang jika perlu.
                    // Untuk saat ini, kita akan menganggap sukses jika API backend merespons 2xx.
                    // Anda mungkin perlu mengambil UID dari respons backend jika ingin menggunakannya di client.

                    // Contoh pengambilan user dari Firebase Auth setelah register sukses
                    // Ini akan berhasil jika backend Anda berhasil membuat user di Firebase Auth
                    val firebaseUser = firebaseAuth.currentUser // Coba ambil user yang sedang login (jika ada)
                        ?: firebaseAuth.signInWithEmailAndPassword(email, password).await().user // Atau login ulang

                    if (firebaseUser != null) {
                        AuthResult.Success(firebaseUser)
                    } else {
                        AuthResult.Error("Registration succeeded on backend, but Firebase user could not be retrieved.")
                    }

                } else {
                    val errorJson = responseBody?.let { JSONObject(it) }
                    val errorMessage = errorJson?.getString("message") ?: "Registration failed: ${response.code}"
                    AuthResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                AuthResult.Error("Network error or server unreachable: ${e.localizedMessage}", e)
            }
        }
    }
}