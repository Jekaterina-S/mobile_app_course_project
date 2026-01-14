package com.example.picturesurfer.ui.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.picturesurfer.R
import com.example.picturesurfer.databinding.ActivityImageDetailBinding
import com.example.picturesurfer.data.api.models.DogImage
import com.example.picturesurfer.data.api.models.Breed
import com.example.picturesurfer.data.preferences.PreferencesManager
import kotlinx.coroutines.launch

class ImageDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageDetailBinding
    private lateinit var preferencesManager: PreferencesManager
    private var currentDog: DogImage? = null
    private var isFavorite = false
    private var favoriteMenuItem: MenuItem? = null

    companion object {
        const val EXTRA_IMAGE_URL = "image_url"
        const val EXTRA_BREED_NAME = "breed_name"
        const val EXTRA_DOG_ID = "dog_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        val breedName = intent.getStringExtra(EXTRA_BREED_NAME) ?: "Dog"
        val dogId = intent.getStringExtra(EXTRA_DOG_ID) ?: imageUrl ?: ""

        currentDog = DogImage(
            id = dogId,
            url = imageUrl ?: "",
            width = 0,
            height = 0,
            breeds = if (breedName != "Dog") listOf(Breed(null, breedName)) else null
        )

        setupImage(imageUrl)
        setupBreedInfo(breedName)
        observeTextSize()
        observeFavoriteStatus()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        favoriteMenuItem = menu.findItem(R.id.action_favorite)
        updateFavoriteIcon()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorite -> {
                toggleFavorite()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupImage(imageUrl: String?) {
        binding.progressBar.visibility = View.VISIBLE

        binding.fullImage.load(imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_placeholder)
            error(R.drawable.ic_error)
            listener(
                onSuccess = { _, _ ->
                    binding.progressBar.visibility = View.GONE
                },
                onError = { _, _ ->
                    binding.progressBar.visibility = View.GONE
                }
            )
        }
    }

    private fun setupBreedInfo(breedName: String) {
        binding.breedName.text = breedName
    }

    private fun observeTextSize() {
        lifecycleScope.launch {
            preferencesManager.textSizeFlow.collect { textSize ->
                binding.breedName.textSize = textSize + 6f  // Slightly larger for detail view
            }
        }
    }

    private fun observeFavoriteStatus() {
        lifecycleScope.launch {
            preferencesManager.favoritesFlow.collect { favorites ->
                currentDog?.let { dog ->
                    isFavorite = dog.id in favorites
                    updateFavoriteIcon()
                }
            }
        }
    }

    private fun toggleFavorite() {
        currentDog?.let { dog ->
            lifecycleScope.launch {
                preferencesManager.toggleFavorite(dog)
            }
        }
    }

    private fun updateFavoriteIcon() {
        favoriteMenuItem?.setIcon(
            if (isFavorite) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}