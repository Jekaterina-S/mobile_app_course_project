package com.example.picturesurfer.data.api.models

import com.google.gson.annotations.SerializedName

data class DogImage(
    @SerializedName("id")
    val id: String,

    @SerializedName("url")
    val url: String,

    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int,

    @SerializedName("breeds")
    val breeds: List<Breed>? = null
)

data class Breed(
    @SerializedName("name")
    val name: String,

    @SerializedName("temperament")
    val temperament: String? = null
)