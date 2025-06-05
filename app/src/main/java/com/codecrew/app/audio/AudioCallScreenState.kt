package com.codecrew.app.audio

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.get
import androidx.lifecycle.viewModelScope
import com.azure.android.communication.calling.Call
import com.azure.android.communication.calling.CallState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.text.firstOrNull
import kotlin.text.isNotBlank

// Simplified CallUiState for this screen
data class AudioCallScreenState(
    val callerName: String = "Unknown Caller",
    val callStatusText: String = "Connecting...",
    val callDurationSeconds: Long = 0L,
    val isMicrophoneMuted: Boolean = false,
    val isSpeakerphoneOn: Boolean = false,
    val showHangUpButton: Boolean = true // Can be false if call is already ended
)

class AudioCallViewModel(
    private val applicationContext: Context, // For AudioManager
    private val acsManager: AcsManager // Your existing AcsManager instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioCallScreenState())
    val uiState: StateFlow<AudioCallScreenState> = _uiState.asStateFlow()

    private val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var callDurationJob: Job? = null
    private var callStartTimeMillis: Long = 0L

    init {
        // Initialize speakerphone state from AudioManager
        _uiState.value = _uiState.value.copy(isSpeakerphoneOn = audioManager.isSpeakerphoneOn)

        viewModelScope.launch {
            acsManager.callUiState.collectLatest { acsCallState ->
                val activeCall = acsCallState.activeCall
                if (activeCall != null) {
                    val remoteParticipant = activeCall.remoteParticipants.firstOrNull()
                    val callerDisplayName = remoteParticipant?.displayName?.takeIf { it.isNotBlank() } ?: "Participant"

                    _uiState.value = _uiState.value.copy(
                        callerName = callerDisplayName,
                        isMicrophoneMuted = activeCall.isMuted, // Get mute state from ACS Call object
                        callStatusText = getCallStatusString(activeCall.state)
                    )

                    when (activeCall.state) {
                        CallState.CONNECTED -> {
                            if (callStartTimeMillis == 0L) { // Start timer only once when connected
                                callStartTimeMillis = System.currentTimeMillis()
                                startCallDurationTimer()
                            }
                        }
                        CallState.DISCONNECTED, CallState.DISCONNECTING -> {
                            stopCallDurationTimer()
                            callStartTimeMillis = 0L // Reset start time
                            _uiState.value = _uiState.value.copy(showHangUpButton = false)
                            // Optionally add a delay before navigating away or finishing
                        }
                        else -> {
                            // Handle other states like None, EarlyMedia, Connecting, LocalHold, RemoteHold
                        }
                    }
                } else {
                    // No active call
                    stopCallDurationTimer()
                    callStartTimeMillis = 0L
                    _uiState.value = _uiState.value.copy(
                        callerName = "No Call",
                        callStatusText = "Call Ended",
                        callDurationSeconds = 0L,
                        showHangUpButton = false
                    )
                }
            }
        }
    }

    private fun getCallStatusString(state: CallState): String {
        return when (state) {
            CallState.NONE -> "Idle"
            CallState.CONNECTING -> "Connecting..."
            CallState.RINGING -> "Ringing..."
            CallState.CONNECTED -> "Connected"
            CallState.LOCAL_HOLD -> "On Hold (Local)"
            CallState.REMOTE_HOLD -> "On Hold (Remote)"
            CallState.DISCONNECTING -> "Disconnecting..."
            CallState.DISCONNECTED -> "Call Ended"
            CallState.EARLY_MEDIA -> "Early Media"
            else -> "Unknown State"
        }
    }

    private fun startCallDurationTimer() {
        callDurationJob?.cancel() // Cancel any existing timer
        callDurationJob = viewModelScope.launch {
            while (true) {
                if (callStartTimeMillis > 0) {
                    val duration = (System.currentTimeMillis() - callStartTimeMillis) / 1000
                    _uiState.value = _uiState.value.copy(callDurationSeconds = duration)
                }
                delay(1000) // Update every second
            }
        }
    }

    private fun stopCallDurationTimer() {
        callDurationJob?.cancel()
    }

    fun toggleMute() {
        viewModelScope.launch {
            val call = acsManager.callUiState.value.activeCall
            if (call != null) {
                try {
                    if (call.isMuted) {
                        //call.unmute().get() // Blocking, consider async if it causes UI jank
                    } else {
                       // call.mute().get()
                    }
                    // ACS SDK will update its state, and our collector will update isMicrophoneMuted
                } catch (e: Exception) {
                    Log.e("AudioCallVM", "Failed to toggle mute: ${e.message}")
                    // Handle error, maybe update UI with a message
                }
            }
        }
    }

    fun toggleSpeakerphone() {
        audioManager.isSpeakerphoneOn = !audioManager.isSpeakerphoneOn
        _uiState.value = _uiState.value.copy(isSpeakerphoneOn = audioManager.isSpeakerphoneOn)
        Log.i("AudioCallVM", "Speakerphone toggled: ${audioManager.isSpeakerphoneOn}")
    }

    fun hangUpCall() {
        viewModelScope.launch {
            acsManager.hangUpCall() // This should update acsManager.callUiState
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopCallDurationTimer()
        // Reset AudioManager mode if it was changed by AcsManager
        // audioManager.mode = AudioManager.MODE_NORMAL
        // audioManager.isSpeakerphoneOn = false // Or restore previous state
    }
}