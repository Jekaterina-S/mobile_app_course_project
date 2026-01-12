package com.example.picturesurfer.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val TEXT_SIZE_KEY = floatPreferencesKey("text_size")
        private val FAVORITES_KEY = stringSetPreferencesKey("favorites")

        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val DEFAULT_TEXT_SIZE = 14f
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: THEME_LIGHT
    }

    val textSizeFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[TEXT_SIZE_KEY] ?: DEFAULT_TEXT_SIZE
    }

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[FAVORITES_KEY] ?: emptySet()
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun saveTextSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_SIZE_KEY] = size
        }
    }

    suspend fun saveFavorites(favorites: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[FAVORITES_KEY] = favorites
        }
    }

    suspend fun toggleFavorite(dogId: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITES_KEY] ?: emptySet()
            preferences[FAVORITES_KEY] = if (dogId in currentFavorites) {
                currentFavorites - dogId
            } else {
                currentFavorites + dogId
            }
        }
    }
}