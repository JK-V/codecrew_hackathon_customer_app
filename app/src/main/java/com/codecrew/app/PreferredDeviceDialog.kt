package com.codecrew.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreferredDeviceDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    var isPreferredDevice by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Preferred Device") },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isPreferredDevice,
                    onCheckedChange = { isPreferredDevice = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Make this my preferred device?")
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(isPreferredDevice) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}