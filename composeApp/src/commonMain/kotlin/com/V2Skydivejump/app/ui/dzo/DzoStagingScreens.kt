package com.V2Skydivejump.app.ui.dzo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun StagingAreaManagementModule(
    uiState: DzoUiState,
    onManifestJumper: (ManifestJumper, Int) -> Unit = { _, _ -> }
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedJumperForManifest by remember { mutableStateOf<ManifestJumper?>(null) }
    val filters = listOf("All", "Ready", "Pending", "Student", "Tandem", "Fun Jumpers")

    Column(modifier = Modifier.fillMaxSize()) {
        StagingSummaryDashboard(uiState.metrics)
        
        ScrollableTabRow(
            selectedTabIndex = filters.indexOf(selectedFilter),
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            filters.forEach { filter ->
                Tab(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    text = { Text(filter, fontSize = 12.sp) }
                )
            }
        }

        val filteredJumpers = uiState.unmanifestedJumpers.filter { jumper ->
            when (selectedFilter) {
                "Ready" -> jumper.requirements.overallStatus == ReadinessStatus.READY
                "Pending" -> jumper.requirements.overallStatus == ReadinessStatus.PENDING || jumper.requirements.overallStatus == ReadinessStatus.MISSING
                "Student" -> jumper.jumpType == JumpType.AFF_STUDENT
                "Tandem" -> jumper.jumpType == JumpType.TANDEM_STUDENT
                "Fun Jumpers" -> jumper.jumpType == JumpType.LICENSED
                else -> true
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            items(filteredJumpers) { jumper ->
                JumperStagingCard(
                    jumper = jumper,
                    onAssignClick = { selectedJumperForManifest = it }
                )
            }
        }
    }

    if (selectedJumperForManifest != null) {
        LoadAssignmentDialog(
            jumper = selectedJumperForManifest!!,
            activeLoads = uiState.activeLoads,
            onDismiss = { selectedJumperForManifest = null },
            onConfirm = { loadId ->
                onManifestJumper(selectedJumperForManifest!!, loadId)
                selectedJumperForManifest = null
            }
        )
    }
}

@Composable
fun StagingSummaryDashboard(metrics: DzOperationalMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Staging Overview", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("Ready", metrics.stagingReady, Color(0xFF2E7D32), Modifier.weight(1f))
                StatChip("Pending", metrics.stagingPending, Color(0xFFF9A825), Modifier.weight(1f))
                StatChip("Issues", metrics.stagingIncomplete, Color(0xFFD32F2F), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("Students", metrics.stagingStudents, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                StatChip("Tandems", metrics.stagingTandems, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun JumperStagingCard(
    jumper: ManifestJumper,
    onAssignClick: (ManifestJumper) -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
    val isReady = jumper.requirements.overallStatus == ReadinessStatus.READY

    Card(
        modifier = Modifier.fillMaxWidth().clickable { showDetails = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Image Placeholder
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (jumper.profilePhotoUrl != null) {
                        AsyncImage(model = jumper.profilePhotoUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Default.Person, null, tint = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(jumper.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("${jumper.license} • ${jumper.membershipNumber}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                
                ReadinessBadge(jumper.requirements.overallStatus)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Jumps: ${jumper.totalJumps}", style = MaterialTheme.typography.labelSmall)
                    Text("Type: ${jumper.jumpType}", style = MaterialTheme.typography.labelSmall)
                }
                
                if (isReady) {
                    Button(
                        onClick = { onAssignClick(jumper) },
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("ASSIGN TO LOAD", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Status: ${jumper.role}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("Discipline: ${jumper.currentDiscipline}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Quick Readiness Summary
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RequirementIndicator("Profile", jumper.requirements.profileComplete)
                RequirementIndicator("Gear", jumper.requirements.equipmentVerified)
                RequirementIndicator("Currency", jumper.requirements.currencyCurrent)
                RequirementIndicator("Waiver", jumper.requirements.waiversComplete)
            }
        }
    }

    if (showDetails) {
        JumperStagingDetailsDialog(jumper, onDismiss = { showDetails = false })
    }
}

@Composable
fun JumperStagingDetailsDialog(jumper: ManifestJumper, onDismiss: () -> Unit) {
    var activeTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Equipment", "Currency", "Waivers")

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(jumper.name, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.weight(1f))
                ReadinessBadge(jumper.requirements.overallStatus)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = activeTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = activeTab == index, onClick = { activeTab = index }, text = { Text(title, fontSize = 10.sp) })
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    when (activeTab) {
                        0 -> JumperStagingOverview(jumper)
                        1 -> EquipmentVerificationList(jumper)
                        2 -> CurrencyVerificationList(jumper)
                        3 -> WaiverVerificationList(jumper)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun JumperStagingOverview(jumper: ManifestJumper) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailRow("Full Name", jumper.name)
        DetailRow("Membership", jumper.membershipNumber)
        DetailRow("License", jumper.license)
        DetailRow("Ratings", jumper.ratings)
        DetailRow("Total Jumps", jumper.totalJumps.toString())
        DetailRow("Current Discipline", jumper.currentDiscipline)
        
        HorizontalDivider()
        
        Text("Readiness Score", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        ReadinessScoreItem("Profile", if (jumper.requirements.profileComplete) "Complete" else "Incomplete", jumper.requirements.profileComplete)
        ReadinessScoreItem("Equipment", if (jumper.requirements.equipmentVerified) "Verified" else "Awaiting Review", jumper.requirements.equipmentVerified)
        ReadinessScoreItem("Currency", if (jumper.requirements.currencyCurrent) "Current" else "Expired/Hold", jumper.requirements.currencyCurrent)
        ReadinessScoreItem("Waivers", if (jumper.requirements.waiversComplete) "All Signed" else "Missing Document", jumper.requirements.waiversComplete)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Logbook", fontSize = 10.sp) }
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Profile", fontSize = 10.sp) }
        }
    }
}

@Composable
fun EquipmentVerificationList(jumper: ManifestJumper) {
    var selectedRigForProof by remember { mutableStateOf<ManifestJumper?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EquipmentItemCard("Container", "United Parachute Tech", "Vector 3", "V34567", true)
        EquipmentItemCard("Main Canopy", "Performance Designs", "Sabre 3 170", "M98765", true)
        EquipmentItemCard("Reserve Canopy", "Performance Designs", "PDR 160", "R12345", true)
        EquipmentItemCard("AAD", "Airtec", "Cypres 2", "A55443", true)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { selectedRigForProof = jumper },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.VerifiedUser, null)
            Spacer(Modifier.width(8.dp))
            Text("Review Digital Repack Card")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Safety Validations", fontWeight = FontWeight.Bold)
        SafetyCheckItem("Reserve Repack Currency", "Current (Exp: 2024-05-12)", true)
        SafetyCheckItem("AAD Service Currency", "Current", true)
        SafetyCheckItem("Equipment Ownership", "Personal", true)
    }

    if (selectedRigForProof != null) {
        RiggingProofDialog(
            jumper = selectedRigForProof!!,
            onDismiss = { selectedRigForProof = null }
        )
    }
}

@Composable
fun RiggingProofDialog(jumper: ManifestJumper, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Repack Card Verification") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Verification for: ${jumper.name}", style = MaterialTheme.typography.labelSmall)
                
                Text("Repack Card Photo", fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.LightGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    // In a real app, this would show the URL from jumper's gear
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Text("Digital Repack Card", modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp), style = MaterialTheme.typography.labelSmall)
                }

                Text("Seal Symbol / Lead Seal", fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.LightGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Text("Seal Verification Photo", modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp), style = MaterialTheme.typography.labelSmall)
                }
                
                Text(
                    "Safety Officer Note: Ensure the rigger seal symbol matches the card signature.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Verify & Close") }
        }
    )
}

@Composable
fun CurrencyVerificationList(jumper: ManifestJumper) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CurrencyItemCard("30-Day Currency", "Current (12 jumps)", true)
        CurrencyItemCard("90-Day Currency", "Current (45 jumps)", true)
        CurrencyItemCard("USPA Membership", "Current (Exp: 2024-12-31)", true)
        CurrencyItemCard("License Validity", "Valid", true)
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("Discipline Currency", fontWeight = FontWeight.Bold)
        CurrencyItemCard("Wingsuit", "Not Applicable", null)
        CurrencyItemCard("Night Jump", "Expired", false)
        CurrencyItemCard("Camera Flyer", "Valid", true)
    }
}

@Composable
fun WaiverVerificationList(jumper: ManifestJumper) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WaiverStatusCard("General Liability", "2023-01-15", "Valid", true)
        WaiverStatusCard("Equipment Release", "2023-01-15", "Valid", true)
        WaiverStatusCard("Medical Declaration", "2023-01-15", "Valid", true)
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.DriveFileRenameOutline, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Renew/Update Waivers")
        }
    }
}

