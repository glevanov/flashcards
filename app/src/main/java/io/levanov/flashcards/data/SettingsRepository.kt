package io.levanov.flashcards.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.levanov.flashcards.srs.SessionBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// File-level extension property — the DataStore singleton lives on the
// Application context; DataStore forbids multiple instances per file.
private val Context.settingsStore by preferencesDataStore(name = "settings")

data class AppSettings(
    val newCardsPerDay: Int,
    val ttsEnabled: Boolean,
)

class SettingsRepository(private val appContext: Context) {

    val settings: Flow<AppSettings> = appContext.settingsStore.data.map { prefs ->
        AppSettings(
            newCardsPerDay = prefs[KEY_NEW_CARDS] ?: SessionBuilder.DEFAULT_NEW_CARDS,
            ttsEnabled = prefs[KEY_TTS] ?: true,
        )
    }

    suspend fun setNewCardsPerDay(value: Int) {
        appContext.settingsStore.edit { it[KEY_NEW_CARDS] = value.coerceIn(0, 100) }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        appContext.settingsStore.edit { it[KEY_TTS] = enabled }
    }

    private companion object {
        val KEY_NEW_CARDS = intPreferencesKey("new_cards_per_day")
        val KEY_TTS = booleanPreferencesKey("tts_enabled")
    }
}