package com.codecrew.app.audio


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AudioCallViewModelFactory(
    private val applicationContext: Context,
    private val acsManager: AcsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioCallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioCallViewModel(applicationContext, acsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for AudioCallViewModelFactory")
    }
}