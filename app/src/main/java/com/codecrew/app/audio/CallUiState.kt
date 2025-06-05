package com.codecrew.app.audio

import android.telecom.Call

data class CallUiState(
    val activeCall: Call? = null,
    val isMicrophoneMuted: Boolean = false, // Derived from activeCall.isMuted
    val isSpeakerphoneOn: Boolean = false, // You'll manage this state
    val callMessage: String? = null,
    val callStartTimeMillis: Long = 0L // When the call became CONNECTED
    // ... other states like incomingCall, etc.
)