@Composable
fun EquipmentItemCard(label: String, make: String, model: String, sn: String, isVerified: Boolean?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Text("$make $model", style = MaterialTheme.typography.bodySmall)
                Text("SN: $sn", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            StatusDot(isVerified)
        }
    }
}

@Composable
fun CurrencyItemCard(label: String, status: String, isCurrent: Boolean?) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(status, style = MaterialTheme.typography.bodySmall, color = if (isCurrent == true) Color(0xFF2E7D32) else if (isCurrent == false) Color.Red else Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            StatusDot(isCurrent)
        }
    }
}

@Composable
fun WaiverStatusCard(type: String, signedDate: String, status: String, isValid: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(type, fontWeight = FontWeight.Bold)
                Text("Signed: $signedDate", style = MaterialTheme.typography.labelSmall)
            }
            Text(status, color = if (isValid) Color(0xFF2E7D32) else Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun ReadinessBadge(status: ReadinessStatus) {
    val (text, color) = when (status) {
        ReadinessStatus.READY -> "READY" to Color(0xFF2E7D32)
        ReadinessStatus.PENDING -> "PENDING" to Color(0xFFF9A825)
        ReadinessStatus.MISSING -> "MISSING" to Color(0xFFEF6C00)
        ReadinessStatus.NOT_ELIGIBLE -> "INELIGIBLE" to Color(0xFFD32F2F)
    }
    Surface(color = color, shape = RoundedCornerShape(4.dp)) {
        Text(text, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun RequirementIndicator(label: String, isComplete: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StatusDot(isComplete)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 9.sp, color = if (isComplete) Color.Unspecified else Color.Gray)
    }
}

@Composable
fun StatusDot(status: Boolean?) {
    val color = when (status) {
        true -> Color(0xFF4CAF50)
        false -> Color(0xFFF44336)
        else -> Color.Gray
    }
    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
}

@Composable
fun StatChip(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = count.toString(), fontWeight = FontWeight.Black, color = color, fontSize = 16.sp)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

@Composable
fun ReadinessScoreItem(label: String, statusText: String, isOk: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(statusText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (isOk) Color(0xFF2E7D32) else Color.Red)
    }
}

@Composable
fun SafetyCheckItem(label: String, status: String, isOk: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(if (isOk) Icons.Default.CheckCircle else Icons.Default.Cancel, null, modifier = Modifier.size(14.dp), tint = if (isOk) Color(0xFF4CAF50) else Color.Red)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(status, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LoadAssignmentDialog(
    jumper: ManifestJumper,
    activeLoads: List<FlightLoad>,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign ${jumper.name} to Load") },
        text = {
            Column {
                if (activeLoads.isEmpty()) {
                    Text("No active loads found. Create a load in Manifest first.", color = Color.Gray)
                } else {
                    Text("Select a load:")
                    Spacer(modifier = Modifier.height(8.dp))
                    activeLoads.forEach { load ->
                        val capacity = load.aircraft?.maxJumpers ?: 10
                        val isFull = load.jumpers.size >= capacity
                        
                        ListItem(
                            headlineContent = { Text("Load #${load.id} - ${load.aircraft?.name}") },
                            supportingContent = { Text("${load.jumpers.size}/$capacity slots filled") },
                            modifier = Modifier.clickable(enabled = !isFull) {
                                onConfirm(load.id)
                            },
                            trailingContent = {
                                if (isFull) {
                                    Text("FULL", color = Color.Red, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Add, null)
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
