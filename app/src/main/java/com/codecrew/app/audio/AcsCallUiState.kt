package com.codecrew.app.audio


import android.content.Context
import android.media.AudioManager // For speakerphone example
import android.util.Log
import com.azure.android.communication.calling.*
import com.azure.android.communication.common.CommunicationTokenCredential
// If you are identifying users by CommunicationUserIdentifier
// import com.azure.android.communication.common.CommunicationUserIdentifier
// If you are using MicrosoftTeamsUserIdentifier
// import com.azure.android.communication.common.MicrosoftTeamsUserIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AcsCallUiState(
    val callClient: CallClient? = null,
    val callAgent: CallAgent? = null,
    val deviceManager: DeviceManager? = null,
    val incomingCall: IncomingCall? = null,
    val activeCall: Call? = null,
    val callMessage: String? = null,
    val isCallAgentReady: Boolean = false,
    val isSpeakerphoneOn: Boolean = false, // For speakerphone state
    val callStartTimeMillis: Long = 0L, // To help ViewModel calculate duration
    // Add other states as needed: e.g., currentAcsToken, acsUserId, isRecordingActive
)