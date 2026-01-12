package com.example.picturesurfer.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.picturesurfer.R
import com.example.picturesurfer.data.api.models.DogImage
import com.example.picturesurfer.databinding.ItemPictureBinding

class PicAdapter(
    private val onFavoriteClick: (DogImage) -> Unit
) : ListAdapter<DogImage, PicAdapter.DogViewHolder>(DogDiffCallback()) {

    private val favorites = mutableSetOf<String>()
    private var textSize = 14f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val binding = ItemPictureBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateFavorites(newFavorites: Set<String>) {
        favorites.clear()
        favorites.addAll(newFavorites)
        notifyDataSetChanged()
    }

    fun setTextSize(size: Float) {
        textSize = size
        notifyDataSetChanged()
    }

    inner class DogViewHolder(
        private val binding: ItemPictureBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dog: DogImage) {
            // Load image with Coil
            binding.dogImage.load(dog.url) {
                crossfade(true)
                placeholder(R.drawable.ic_placeholder)
                error(R.drawable.ic_error)
            }

            // Set breed name
            val breedName = dog.breeds?.firstOrNull()?.name ?: "Unknown Breed"
            binding.breedName.text = breedName
            binding.breedName.textSize = textSize

            // Set favorite state
            val isFavorite = favorites.contains(dog.id)
            binding.favoriteButton.setImageResource(
                if (isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )

            // Handle favorite click
            binding.favoriteButton.setOnClickListener {
                onFavoriteClick(dog)
            }
        }
    }

    class DogDiffCallback : DiffUtil.ItemCallback<DogImage>() {
        override fun areItemsTheSame(oldItem: DogImage, newItem: DogImage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DogImage, newItem: DogImage): Boolean {
            return oldItem == newItem
        }
    }
}