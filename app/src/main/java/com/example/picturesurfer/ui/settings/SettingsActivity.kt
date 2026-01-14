package com.example.picturesurfer.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.picturesurfer.databinding.ActivitySettingsBinding
import com.example.picturesurfer.data.preferences.PreferencesManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupThemeSelection()
        setupTextSizeSlider()
        observeSettings()
    }

    private fun setupThemeSelection() {
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                binding.lightThemeRadio.id -> PreferencesManager.THEME_LIGHT
                binding.darkThemeRadio.id -> PreferencesManager.THEME_DARK
                else -> PreferencesManager.THEME_LIGHT
            }

            lifecycleScope.launch {
                preferencesManager.saveTheme(theme)
                applyTheme(theme)
            }
        }
    }

    private fun setupTextSizeSlider() {
        binding.textSizeSlider.addOnChangeListener { _, value, _ ->
            binding.textSizeLabel.text = "Font Size: ${value.toInt()}sp"
            binding.previewText.textSize = value

            lifecycleScope.launch {
                preferencesManager.saveTextSize(value)
            }
        }
    }

    private fun observeSettings() {
        // Observe theme
        lifecycleScope.launch {
            preferencesManager.themeFlow.collect { theme ->
                when (theme) {
                    PreferencesManager.THEME_LIGHT -> {
                        binding.lightThemeRadio.isChecked = true
                    }
                    PreferencesManager.THEME_DARK -> {
                        binding.darkThemeRadio.isChecked = true
                    }
                }
            }
        }

        // Observe text size
        lifecycleScope.launch {
            preferencesManager.textSizeFlow.collect { size ->
                binding.textSizeSlider.value = size
                binding.textSizeLabel.text = "Font Size: ${size.toInt()}sp"
                binding.previewText.textSize = size
            }
        }

        // Observe favorites count
        lifecycleScope.launch {
            preferencesManager.favoriteDogsFlow.collect { favoriteDogs ->
                binding.favoritesCount.text = "Favorite Dogs: ${favoriteDogs.size}"
            }
        }
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}