package com.codecrew.app.audio.incomingcall


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azure.android.communication.calling.IncomingCall
import com.codecrew.app.audio.AcsCallUiState
import com.codecrew.app.audio.AcsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class IncomingCallViewModel(
    private val acsManager: AcsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncomingCallScreenState())
    val uiState: StateFlow<IncomingCallScreenState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            acsManager.callUiState.map { acsState ->
                transformAcsState(acsState)
            }.collect { screenState ->
                _uiState.value = screenState
            }
        }
    }

    private fun transformAcsState(acsState: AcsCallUiState): IncomingCallScreenState {
        val incomingCall: IncomingCall? = acsState.incomingCall
        return IncomingCallScreenState(
            callerName = incomingCall?.callerInfo?.displayName ?: "Unknown Caller",
            callerIdDetail = incomingCall?.callerInfo?.identifier?.rawId, // Or format as needed
            isCallAgentReady = acsState.isCallAgentReady,
            hasIncomingCall = incomingCall != null,
            errorMessage = if (!acsState.isCallAgentReady && incomingCall != null) "Call Agent not ready. Please wait." else acsState.callMessage
        )
    }

    fun acceptCall() {
        if (_uiState.value.hasIncomingCall && _uiState.value.isCallAgentReady) {
            // VideoOptions can be null for audio-only.
            // If you want to start with video, create VideoOptions here.
            acsManager.acceptIncomingCall(videoOptions = null)
            // Navigation to the main call screen will be handled based on AcsManager's state update
            // (i.e., when activeCall becomes non-null).
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = "Cannot accept call now.")
        }
    }

    fun rejectCall() {
        if (_uiState.value.hasIncomingCall) {
            acsManager.rejectIncomingCall()
            // The UI should react to hasIncomingCall becoming false (e.g., navigate back)
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = "No call to reject.")
        }
    }
}