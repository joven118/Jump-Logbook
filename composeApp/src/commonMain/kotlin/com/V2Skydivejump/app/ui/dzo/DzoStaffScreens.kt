package com.V2Skydivejump.app.ui.dzo

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun StaffDashboardScreen(
    uiState: DzoUiState,
    onUpdateLoadStatus: (Int, LoadStatus) -> Unit,
    onVerifyStudent: (String, String) -> Unit = { _, _ -> },
    onManageSeal: () -> Unit = {},
    onOpenRepackQueue: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {},
    onNavigateToSafety: () -> Unit = {},
    onNavigateToFacilities: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {}
) {
    val userRole = uiState.user?.role ?: "JUMPER"
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Staff Duty Terminal", 
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Role: ${userRole.replace("_", " ")}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (userRole == "PILOT" || userRole == "DZ_OPERATOR" || userRole == "DZ_OWNER" || userRole == "DZ_MANAGER") { // DZO/Management can see everything
            PilotOpsSection(uiState, onUpdateLoadStatus)
        } 
        
        if (userRole == "INSTRUCTOR" || userRole == "DZ_OPERATOR" || userRole == "DZ_OWNER" || userRole == "DZ_MANAGER") {
            if (userRole != "INSTRUCTOR") Spacer(modifier = Modifier.height(32.dp))
            InstructorOpsSection(uiState, onVerifyStudent)
        }

        if (userRole == "SAFETY_OFFICER" || userRole == "DZ_OPERATOR" || userRole == "DZ_OWNER") {
            Spacer(modifier = Modifier.height(32.dp))
            SafetyOfficerOpsSection(uiState, onNavigateToSafety)
        }
        
        if (userRole == "RIGGER" || userRole == "DZ_OPERATOR") {
            Spacer(modifier = Modifier.height(32.dp))
            RiggerOpsSection(uiState, onManageSeal, onOpenRepackQueue)
        }

        if (userRole == "FINANCE_OFFICER" || userRole == "DZ_OPERATOR" || userRole == "DZ_OWNER") {
            Spacer(modifier = Modifier.height(32.dp))
            FinanceOfficerOpsSection(uiState, onNavigateToFinance)
        }

        if (userRole == "ADMIN_OFFICER" || userRole == "DZ_OPERATOR" || userRole == "DZ_OWNER" || userRole == "DZ_MANAGER") {
            Spacer(modifier = Modifier.height(32.dp))
            AdminOfficerOpsSection(uiState, onNavigateToFacilities, onNavigateToInventory)
        }
        
        if (userRole != "PILOT" && userRole != "INSTRUCTOR" && userRole != "DZ_OPERATOR" && userRole != "SAFETY_OFFICER" && userRole != "RIGGER" && userRole != "FINANCE_OFFICER" && userRole != "ADMIN_OFFICER") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Select a duty from the manifest or contact DZO.")
            }
        }
    }
}

@Composable
fun PilotOpsSection(
    uiState: DzoUiState,
    onUpdateLoadStatus: (Int, LoadStatus) -> Unit
) {
    Text("Active Flight Duty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    
    val pilotLoads = uiState.activeLoads.filter { it.status != LoadStatus.LANDED }
    
    if (pilotLoads.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No active loads assigned.", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pilotLoads) { load ->
                PilotLoadCard(load, onUpdateLoadStatus)
            }
        }
    }
}

@Composable
fun PilotLoadCard(
    load: FlightLoad,
    onUpdateLoadStatus: (Int, LoadStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Load #${load.id}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text("${load.aircraft?.name} (${load.aircraft?.serialNumber})", style = MaterialTheme.typography.labelSmall)
                }
                Surface(
                    color = when(load.status) {
                        LoadStatus.PLANNING -> Color.Gray
                        LoadStatus.BOARDING -> MaterialTheme.colorScheme.primary
                        LoadStatus.TAXIING -> Color(0xFFFFA000)
                        LoadStatus.IN_FLIGHT -> Color(0xFF1976D2)
                        LoadStatus.LANDED -> Color(0xFF388E3C)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(load.status.name, modifier = Modifier.padding(4.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (load.status == LoadStatus.BOARDING || load.status == LoadStatus.PLANNING) {
                    Button(
                        onClick = { onUpdateLoadStatus(load.id, LoadStatus.TAXIING) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FlightTakeoff, null)
                        Spacer(Modifier.width(8.dp))
                        Text("TAXI")
                    }
                }
                
                if (load.status == LoadStatus.TAXIING) {
                    Button(
                        onClick = { onUpdateLoadStatus(load.id, LoadStatus.IN_FLIGHT) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Icon(Icons.Default.CloudUpload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("TAKEOFF")
                    }
                }

                if (load.status == LoadStatus.IN_FLIGHT) {
                    Button(
                        onClick = { onUpdateLoadStatus(load.id, LoadStatus.LANDED) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                    ) {
                        Icon(Icons.Default.FlightLand, null)
                        Spacer(Modifier.width(8.dp))
                        Text("LANDED")
                    }
                }
            }
        }
    }
}

@Composable
fun InstructorOpsSection(uiState: DzoUiState, onVerifyStudent: (String, String) -> Unit = { _, _ -> }) {
    Text("Assigned Students", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    
    val students = uiState.unmanifestedJumpers.filter { 
        it.jumpType == JumpType.AFF_STUDENT || it.jumpType == JumpType.TANDEM_STUDENT 
    }
    
    if (students.isEmpty()) {
        Text("No students assigned today.", color = Color.Gray)
    } else {
        LazyColumn(
            modifier = Modifier.heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(students) { student ->
                StudentStatusRow(student, onVerifyStudent)
            }
        }
    }
}

@Composable
fun StudentStatusRow(student: ManifestJumper, onVerifyClick: (String, String) -> Unit = { _, _ -> }) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onVerifyClick(student.id, student.name) }
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Text(student.name.take(1))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, fontWeight = FontWeight.Bold)
                Text(student.jumpType.name, style = MaterialTheme.typography.labelSmall)
            }
            ReadinessBadge(student.requirements.overallStatus)
        }
    }
}

