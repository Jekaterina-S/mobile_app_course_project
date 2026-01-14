package com.example.picturesurfer.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.picturesurfer.databinding.ActivityFavoritesBinding
import com.example.picturesurfer.data.api.models.DogImage
import com.example.picturesurfer.data.preferences.PreferencesManager
import com.example.picturesurfer.ui.main.DogAdapter
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: DogAdapter
    private lateinit var preferencesManager: PreferencesManager

    companion object {
        // Static storage for dogs - shared across activities
        var allDogs: List<DogImage> = emptyList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        observeData()
        observeTextSize()
    }

    private fun setupRecyclerView() {
        adapter = DogAdapter(
            onFavoriteClick = { dog ->
                lifecycleScope.launch {
                    preferencesManager.toggleFavorite(dog)
                }
            },
            onImageClick = { dog ->
                openImageDetail(dog)
            }
        )

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@FavoritesActivity, 2)
            adapter = this@FavoritesActivity.adapter
        }
    }

    private fun observeData() {
        // Observe favorites and update list
        lifecycleScope.launch {
            preferencesManager.favoritesFlow.collect { favoriteIds ->
                adapter.updateFavorites(favoriteIds)
                updateFavoritesList(favoriteIds)
            }
        }
    }

    private fun observeTextSize() {
        lifecycleScope.launch {
            preferencesManager.textSizeFlow.collect { textSize ->
                adapter.setTextSize(textSize)
            }
        }
    }

    private fun updateFavoritesList(favoriteIds: Set<String>) {
        val favoriteDogs = allDogs.filter { dog ->
            dog.id in favoriteIds
        }

        if (favoriteDogs.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            adapter.submitList(favoriteDogs)
        }
    }

    private fun openImageDetail(dog: DogImage) {
        val breedName = dog.breeds?.firstOrNull()?.name ?: "Dog"
        val intent = Intent(this, com.example.picturesurfer.ui.detail.ImageDetailActivity::class.java).apply {
            putExtra(com.example.picturesurfer.ui.detail.ImageDetailActivity.EXTRA_IMAGE_URL, dog.url)
            putExtra(com.example.picturesurfer.ui.detail.ImageDetailActivity.EXTRA_BREED_NAME, breedName)
            putExtra(com.example.picturesurfer.ui.detail.ImageDetailActivity.EXTRA_DOG_ID, dog.id)
        }
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}