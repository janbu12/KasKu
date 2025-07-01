package com.android.kasku.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                // Simpan username ke Firestore
                firestore.collection("users").document(user.uid).set(
                    mapOf(
                        "uid" to user.uid,
                        "username" to username,
                        "email" to user.email,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                ).await()
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Registration failed: User is null.")
            }
        } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            AuthResult.Error("Email is already in use.", e)
        } catch (e: Exception) {
            AuthResult.Error("Registration error: ${e.localizedMessage}", e)
        }
    }
}