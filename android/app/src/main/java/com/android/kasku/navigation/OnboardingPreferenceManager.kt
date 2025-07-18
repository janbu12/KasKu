package com.android.kasku.navigation

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

object OnboardingPreferenceManager {
    private val Context.dataStore by preferencesDataStore("settings")
    private val ONBOARDING_SHOWN = booleanPreferencesKey("onboarding_shown")

    suspend fun setOnboardingShown(context: Context, shown: Boolean) {
        context.dataStore.edit { settings ->
            settings[ONBOARDING_SHOWN] = shown
        }
    }

    suspend fun isOnboardingShown(context: Context): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[ONBOARDING_SHOWN] ?: false
    }
}