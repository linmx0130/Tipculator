package me.mengxiaolin.tipculator.repository

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Application.dataStore: DataStore<Preferences> by preferencesDataStore("settings")
class PreferencesRepository(private val applicationContext: Application) {
    private val TIPS_RATE_KEY = intPreferencesKey("tips_rate")
    private val IS_ROUND_TO_ZERO_KEY = booleanPreferencesKey("is_round_to_zero")

    val tipsRate: Flow<Int> = applicationContext.dataStore.data.map {
        preferences -> preferences[TIPS_RATE_KEY] ?: 15
    }
    suspend fun setTipsRate(value: Int) {
        applicationContext.dataStore.edit {preferences ->
            preferences[TIPS_RATE_KEY] = value
        }
    }

    val isRoundToZero: Flow<Boolean> = applicationContext.dataStore.data.map {
            preferences -> preferences[IS_ROUND_TO_ZERO_KEY] ?: false
    }
    suspend fun setIsRoundToZero(value: Boolean) {
        applicationContext.dataStore.edit {preferences ->
            preferences[IS_ROUND_TO_ZERO_KEY] = value
        }
    }
}