package com.V2Skydivejump.app.ui.dzo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.database.entities.UserEntity
import com.V2Skydivejump.app.database.entities.UserGearEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiggerSealGeneratorScreen(
    user: UserEntity,
    onSaveSeal: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var sealSymbol by remember { mutableStateOf(user.riggerSealSymbol ?: "") }
    var license by remember { mutableStateOf(user.riggerLicense ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rigger Seal Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Your Unique Identification", style = MaterialTheme.typography.titleMedium)
            Text("This symbol is embossed on physical lead seals and digitally linked to your repacks.", 
                style = MaterialTheme.typography.labelSmall, 
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(48.dp))
            
            // Visual Seal Representation
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB0BEC5))
                    .border(4.dp, Color.DarkGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if(sealSymbol.isBlank()) "???" else sealSymbol.uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.DarkGray,
                        letterSpacing = 4.sp
                    )
                    Text("RIGGER SEAL", fontSize = 10.sp, color = Color.DarkGray)
                }
            }
            
            Spacer(Modifier.height(48.dp))
            
            OutlinedTextField(
                value = sealSymbol,
                onValueChange = { if (it.length <= 4) sealSymbol = it },
                label = { Text("Seal Symbol (e.g. ABC1)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter 2-4 chars") },
                supportingText = { Text("Assigned by your national aviation authority.") }
            )
            
            Spacer(Modifier.height(16.dp))
            
            OutlinedTextField(
                value = license,
                onValueChange = { license = it },
                label = { Text("Rigger License Number") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.weight(1f))
            
            Button(
                onClick = { onSaveSeal(sealSymbol, license) },
                modifier = Modifier.fillMaxWidth(),
                enabled = sealSymbol.isNotBlank() && license.isNotBlank()
            ) {
                Text("Lock Digital Seal")
            }
        }
    }
}

@Composable
fun GearRepackLogScreen(
    gearList: List<UserGearEntity>,
    riggerName: String,
    onVerifyRepack: (UserGearEntity) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Repack Queue") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val dueSoon = gearList.filter { it.category == "Container" } // Simplification
            
            items(dueSoon) { gear ->
                RepackQueueItem(gear, onVerifyRepack)
            }
            
            if (dueSoon.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No rigs pending repack.", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun RepackQueueItem(gear: UserGearEntity, onVerify: (UserGearEntity) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${gear.make} ${gear.model}", fontWeight = FontWeight.Bold)
                Text("SN: ${gear.serialNumber}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("Due: ${gear.repackDueDate ?: "UNKNOWN"}", color = Color.Red, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
            Button(onClick = { onVerify(gear) }, shape = RoundedCornerShape(8.dp)) {
                Text("SIGN OFF")
            }
        }
    }
}
