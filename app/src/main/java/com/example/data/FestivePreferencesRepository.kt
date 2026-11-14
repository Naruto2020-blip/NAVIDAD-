package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.FestiveTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.festiveDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "navidad_preferences",
)

data class FestivePreferences(
    val snowEnabled: Boolean = true,
    val darkAmbiance: Boolean = true,
    val selectedTarget: FestiveTarget = FestiveTarget.NAVIDAD,
)

class FestivePreferencesRepository(private val context: Context) {

    private object Keys {
        val SNOW_ENABLED = booleanPreferencesKey("snow_enabled")
        val DARK_AMBIANCE = booleanPreferencesKey("dark_ambiance")
        val TARGET = stringPreferencesKey("selected_target")
    }

    val preferencesFlow: Flow<FestivePreferences> = context.festiveDataStore.data.map { prefs ->
        val savedTargetName = prefs[Keys.TARGET] ?: FestiveTarget.NAVIDAD.name
        val target = FestiveTarget.entries.firstOrNull { it.name == savedTargetName }
            ?: FestiveTarget.NAVIDAD

        FestivePreferences(
            snowEnabled = prefs[Keys.SNOW_ENABLED] ?: true,
            darkAmbiance = prefs[Keys.DARK_AMBIANCE] ?: true,
            selectedTarget = target,
        )
    }

    suspend fun setSnowEnabled(enabled: Boolean) {
        context.festiveDataStore.edit { it[Keys.SNOW_ENABLED] = enabled }
    }

    suspend fun setDarkAmbiance(dark: Boolean) {
        context.festiveDataStore.edit { it[Keys.DARK_AMBIANCE] = dark }
    }

    suspend fun setSelectedTarget(target: FestiveTarget) {
        context.festiveDataStore.edit { it[Keys.TARGET] = target.name }
    }
}
