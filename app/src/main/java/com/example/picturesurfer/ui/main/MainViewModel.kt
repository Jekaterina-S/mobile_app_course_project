package com.example.picturesurfer.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picturesurfer.data.api.models.DogImage
import com.example.picturesurfer.data.repository.DogRepository
import com.example.picturesurfer.data.repository.Resource
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = DogRepository()
    private val _dogs = MutableLiveData<Resource<List<DogImage>>>()
    val dogs: LiveData<Resource<List<DogImage>>> = _dogs
    private val _favorites = MutableLiveData<Set<String>>(emptySet())
    val favorites: LiveData<Set<String>> = _favorites

    init {
        loadDogs()
    }

    /**
     * Loads dog images from the repository
     * @param limit Number of images to load
     */
    fun loadDogs(limit: Int = 20) {
        _dogs.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.getRandomDogs(limit)

            result.onSuccess { dogList ->
                _dogs.value = Resource.Success(dogList)
            }

            result.onFailure { error ->
                _dogs.value = Resource.Error(
                    message = error.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Refreshes the dog list
     */
    fun refreshDogs() {
        loadDogs()
    }

    /**
     * Toggles a dog image as favorite
     * @param dogId The ID of the dog image
     */
    fun toggleFavorite(dogId: String) {
        val currentFavorites = _favorites.value ?: emptySet()
        _favorites.value = if (dogId in currentFavorites) {
            currentFavorites - dogId
        } else {
            currentFavorites + dogId
        }
    }

    /**
     * Checks if a dog is favorited
     */
    fun isFavorite(dogId: String): Boolean {
        return _favorites.value?.contains(dogId) == true
    }
}