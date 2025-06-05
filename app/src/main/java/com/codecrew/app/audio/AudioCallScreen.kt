package com.codecrew.app.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp // Speakerphone On
import androidx.compose.material.icons.filled.PhoneInTalk // Speakerphone Off (Earpiece)
// Or use: import androidx.compose.material.icons.filled.VolumeOff // For speaker off if you prefer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

// Assuming AudioCallViewModelFactory if you need to pass AcsManager instance
// For simplicity, this example might not show factory usage directly in preview
// but you'll need it in your actual navigation graph.

@Composable
fun AudioCallScreen(
    viewModel: AudioCallViewModel, // Obtain this via Hilt, Koin, or a ViewModelProvider.Factory
    onCallEnded: () -> Unit // Callback to navigate away when call ends
) {
    val uiState by viewModel.uiState.collectAsState()

    // Effect to trigger navigation when call has truly ended
    LaunchedEffect(uiState.showHangUpButton) {
        if (!uiState.showHangUpButton && uiState.callStatusText == "Call Ended") {
            // Add a small delay for user to see "Call Ended" message
            delay(1500)
            onCallEnded()
        }
    }

    AudioCallScreenContent(
        callerName = uiState.callerName,
        callStatusText = uiState.callStatusText,
        callDurationSeconds = uiState.callDurationSeconds,
        isMicrophoneMuted = uiState.isMicrophoneMuted,
        isSpeakerphoneOn = uiState.isSpeakerphoneOn,
        showHangUpButton = uiState.showHangUpButton,
        onMuteToggle = { viewModel.toggleMute() },
        onSpeakerToggle = { viewModel.toggleSpeakerphone() },
        onHangUp = { viewModel.hangUpCall() }
    )
}

@Composable
fun AudioCallScreenContent(
    callerName: String,
    callStatusText: String,
    callDurationSeconds: Long,
    isMicrophoneMuted: Boolean,
    isSpeakerphoneOn: Boolean,
    showHangUpButton: Boolean,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onHangUp: () -> Unit
) {
    val callDurationFormatted = formatDuration(callDurationSeconds)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Or a custom call screen background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Pushes controls to bottom
        ) {
            // Caller Info Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f) // Takes up available space pushing controls down
                    .padding(top = 60.dp)
            ) {
                Text(
                    text = callerName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (callDurationSeconds > 0 && callStatusText == "Connected") callDurationFormatted else callStatusText,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Controls Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CallControlButton(
                    icon = if (isMicrophoneMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    description = if (isMicrophoneMuted) "Unmute" else "Mute",
                    backgroundColor = if (isMicrophoneMuted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = if (isMicrophoneMuted) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onMuteToggle
                )

                CallControlButton(
                    icon = if (isSpeakerphoneOn) Icons.Filled.VolumeUp else Icons.Filled.PhoneInTalk, // Or Icons.Filled.VolumeOff
                    description = if (isSpeakerphoneOn) "Turn Speaker Off" else "Turn Speaker On",
                    backgroundColor = if (isSpeakerphoneOn) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = if (isSpeakerphoneOn) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onSpeakerToggle
                )

                if (showHangUpButton) {
                    CallControlButton(
                        icon = Icons.Filled.CallEnd,
                        description = "Hang Up",
                        backgroundColor = Color(0xFFE63946), // Red color for hang up
                        iconColor = Color.White,
                        onClick = onHangUp,
                        size = 72.dp // Make hangup button slightly larger
                    )
                }
            }
        }
    }
}

@Composable
fun CallControlButton(
    icon: ImageVector,
    description: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 56.dp
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = iconColor
        ),
        contentPadding = PaddingValues(0.dp) // Remove default padding
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(size * 0.5f) // Icon size relative to button
        )
    }
}

fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}


// --- Preview ---
@Preview(showBackground = true)
@Composable
fun PreviewAudioCallScreenContentRinging() {
    MaterialTheme { // Wrap with your app's theme or MaterialTheme
        AudioCallScreenContent(
            callerName = "Jane Doe",
            callStatusText = "Ringing...",
            callDurationSeconds = 0,
            isMicrophoneMuted = false,
            isSpeakerphoneOn = false,
            showHangUpButton = true,
            onMuteToggle = {},
            onSpeakerToggle = {},
            onHangUp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAudioCallScreenContentConnected() {
    MaterialTheme {
        AudioCallScreenContent(
            callerName = "John Appleseed",
            callStatusText = "Connected",
            callDurationSeconds = 125, // 2 minutes 5 seconds
            isMicrophoneMuted = true,
            isSpeakerphoneOn = true,
            showHangUpButton = true,
            onMuteToggle = {},
            onSpeakerToggle = {},
            onHangUp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAudioCallScreenContentEnded() {
    MaterialTheme {
        AudioCallScreenContent(
            callerName = "Service",
            callStatusText = "Call Ended",
            callDurationSeconds = 300,
            isMicrophoneMuted = false,
            isSpeakerphoneOn = false,
            showHangUpButton = false, // Hang up button hidden
            onMuteToggle = {},
            onSpeakerToggle = {},
            onHangUp = {}
        )
    }
}