@Composable
fun SafetyOfficerOpsSection(uiState: DzoUiState, onNavigateToSafety: () -> Unit = {}) {
    Text("Safety & Compliance Duty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, null, tint = Color.Red)
                Spacer(Modifier.width(8.dp))
                Text("Incident Investigation Queue", fontWeight = FontWeight.Bold)
            }
            Text("No pending incidents to review.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToSafety, modifier = Modifier.fillMaxWidth()) {
                Text("Open Safety Dashboard")
            }
        }
    }
}

@Composable
fun RiggerOpsSection(uiState: DzoUiState, onManageSeal: () -> Unit = {}, onOpenQueue: () -> Unit = {}) {
    Text("Rigging Service Duty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Build, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Repack & Gear Queue", fontWeight = FontWeight.Bold)
            }
            Text("${uiState.user?.riggerSealSymbol ?: "NO SEAL"} | 5 rigs due for repack.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenQueue, modifier = Modifier.weight(1f)) {
                    Text("Repack Logs", fontSize = 10.sp)
                }
                OutlinedButton(onClick = onManageSeal, modifier = Modifier.weight(1f)) {
                    Text("Manage Seal", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun FinanceOfficerOpsSection(uiState: DzoUiState, onNavigateToFinance: () -> Unit = {}) {
    Text("Financial Management Duty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalance, null, tint = Color(0xFF2E7D32))
                Spacer(Modifier.width(8.dp))
                Text("Treasury & Revenue", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Revenue Today:", style = MaterialTheme.typography.bodySmall)
                Text(uiState.totalRevenue, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToFinance, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                Text("Open CFO Dashboard")
            }
        }
    }
}

@Composable
fun AdminOfficerOpsSection(uiState: DzoUiState, onNavigateToFacilities: () -> Unit = {}, onNavigateToInventory: () -> Unit = {}) {
    Text("General Administration Duty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(8.dp))
                Text("Facilities & General Tasks", fontWeight = FontWeight.Bold)
            }
            Text("Assigned to: Maintenance, Inventory Audit, and Staff Schedules.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onNavigateToFacilities, modifier = Modifier.weight(1f)) {
                    Text("Facilities", fontSize = 10.sp)
                }
                OutlinedButton(onClick = onNavigateToInventory, modifier = Modifier.weight(1f)) {
                    Text("Inventory", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun BoardingScannerScreen(
    uiState: DzoUiState,
    onBoardJumper: (ManifestJumper, Int) -> Unit,
    onBack: () -> Unit
) {
    var selectedMode by remember { mutableIntStateOf(0) } // 0: QR, 1: NFC
    var scannedInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Boarding Pass Scanner", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            
            TabRow(selectedTabIndex = selectedMode, modifier = Modifier.padding(vertical = 16.dp)) {
                Tab(selected = selectedMode == 0, onClick = { selectedMode = 0 }, text = { Text("QR CODE") })
                Tab(selected = selectedMode == 1, onClick = { selectedMode = 1 }, text = { Text("NFC TAG") })
            }

            if (selectedMode == 0) {
                Text("Scan Jumper QR Code to Board", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier.size(200.dp).background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                }
            } else {
                Text("Tap NFC Wristband/Helmet to Board", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier.size(200.dp).background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Contactless, null, modifier = Modifier.size(80.dp), tint = Color(0xFF388E3C).copy(alpha = 0.5f))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("SIMULATED ${if(selectedMode == 0) "SCAN" else "NFC TAP"}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = scannedInput,
                onValueChange = { scannedInput = it },
                label = { Text(if(selectedMode == 0) "Jumper Name (Mock Scan)" else "NFC Serial (Mock Tap)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    var found = false
                    uiState.activeLoads.forEach { load ->
                        val jumper = if (selectedMode == 0) {
                            load.jumpers.find { it.name.contains(scannedInput, ignoreCase = true) && !it.isCheckedIn }
                        } else {
                            // In real app, we'd lookup user by nfcTagId
                            // For mock, we'll check name or a mock serial
                            load.jumpers.find { (it.name.contains(scannedInput, ignoreCase = true) || scannedInput == "V2_NFC_123") && !it.isCheckedIn }
                        }
                        
                        if (jumper != null) {
                            onBoardJumper(jumper, load.id)
                            scope.launch {
                                snackbarHostState.showSnackbar("Boarded ${jumper.name} via ${if(selectedMode==0) "QR" else "NFC"} on Load #${load.id}")
                            }
                            scannedInput = ""
                            found = true
                            return@Button
                        }
                    }
                    if (!found) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Jumper not found on any active load.")
                        }
                    }
                },
                enabled = scannedInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if(selectedMode == 0) MaterialTheme.colorScheme.primary else Color(0xFF388E3C))
            ) {
                Text(if(selectedMode == 0) "PROCESS QR BOARDING" else "PROCESS NFC BOARDING")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(onClick = onBack) {
                Text("Return to Duty Terminal")
            }
        }
    }
}
