package com.android.kasku.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Deklarasi ekstensi properti DataStore di luar kelas untuk akses global
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

object DataStoreManager {
    private val TOKEN_KEY = stringPreferencesKey("id_token")
    private val USERNAME_KEY = stringPreferencesKey("username") // Kunci baru untuk username

    suspend fun saveToken(context: Context, token: String?) {
        context.dataStore.edit { prefs ->
            // Pastikan token tidak null sebelum disimpan, atau simpan null jika itu yang diinginkan
            prefs[TOKEN_KEY] = token ?: "" // Menggunakan string kosong jika null, sesuaikan jika Anda ingin null
        }
    }

    suspend fun getToken(context: Context): String? {
        val prefs = context.dataStore.data.first()
        return prefs[TOKEN_KEY].takeIf { it?.isNotBlank() == true } // Mengembalikan null jika kosong/blank
    }

    suspend fun saveUsername(context: Context, username: String?) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = username ?: "" // Simpan username, kosong jika null
        }
    }

    fun getUsername(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USERNAME_KEY].takeIf { it?.isNotBlank() == true } // Mengembalikan null jika kosong/blank
        }
    }

    suspend fun clearAllUserData(context: Context) { // Nama fungsi diubah
        context.dataStore.edit { prefs ->
            prefs.clear() // Menghapus semua data yang disimpan
        }
    }
}