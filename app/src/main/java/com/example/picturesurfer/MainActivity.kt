package com.example.picturesurfer

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.picturesurfer.databinding.ActivityMainBinding
import com.example.picturesurfer.data.api.models.DogImage
import com.example.picturesurfer.data.preferences.PreferencesManager
import com.example.picturesurfer.data.repository.Resource
import com.example.picturesurfer.ui.main.DogAdapter
import com.example.picturesurfer.ui.main.MainViewModel
import com.example.picturesurfer.ui.settings.SettingsActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: DogAdapter
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(this)

        lifecycleScope.launch {
            preferencesManager.themeFlow.collect { theme ->
                applyTheme(theme)
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        setupObservers()
        setupListeners()
        observeTextSize()
    }

    private fun applyTheme(theme: String) {
        when (theme) {
            PreferencesManager.THEME_LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            PreferencesManager.THEME_DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
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
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = this@MainActivity.adapter
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

    private fun setupObservers() {
        viewModel.dogs.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.errorText.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    resource.data?.let { dogs ->
                        adapter.submitList(dogs)
                        // Share dogs with FavoritesActivity
                        com.example.picturesurfer.ui.favorites.FavoritesActivity.allDogs = dogs
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = resource.message
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            preferencesManager.favoritesFlow.collect { favorites ->
                adapter.updateFavorites(favorites)
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

    private fun setupListeners() {
        binding.fabRefresh.setOnClickListener {
            viewModel.refreshDogs()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorites -> {
                startActivity(Intent(this, com.example.picturesurfer.ui.favorites.FavoritesActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}