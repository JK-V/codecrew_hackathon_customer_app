package com.codecrew.app.audio.incomingcall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codecrew.app.audio.AcsManager

class IncomingCallViewModelFactory(
    private val acsManager: AcsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncomingCallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IncomingCallViewModel(acsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for IncomingCallViewModelFactory")
    }
}