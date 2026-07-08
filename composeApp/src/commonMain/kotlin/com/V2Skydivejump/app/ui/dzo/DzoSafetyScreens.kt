package com.V2Skydivejump.app.ui.dzo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.database.entities.IncidentReportEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyComplianceModule(
    uiState: DzoUiState,
    onAddIncident: (IncidentReportEntity) -> Unit,
    onBack: () -> Unit
) {
    var showAddIncident by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety & Compliance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddIncident = true }) {
                Icon(Icons.Default.AddAlert, contentDescription = "Report Incident")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            SafetyMetricsOverview(uiState)
            
            Text(
                "Incident Logs (${uiState.incidentReports.size})", 
                modifier = Modifier.padding(16.dp), 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                if (uiState.incidentReports.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No incidents logged yet.", color = Color.Gray)
                        }
                    }
                }
                
                items(uiState.incidentReports) { incident ->
                    IncidentCard(incident)
                }
            }
        }
    }

    if (showAddIncident) {
        AddIncidentReportDialog(
            dzId = uiState.user?.userId ?: "",
            onDismiss = { showAddIncident = false },
            onConfirm = { onAddIncident(it) }
        )
    }
}

@Composable
fun SafetyMetricsOverview(uiState: DzoUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Operational Safety Index", fontWeight = FontWeight.Bold)
                Text("98.5% incident-free month", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun IncidentCard(incident: IncidentReportEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(
                    color = when(incident.type) {
                        "MALFUNCTION" -> Color(0xFFD32F2F)
                        "OFF_FIELD" -> Color(0xFFFFA000)
                        else -> Color.Gray
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(incident.type, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                
                if (incident.isInvestigated) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF388E3C), modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Text(incident.description, style = MaterialTheme.typography.bodySmall)
            
            Spacer(Modifier.height(8.dp))
            Text(
                "Logged: ${com.V2Skydivejump.app.TimeUtils.formatEpochMillis(incident.date)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            
            if (incident.isInvestigated) {
                Spacer(Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(8.dp)) {
                    Text("SO Review: ${incident.investigationNotes}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun AddIncidentReportDialog(
    dzId: String,
    onDismiss: () -> Unit,
    onConfirm: (IncidentReportEntity) -> Unit
) {
    var type by remember { mutableStateOf("MALFUNCTION") }
    var description by remember { mutableStateOf("") }
    var expandedType by remember { mutableStateOf(false) }
    
    val types = listOf("MALFUNCTION", "OFF_FIELD", "GEAR_ISSUE", "INJURY", "OTHER")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Incident Report") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Incident Type") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth().clickable { expandedType = true }
                    )
                    DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                        types.forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { type = t; expandedType = false })
                        }
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Event Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (description.isNotBlank()) {
                    onConfirm(IncidentReportEntity(
                        dzId = dzId,
                        userId = "current_staff", // Real app: current user
                        date = com.V2Skydivejump.app.TimeUtils.nowEpochMillis(),
                        type = type,
                        description = description
                    ))
                    onDismiss()
                }
            }) { Text("Submit Report") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
