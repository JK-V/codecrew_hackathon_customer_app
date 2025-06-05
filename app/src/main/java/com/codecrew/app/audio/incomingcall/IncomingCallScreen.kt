package com.codecrew.app.audio.incomingcall

import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Import your IncomingCallViewModel

@Composable
fun IncomingCallScreen(
    viewModel: IncomingCallViewModel,
    onCallAccepted: () -> Unit, // Navigate to the main AudioCallScreen
    onCallRejectedOrMissed: () -> Unit // Navigate back or close screen
) {
    val uiState by viewModel.uiState.collectAsState()

    // Effect to handle navigation when call is no longer incoming
    LaunchedEffect(uiState.hasIncomingCall) {
        if (!uiState.hasIncomingCall && uiState.errorMessage == null) { // Or based on a specific "rejected" signal
            onCallRejectedOrMissed()
        }
    }
    // Effect to handle navigation when call is accepted (and AcsManager signals an active call)
    // This part is tricky as 'onCallAccepted' implies the *intent* to accept.
    // The actual navigation should happen when AcsManager confirms the call is active.
    // For now, onCallAccepted is called immediately after user presses accept.
    // A more robust way is to observe acsManager.callUiState for activeCall becoming non-null.

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (!uiState.isCallAgentReady && uiState.hasIncomingCall) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Preparing for call...")
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                    uiState.errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
            }
        } else if (uiState.hasIncomingCall) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround // Distributes space
            ) {

                // Caller Information
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Incoming Call",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // You could add an Avatar here based on caller info
                    Text(
                        text = uiState.callerName,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    uiState.callerIdDetail?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CallActionButton(
                        icon = Icons.Filled.CallEnd,
                        backgroundColor = Color(0xFFE63946), // Red
                        contentDescription = "Decline Call",
                        label = "Decline",
                        onClick = {
                            viewModel.rejectCall()
                            // onCallRejectedOrMissed() will be triggered by LaunchedEffect
                        }
                    )
                    CallActionButton(
                        icon = Icons.Filled.Call,
                        backgroundColor = Color(0xFF2E7D32), // Green
                        contentDescription = "Accept Call",
                        label = "Accept",
                        onClick = {
                            viewModel.acceptCall()
                            onCallAccepted() // Navigate immediately to AudioCallScreen
                            // AudioCallScreen's ViewModel will pick up the active call from AcsManager
                        }
                    )
                }


            }
        } else {
            // Optional: Show a message if there's no incoming call but this screen is somehow reached
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.errorMessage ?: "No incoming call.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onCallRejectedOrMissed) { Text("Dismiss") }
                }
            }
        }
    }
}

@Composable
fun CallActionButton(
    icon: ImageVector,
    backgroundColor: Color,
    contentDescription: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Button(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.size(72.dp),
            colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}