package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.FestiveCatalog
import com.example.model.FestiveTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.festiveDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "navidad_preferences",
)

data class FestivePreferences(
    val selectedTrackId: String = FestiveCatalog.carols.first().id,
    val musicEnabled: Boolean = true,
    val volume: Float = 0.72f,
    val snowEnabled: Boolean = true,
    val darkAmbiance: Boolean = true,
    val selectedTarget: FestiveTarget = FestiveTarget.NAVIDAD,
)

class FestivePreferencesRepository(private val context: Context) {

    private object Keys {
        val TRACK_ID = stringPreferencesKey("selected_track_id")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val VOLUME = floatPreferencesKey("volume")
        val SNOW_ENABLED = booleanPreferencesKey("snow_enabled")
        val DARK_AMBIANCE = booleanPreferencesKey("dark_ambiance")
        val TARGET = stringPreferencesKey("selected_target")
    }

    val preferencesFlow: Flow<FestivePreferences> = context.festiveDataStore.data.map { prefs ->
        val savedTargetName = prefs[Keys.TARGET] ?: FestiveTarget.NAVIDAD.name
        val target = FestiveTarget.entries.firstOrNull { it.name == savedTargetName }
            ?: FestiveTarget.NAVIDAD

        val savedTrackId = prefs[Keys.TRACK_ID] ?: FestiveCatalog.carols.first().id
        val validTrackId = if (FestiveCatalog.carols.any { it.id == savedTrackId }) {
            savedTrackId
        } else {
            FestiveCatalog.carols.first().id
        }

        FestivePreferences(
            selectedTrackId = validTrackId,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
            volume = (prefs[Keys.VOLUME] ?: 0.72f).coerceIn(0f, 1f),
            snowEnabled = prefs[Keys.SNOW_ENABLED] ?: true,
            darkAmbiance = prefs[Keys.DARK_AMBIANCE] ?: true,
            selectedTarget = target,
        )
    }

    suspend fun setSelectedTrack(trackId: String) {
        context.festiveDataStore.edit { it[Keys.TRACK_ID] = trackId }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.festiveDataStore.edit { it[Keys.MUSIC_ENABLED] = enabled }
    }

    suspend fun setVolume(volume: Float) {
        context.festiveDataStore.edit { it[Keys.VOLUME] = volume.coerceIn(0f, 1f) }
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
