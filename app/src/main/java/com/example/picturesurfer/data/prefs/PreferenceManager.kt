package com.example.picturesurfer.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.picturesurfer.data.api.models.DogImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension to create DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    private val gson = Gson()

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val TEXT_SIZE_KEY = floatPreferencesKey("text_size")
        private val FAVORITES_KEY = stringSetPreferencesKey("favorites")
        private val FAVORITE_DOGS_DATA_KEY = stringPreferencesKey("favorite_dogs_data")

        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val DEFAULT_TEXT_SIZE = 14f
    }

    // Flow for theme
    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: THEME_LIGHT
    }

    // Flow for text size
    val textSizeFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[TEXT_SIZE_KEY] ?: DEFAULT_TEXT_SIZE
    }

    // Flow for favorites
    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[FAVORITES_KEY] ?: emptySet()
    }

    // Flow for favorite dogs data
    val favoriteDogsFlow: Flow<List<DogImage>> = context.dataStore.data.map { preferences ->
        val json = preferences[FAVORITE_DOGS_DATA_KEY] ?: "[]"
        try {
            val type = object : TypeToken<List<DogImage>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Save theme
    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    // Save text size
    suspend fun saveTextSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_SIZE_KEY] = size
        }
    }

    // Save favorites
    suspend fun saveFavorites(favorites: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[FAVORITES_KEY] = favorites
        }
    }

    // Toggle favorite and save dog data
    suspend fun toggleFavorite(dog: DogImage) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITES_KEY] ?: emptySet()
            val currentDogsJson = preferences[FAVORITE_DOGS_DATA_KEY] ?: "[]"

            val type = object : TypeToken<MutableList<DogImage>>() {}.type
            val currentDogs: MutableList<DogImage> = try {
                gson.fromJson(currentDogsJson, type) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            if (dog.id in currentFavorites) {
                // Remove from favorites
                preferences[FAVORITES_KEY] = currentFavorites - dog.id
                currentDogs.removeAll { it.id == dog.id }
            } else {
                // Add to favorites
                preferences[FAVORITES_KEY] = currentFavorites + dog.id
                // Only add if not already there
                if (currentDogs.none { it.id == dog.id }) {
                    currentDogs.add(dog)
                }
            }

            // Save updated dogs list
            preferences[FAVORITE_DOGS_DATA_KEY] = gson.toJson(currentDogs)
        }
    }

    // Get favorite dogs count
    suspend fun getFavoritesCount(): Int {
        val preferences = context.dataStore.data.first()
        return (preferences[FAVORITES_KEY] ?: emptySet()).size
    }
}