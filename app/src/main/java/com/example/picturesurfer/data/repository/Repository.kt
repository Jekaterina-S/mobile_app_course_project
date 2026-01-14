package com.example.picturesurfer.data.repository

import com.example.picturesurfer.data.api.RetrofitClient
import com.example.picturesurfer.data.api.models.DogImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DogRepository {
    private val apiService = RetrofitClient.dogApiService

    /**
     * Fetches random dog images from the API
     * @param limit Number of images to fetch
     * @return Result containing list of dog images or error
     */
    suspend fun getRandomDogs(limit: Int = 10): Result<List<DogImage>> {
        return withContext(Dispatchers.IO) {
            try {
                val dogs = apiService.getRandomDogs(limit = limit)
                Result.success(dogs)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Refreshes the dog list - fetches new random dogs
     */
    suspend fun refreshDogs(limit: Int = 10): Result<List<DogImage>> {
        return getRandomDogs(limit)
    }
}