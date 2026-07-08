package com.V2Skydivejump.app.testing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.V2Skydivejump.app.SessionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    initializer: TestDzoInitializer,
    sessionViewModel: SessionViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val user by sessionViewModel.currentUser.collectAsState()
    var statusMessage by remember { mutableStateOf("Ready for testing.") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Data Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Scenario 1: Account Creation", style = MaterialTheme.typography.titleMedium)
                    Text("Registers test_jumper@v2.com and test_dzo@v2.com in Supabase Auth.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                statusMessage = "Registering accounts..."
                                initializer.createTestAccounts()
                                statusMessage = "Accounts created. Try Force Login below."
                                isLoading = false
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("RUN: REGISTER TEST ACCOUNTS")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    statusMessage = "Logging in as DZO..."
                                    sessionViewModel.signIn("test_dzo@v2.com", "Password123!")
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("FORCE DZO")
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    statusMessage = "Logging in as Jumper..."
                                    sessionViewModel.signIn("test_jumper@v2.com", "Password123!")
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("FORCE JUMPER")
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Scenario 2: DZO Asset Injection", style = MaterialTheme.typography.titleMedium)
                    Text("Injects Aircraft, Hangar, and Waivers into the current DZO account.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            user?.let { u ->
                                scope.launch {
                                    isLoading = true
                                    statusMessage = "Injecting assets for ${u.userId}..."
                                    initializer.setupDzoAssets(u.userId)
                                    statusMessage = "Assets injected and synced."
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading && user?.role == "DZ_OPERATOR",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("RUN: SETUP DZO ASSETS")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Scenario 3: Operational Loop", style = MaterialTheme.typography.titleMedium)
                    Text("Simulates a landing for a mock jumper to verify the automated logbook entry.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val uid = user?.userId ?: "test_user_id"
                            scope.launch {
                                isLoading = true
                                statusMessage = "Simulating jump for $uid..."
                                initializer.simulateFlightLoop(uid, uid) // Self-test
                                statusMessage = "Jump logged locally. Check Logbook."
                                isLoading = false
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("RUN: SIMULATE JUMP LOG")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.05f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = statusMessage,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
