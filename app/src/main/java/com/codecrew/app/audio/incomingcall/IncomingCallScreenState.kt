package com.codecrew.app.audio.incomingcall

data class IncomingCallScreenState(
    val callerName: String = "Unknown Caller",
    val callerIdDetail: String? = null, // e.g., phone number or ACS ID
    val isCallAgentReady: Boolean = false,
    val hasIncomingCall: Boolean = false,
    val errorMessage: String? = null
)