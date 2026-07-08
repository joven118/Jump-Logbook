package com.V2Skydivejump.app.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.V2Skydivejump.app.database.entities.*
import coil3.compose.AsyncImage
import com.V2Skydivejump.app.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    uiState: AdminUiState,
    onVerifyUser: (String) -> Unit,
    onVerifyBadge: (Long) -> Unit,
    onAddAdmin: (String) -> Unit,
    onUpdateConfig: (AppConfig) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Verification", "Users", "Settings")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Console") },
                actions = {
                    IconButton(onClick = { /* TODO: Refresh */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(label) },
                        icon = {
                            Icon(
                                when (index) {
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.CheckCircle
                                    2 -> Icons.Default.Person
                                    else -> Icons.Default.Settings
                                },
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> OverviewTab(uiState.systemMetrics)
                1 -> VerificationTab(uiState, onVerifyUser, onVerifyBadge)
                2 -> UsersTab(uiState, onAddAdmin)
                3 -> SettingsTab(uiState.appConfig, onUpdateConfig)
            }
        }
    }
}

@Composable
fun OverviewTab(metrics: Map<String, Int>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("System Overview", style = MaterialTheme.typography.headlineMedium)
        }
        
        items(metrics.toList()) { (label, value) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = value.toString(), 
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        if (metrics.isEmpty()) {
            item {
                Text("No data available.", color = Color.Gray)
            }
        }
    }
}

@Composable
fun VerificationTab(
    uiState: AdminUiState,
    onVerifyUser: (String) -> Unit,
    onVerifyBadge: (Long) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Pending User Verifications", style = MaterialTheme.typography.titleLarge) }
        items(uiState.unverifiedUsers) { user ->
            ListItem(
                headlineContent = { Text(user.name) },
                supportingContent = { Text(user.role) },
                trailingContent = {
                    Button(onClick = { onVerifyUser(user.userId) }) { Text("Verify") }
                }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { Text("Pending Badge Claims", style = MaterialTheme.typography.titleLarge) }
        items(uiState.pendingBadges) { badge ->
            var showDocDialog by remember { mutableStateOf(false) }
            
            ListItem(
                headlineContent = { Text("Badge: ${badge.badgeId}") },
                supportingContent = { 
                    Column {
                        Text("User: ${badge.userId}")
                        Text("Date: ${TimeUtils.formatEpochMillis(badge.dateEarned)}", style = MaterialTheme.typography.labelSmall)
                        if (badge.supportingDocumentUrl != null) {
                            TextButton(onClick = { showDocDialog = true }) {
                                Text("View Proof Document", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                },
                trailingContent = {
                    Button(onClick = { onVerifyBadge(badge.id) }) { Text("Approve") }
                }
            )

            if (showDocDialog && badge.supportingDocumentUrl != null) {
                AlertDialog(
                    onDismissRequest = { showDocDialog = false },
                    title = { Text("Proof Document") },
                    text = {
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                            AsyncImage(
                                model = badge.supportingDocumentUrl,
                                contentDescription = "Proof",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDocDialog = false }) { Text("Close") }
                    }
                )
            }
        }
    }
}

@Composable
fun UsersTab(uiState: AdminUiState, onAddAdmin: (String) -> Unit) {
    var adminEmail by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Promote User to Admin", style = MaterialTheme.typography.titleLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = adminEmail,
                onValueChange = { adminEmail = it },
                label = { Text("Email Address") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onAddAdmin(adminEmail); adminEmail = "" }) {
                Text("Add")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("User Directory", style = MaterialTheme.typography.titleLarge)
        LazyColumn {
            items(uiState.allUsers) { user ->
                ListItem(
                    headlineContent = { Text(user.name) },
                    supportingContent = { Text(user.email ?: "") },
                    trailingContent = { Text(user.role) }
                )
            }
        }
    }
}

@Composable
fun SettingsTab(config: AppConfig, onUpdateConfig: (AppConfig) -> Unit) {
    var primaryColor by remember { mutableStateOf(config.primaryColor) }
    var welcomeText by remember { mutableStateOf(config.welcomeText) }
    var feeType by remember { mutableStateOf(config.convenienceFeeType) }
    var feeRate by remember { mutableStateOf(config.convenienceFeeRate.toString()) }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("App Customization", style = MaterialTheme.typography.titleLarge)
        
        OutlinedTextField(
            value = primaryColor,
            onValueChange = { primaryColor = it },
            label = { Text("Primary Theme Color (Hex)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = welcomeText,
            onValueChange = { welcomeText = it },
            label = { Text("Welcome Message") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Platform Revenue Settings", style = MaterialTheme.typography.titleLarge)
        
        // Fee Type Selection
        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                value = feeType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Convenience Fee Type") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                modifier = Modifier.fillMaxWidth().clickable { expanded = true }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("Fixed ($)") }, onClick = { feeType = "FIXED"; expanded = false })
                DropdownMenuItem(text = { Text("Percentage (%)") }, onClick = { feeType = "PERCENTAGE"; expanded = false })
            }
        }

        OutlinedTextField(
            value = feeRate,
            onValueChange = { feeRate = it },
            label = { Text(if (feeType == "FIXED") "Fee Amount ($)" else "Fee Percentage (%)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { 
                onUpdateConfig(
                    AppConfig(
                        primaryColor, 
                        welcomeText, 
                        convenienceFeeType = feeType, 
                        convenienceFeeRate = feeRate.toDoubleOrNull() ?: 0.0
                    )
                ) 
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply Global Settings")
        }
    }
}
