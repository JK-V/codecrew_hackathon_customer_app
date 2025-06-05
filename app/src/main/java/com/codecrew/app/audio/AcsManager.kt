package com.codecrew.app.audio

import androidx.compose.foundation.layout.size
import androidx.lifecycle.get
import android.content.Context
import android.media.AudioManager // For speakerphone example
import android.util.Log
import com.azure.android.communication.calling.*
import com.azure.android.communication.common.CommunicationIdentifier
import com.azure.android.communication.common.CommunicationTokenCredential
import com.azure.android.communication.common.CommunicationUserIdentifier
// If you are identifying users by CommunicationUserIdentifier
// If you are using MicrosoftTeamsUserIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class AcsManager private constructor(private val applicationContext: Context) {

    private val _callUiState = MutableStateFlow(AcsCallUiState())
    val callUiState: StateFlow<AcsCallUiState> = _callUiState.asStateFlow()

    private val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var callAgentJob: Job? = null // To manage CallAgent creation coroutine
    lateinit var callAgent: CallAgent

    companion object {
        @Volatile
        private var INSTANCE: AcsManager? = null
        private const val TAG = "AcsManager"

        fun getInstance(context: Context): AcsManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AcsManager(context.applicationContext).also { INSTANCE = it }
            }
    }

    init {
        try {
            val callClientOptions = CallClientOptions()
            val callClient = CallClient(callClientOptions)
            _callUiState.value = _callUiState.value.copy(
                callClient = callClient,
                isSpeakerphoneOn = audioManager.isSpeakerphoneOn // Initialize speakerphone state
            )
            Log.i(TAG, "CallClient initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize CallClient: ${e.message}", e)
            _callUiState.value = _callUiState.value.copy(callMessage = "CallClient initialization failed.")
        }
    }



    // --- Token and Agent Creation ---
    fun createCallAgent(acsToken: String, fcmToken: String, displayName: String = "Android User") {
        callAgentJob?.cancel() // Cancel any ongoing agent creation
        callAgentJob = CoroutineScope(Dispatchers.Main).launch {
            val client = _callUiState.value.callClient
            if (client == null) {
                _callUiState.value = _callUiState.value.copy(callMessage = "CallClient not initialized.")
                Log.e(TAG, "CallClient not initialized. Cannot create CallAgent.")
                return@launch
            }
            if (_callUiState.value.isCallAgentReady) {
                Log.i(TAG, "CallAgent already created and ready.")
                return@launch
            }

            Log.i(TAG, "Attempting to create CallAgent...")
            try {
                val tokenCredential = CommunicationTokenCredential(acsToken)
                val callAgentOptions = CallAgentOptions().apply {
                    this.displayName = displayName
                    // this.extensionId = UUID.randomUUID() // Example if needing specific extension ID
                }

                val agent = withContext(Dispatchers.IO) {
                    client.createCallAgent(applicationContext, tokenCredential, callAgentOptions).get()
                }
                Log.i(TAG, "CallAgent created successfully.")
                agent.registerPushNotification(fcmToken)
                callAgent = agent
                val dm = withContext(Dispatchers.IO) { client.getDeviceManager(applicationContext).get() }
                setupDeviceManager(dm)

                agent.addOnCallsUpdatedListener { callsUpdatedEvent ->
                    handleCallsUpdated(callsUpdatedEvent)
                }
                agent.addOnIncomingCallListener { incomingCallEvent ->
                    handleIncomingCall(incomingCallEvent)
                }

                _callUiState.value = _callUiState.value.copy(
                    callAgent = agent,
                    isCallAgentReady = true,
                    callMessage = "ACS Ready"
                )

            } catch (e: Exception) {
                Log.e(TAG, "Failed to create CallAgent: ${e.message}", e)
                _callUiState.value = _callUiState.value.copy(
                    callMessage = "ACS Agent creation failed: ${e.message}",
                    isCallAgentReady = false
                )
            }
        }
    }

    private fun setupDeviceManager(manager: DeviceManager) {
        _callUiState.value = _callUiState.value.copy(deviceManager = manager)
       // Log.i(TAG, "DeviceManager setup. Microphones: ${manager.}, Speakers: ${manager.speakers.size}")
    }

    // --- Call Handling ---
    private fun handleIncomingCall(incomingCallEvent: IncomingCall) {
        Log.i(TAG, "Incoming call from: ${incomingCallEvent.callerInfo.displayName}, ID: ${incomingCallEvent.id}")
        _callUiState.value = _callUiState.value.copy(incomingCall = incomingCallEvent, activeCall = null)
        // Here, you'd trigger UI for incoming call (notification, specific screen)
    }

    private fun handleCallsUpdated(callsUpdatedEvent: CallsUpdatedEvent) {
        callsUpdatedEvent.addedCalls.forEach { call ->
            Log.i(TAG, "Call added. ID: ${call.id}, State: ${call.state}")
            if (_callUiState.value.activeCall == null || _callUiState.value.activeCall?.id != call.id) {
                clearCallListeners(_callUiState.value.activeCall) // Clean up old call listeners
                _callUiState.value = _callUiState.value.copy(
                    activeCall = call,
                    incomingCall = null, // Clear incoming call once a call is active
                    callStartTimeMillis = if(call.state == CallState.CONNECTED) System.currentTimeMillis() else 0L
                )
                subscribeToCallStateChanges(call)
            }
        }
        callsUpdatedEvent.removedCalls.forEach { call ->
            Log.i(TAG, "Call removed. ID: ${call.id}, State: ${call.state}")
            if (_callUiState.value.activeCall?.id == call.id) {
                clearCallListeners(call)
                _callUiState.value = _callUiState.value.copy(
                    activeCall = null,
                    callMessage = "Call ended.",
                    callStartTimeMillis = 0L
                )
            }
            if (_callUiState.value.incomingCall?.id == call.id) {
                _callUiState.value = _callUiState.value.copy(incomingCall = null, callMessage = "Incoming call missed or ended.")
            }
        }
    }

    private val callStateChangedListener = PropertyChangedListener {
        val call = _callUiState.value.activeCall
        if (call != null) {
            Log.i(TAG, "Call ${call.id} state changed to: ${call.state}")
            var startTime = _callUiState.value.callStartTimeMillis
            if (call.state == CallState.CONNECTED && startTime == 0L) {
                startTime = System.currentTimeMillis()
            } else if (call.state == CallState.DISCONNECTED || call.state == CallState.DISCONNECTING) {
                startTime = 0L // Reset if call is over
            }
            // Update the activeCall object and start time in state
            _callUiState.value = _callUiState.value.copy(activeCall = call, callStartTimeMillis = startTime)

            if (call.state == CallState.DISCONNECTED) {
                // The onCallsUpdated listener's removedCalls will also set activeCall to null.
                // Additional cleanup specific to disconnection can go here.
                _callUiState.value = _callUiState.value.copy(callMessage = "Call disconnected.")
            }
        }
    }

    private val isMutedChangedListener = PropertyChangedListener {
        val call = _callUiState.value.activeCall
        if (call != null) {
            Log.i(TAG, "Call ${call.id} isMuted changed to: ${call.isMuted}")
            // The ViewModel observes this through callUiState.activeCall.isMuted
            _callUiState.value = _callUiState.value.copy(activeCall = call) // Ensure state is updated
        }
    }

    private fun subscribeToCallStateChanges(call: Call) {
        call.addOnStateChangedListener(callStateChangedListener)
        call.addOnIsMutedChangedListener(isMutedChangedListener)
        // Add other listeners as needed (e.g., remote participants, recording status)
        Log.i(TAG, "Subscribed to state changes for call ${call.id}")
    }

    private fun clearCallListeners(call: Call?) {
        call?.removeOnStateChangedListener(callStateChangedListener)
        call?.removeOnIsMutedChangedListener(isMutedChangedListener)
        // Remove other listeners
        Log.i(TAG, "Cleared listeners for call ${call?.id}")
    }


    // --- Call Actions ---
    fun startCall(calleeId: String /* User ID or phone number */, videoOptions: VideoOptions? = null) {
        val agent = _callUiState.value.callAgent
        if (agent == null || !_callUiState.value.isCallAgentReady) {
            _callUiState.value = _callUiState.value.copy(callMessage = "CallAgent not ready.")
            Log.e(TAG, "CallAgent not ready to start a call.")
            return
        }
        Log.i(TAG, "Attempting to start call to: $calleeId")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Define participants based on the calleeId type
                // This is a simplified example. You'll need to parse calleeId
                // to create the correct CommunicationIdentifier type.
                val participants = listOf(createIdentifierFromRawId(calleeId)) // Implement createIdentifierFromRawId

                val startCallOptions = StartCallOptions().apply {
                    videoOptions?.let { this.videoOptions = it }
                    // audioOptions can also be set here if needed
                }
                withContext(Dispatchers.IO) {
                    agent.startCall(applicationContext, participants, startCallOptions)
                }
                // The onCallsUpdated listener will handle setting this as the active call
                Log.i(TAG, "Start call initiated to $calleeId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start call: ${e.message}", e)
                _callUiState.value = _callUiState.value.copy(callMessage = "Failed to start call: ${e.message}")
            }
        }
    }

    fun acceptIncomingCall(videoOptions: VideoOptions? = null) {
        val incoming = _callUiState.value.incomingCall
        if (incoming == null) {
            _callUiState.value = _callUiState.value.copy(callMessage = "No incoming call to accept.")
            Log.w(TAG, "No incoming call to accept.")
            return
        }
        Log.i(TAG, "Attempting to accept incoming call ID: ${incoming.id}")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val acceptCallOptions = AcceptCallOptions().apply {
                    videoOptions?.let { this.videoOptions = it }
                }
                val call = withContext(Dispatchers.IO) {
                    incoming.accept(applicationContext, acceptCallOptions).get()
                }
                Log.i(TAG, "Incoming call accepted. Call ID: ${call.id}")
                // onCallsUpdated listener should then set this as the active call.
                // We clear incomingCall here because it's now handled.
                _callUiState.value = _callUiState.value.copy(incomingCall = null)
                // Redundant if onCallsUpdated works perfectly, but good for immediate UI feedback.
                // _callUiState.value = _callUiState.value.copy(activeCall = call)
                // subscribeToCallStateChanges(call)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to accept call: ${e.message}", e)
                _callUiState.value = _callUiState.value.copy(callMessage = "Failed to accept call: ${e.message}")
            }
        }
    }

    fun rejectIncomingCall() {
        val incoming = _callUiState.value.incomingCall
        if (incoming == null) {
            _callUiState.value = _callUiState.value.copy(callMessage = "No incoming call to reject.")
            Log.w(TAG, "No incoming call to reject.")
            return
        }
        Log.i(TAG, "Attempting to reject incoming call ID: ${incoming.id}")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    incoming.reject().get()
                }
                Log.i(TAG, "Incoming call rejected.")
                _callUiState.value = _callUiState.value.copy(incomingCall = null, callMessage = "Call rejected.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reject call: ${e.message}", e)
                _callUiState.value = _callUiState.value.copy(callMessage = "Failed to reject call: ${e.message}")
            }
        }
    }

    fun hangUpCall() {
        val call = _callUiState.value.activeCall
        if (call == null || call.state == CallState.DISCONNECTED || call.state == CallState.DISCONNECTING) {
            _callUiState.value = _callUiState.value.copy(callMessage = "No active call to hang up.")
            Log.w(TAG, "No active call to hang up or call is already ending.")
            return
        }
        Log.i(TAG, "Attempting to hang up call ID: ${call.id}")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    call.hangUp(HangUpOptions()).get()
                }
                Log.i(TAG, "Call hang up initiated.")
                // State change will be handled by onStateChangedListener and onCallsUpdated
            } catch (e: Exception) {
                Log.e(TAG, "Failed to hang up call: ${e.message}", e)
                _callUiState.value = _callUiState.value.copy(callMessage = "Failed to hang up: ${e.message}")
            }
        }
    }

    fun toggleMute() { // This is now directly handled by AudioCallViewModel, but AcsManager could also do it
        val call = _callUiState.value.activeCall ?: return
        CoroutineScope(Dispatchers.Main).launch {
            try {
                if (call.isMuted) {
                    withContext(Dispatchers.IO) { call.unmute(applicationContext).get() }
                    Log.i(TAG, "Call unmuted.")
                } else {
                    withContext(Dispatchers.IO) { call.mute(applicationContext).get() }
                    Log.i(TAG, "Call muted.")
                }
                // isMutedChangedListener will update the state
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle mute: ${e.message}", e)
            }
        }
    }

    fun toggleSpeakerphone() {
        audioManager.isSpeakerphoneOn = !audioManager.isSpeakerphoneOn
        _callUiState.value = _callUiState.value.copy(isSpeakerphoneOn = audioManager.isSpeakerphoneOn)
        Log.i(TAG, "Speakerphone toggled: ${audioManager.isSpeakerphoneOn}")
    }


    // --- Utility (You MUST implement this based on your identifier types) ---
    private fun createIdentifierFromRawId(rawId: String): CommunicationIdentifier {
        // This is a placeholder. You need to determine the type of ID and create the correct identifier.
        // Examples:
        // if (rawId.startsWith("8:acs:")) {
        //     return CommunicationUserIdentifier(rawId)
        // } else if (rawId.startsWith("8:teamsvisitor:")) {
        //     return MicrosoftTeamsUserIdentifier(rawId).setCloud(CommunicationCloudEnvironment.PUBLIC) // Or GCCH, DOD
        // } else if (rawId.startsWith("+")) { // Example for phone numbers
        //     return PhoneNumberIdentifier(rawId)
        // }
        // For simplicity, assuming it's an ACS User ID if not specified otherwise
        Log.w(TAG, "createIdentifierFromRawId assuming CommunicationUserIdentifier for ID: $rawId. Adjust if needed.")
        return CommunicationUserIdentifier(rawId) // THIS IS A SIMPLIFIED DEFAULT
        // throw IllegalArgumentException("Unsupported identifier format: $rawId")
    }

    fun dispose() {
        Log.i(TAG, "Disposing AcsManager")
        callAgentJob?.cancel()
        _callUiState.value.activeCall?.let {
            try {
                it.hangUp(HangUpOptions()).get()
            } catch (e: Exception) { Log.e(TAG, "Error hanging up call on dispose: $e") }
            clearCallListeners(it)
        }
        _callUiState.value.callAgent?.dispose()
        _callUiState.value = AcsCallUiState() // Reset state
        INSTANCE = null // Clear singleton instance
        Log.i(TAG, "AcsManager disposed.")
    }
}