package com.example.picturesurfer.data.api

import com.example.picturesurfer.data.api.models.DogImage
import retrofit2.http.GET
import retrofit2.http.Query

interface DogApiService {
    @GET("v1/images/search")
    suspend fun getRandomDogs(
        @Query("limit") limit: Int = 10,
        @Query("has_breeds") hasBreeds: Int = 1
    ): List<DogImage>
}