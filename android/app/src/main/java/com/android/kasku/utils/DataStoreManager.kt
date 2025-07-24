package com.android.kasku.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

object DataStoreManager {
    private val TOKEN_KEY = stringPreferencesKey("id_token")

    suspend fun saveToken(context: Context, token: String?) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token as String
        }
    }

    suspend fun getToken(context: Context): String? {
        val prefs = context.dataStore.data.first()
        return prefs[TOKEN_KEY]
    }

    suspend fun clearToken(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}
