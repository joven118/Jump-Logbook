package com.V2Skydivejump.app.ui.jumper

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalLinkHubScreen(viewModel: DigitalLinkViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digital Link Hub") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { viewModel.startScan() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Scan")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            
            // Connection Status
            Surface(
                color = if (uiState.connectionStatus == "Connected") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (uiState.connectionStatus == "Connected") Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                        null,
                        tint = if (uiState.connectionStatus == "Connected") Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = uiState.connectionStatus,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.connectionStatus == "Connected") Color(0xFF2E7D32) else Color.Unspecified
                    )
                }
            }

            // Sync Options
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SyncOptionCard("Bluetooth", Icons.Default.Bluetooth, true, Modifier.weight(1f))
                SyncOptionCard("WiFi Sync", Icons.Default.Wifi, false, Modifier.weight(1f))
                SyncOptionCard("CSV Import", Icons.Default.UploadFile, true, Modifier.weight(1f))
            }

            Text(
                text = "Nearby Hardware",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (uiState.isScanning && uiState.foundDevices.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.foundDevices) { device ->
                    BleDeviceCard(device) {
                        viewModel.connectToDevice(device)
                    }
                }
            }
        }
    }

    // New Jump Sync Dialog
    if (uiState.pendingJump != null) {
        val jump = uiState.pendingJump!!
        AlertDialog(
            onDismissRequest = { viewModel.discardPendingJump() },
            title = { Text("New Jump Detected!") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Syncing data from your altimeter:", style = MaterialTheme.typography.bodySmall)
                    
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Jump #${jump.jumpNumber}", fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Spacer(Modifier.height(8.dp))
                            TelemetryRow("Exit Altitude", "${jump.exitAltitudeAgl} ft")
                            TelemetryRow("Deployment", "${jump.deploymentAltitudeAgl} ft")
                            TelemetryRow("Max Speed", "${jump.maxSpeedMph} mph")
                            TelemetryRow("FF Time", "${jump.freefallTimeSeconds} sec")
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.savePendingJump() }) { Text("Review & Save to Logbook") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.discardPendingJump() }) { Text("Discard") }
            }
        )
    }
}

@Composable
fun SyncOptionCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(containerColor = if (enabled) MaterialTheme.colorScheme.surface else Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray)
            Text(label, fontSize = 10.sp, color = if (enabled) Color.Unspecified else Color.Gray)
        }
    }
}

@Composable
fun TelemetryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BleDeviceCard(device: BleDevice, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = device.name ?: "Unknown Device", fontWeight = FontWeight.Bold)
                Text(text = device.address, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(text = "${device.rssi} dBm", color = MaterialTheme.colorScheme.secondary)
        }
    }
}
