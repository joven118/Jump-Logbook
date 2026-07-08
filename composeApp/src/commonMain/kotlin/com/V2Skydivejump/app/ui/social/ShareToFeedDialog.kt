package com.V2Skydivejump.app.ui.social

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.database.entities.JumpLogEntity

data class ShareSelection(
    val includeLocation: Boolean = true,
    val includeAltitude: Boolean = true,
    val includeStats: Boolean = true,
    val includeGear: Boolean = false,
    val includeNotes: Boolean = false,
    val includeDisciplines: Boolean = false,
    val includeLandingStyle: Boolean = false,
    val includeJumpType: Boolean = false,
    val includeAircraft: Boolean = false,
    val includeMedia: Boolean = true
)

@Composable
fun ShareToFeedDialog(
    jump: JumpLogEntity,
    onConfirm: (ShareSelection) -> Unit,
    onDismiss: () -> Unit
) {
    var selection by remember { mutableStateOf(ShareSelection()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Jump #${jump.jumpNumber}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Select details to share in the global feed:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                ShareToggle("Location & DZ Name", selection.includeLocation) { selection = selection.copy(includeLocation = it) }
                ShareToggle("Exit & Deploy Altitude", selection.includeAltitude) { selection = selection.copy(includeAltitude = it) }
                ShareToggle("Freefall Time & Speed", selection.includeStats) { selection = selection.copy(includeStats = it) }
                ShareToggle("Disciplines", selection.includeDisciplines) { selection = selection.copy(includeDisciplines = it) }
                ShareToggle("Landing Style", selection.includeLandingStyle) { selection = selection.copy(includeLandingStyle = it) }
                ShareToggle("Type of Jump", selection.includeJumpType) { selection = selection.copy(includeJumpType = it) }
                ShareToggle("Aircraft Type", selection.includeAircraft) { selection = selection.copy(includeAircraft = it) }
                ShareToggle("Equipment Used (ID)", selection.includeGear) { selection = selection.copy(includeGear = it) }
                ShareToggle("Photos & Videos", selection.includeMedia) { selection = selection.copy(includeMedia = it) }
                ShareToggle("Personal Notes", selection.includeNotes) { selection = selection.copy(includeNotes = it) }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selection) }) {
                Text("Share Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ShareToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
