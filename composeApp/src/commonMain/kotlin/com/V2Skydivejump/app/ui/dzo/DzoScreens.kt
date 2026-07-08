package com.V2Skydivejump.app.ui.dzo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.database.entities.*
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.V2Skydivejump.app.rememberPhotoPickerLauncher

@Composable
fun SafetyStatusBanner(alerts: List<SafetyAlert>) {
    if (alerts.isEmpty()) return
    
    val latest = alerts.first()
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        color = when(latest.level) {
            SafetyLevel.NORMAL -> Color(0xFFE8F5E9)
            SafetyLevel.CAUTION -> Color(0xFFFFF3E0)
            SafetyLevel.DANGER -> Color(0xFFFFEBEE)
        },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, when(latest.level) {
            SafetyLevel.NORMAL -> Color(0xFF2E7D32)
            SafetyLevel.CAUTION -> Color(0xFFEF6C00)
            SafetyLevel.DANGER -> Color(0xFFC62828)
        })
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                when(latest.level) {
                    SafetyLevel.NORMAL -> Icons.Default.CheckCircle
                    SafetyLevel.CAUTION -> Icons.Default.Warning
                    SafetyLevel.DANGER -> Icons.Default.Error
                },
                contentDescription = null,
                tint = when(latest.level) {
                    SafetyLevel.NORMAL -> Color(0xFF2E7D32)
                    SafetyLevel.CAUTION -> Color(0xFFEF6C00)
                    SafetyLevel.DANGER -> Color(0xFFC62828)
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = latest.message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoadDialog(
    aircraftList: List<DzInventoryEntity>,
    staffList: List<UserEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Int, DzInventoryEntity, Int, String) -> Unit
) {
    var loadNum by remember { mutableStateOf("") }
    var selectedAircraft by remember { mutableStateOf<DzInventoryEntity?>(null) }
    var capacity by remember { mutableStateOf("") }
    var pilotName by remember { mutableStateOf("") }
    
    var aircraftExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manifest New Load") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = loadNum, onValueChange = { loadNum = it }, label = { Text("Load Number") }, modifier = Modifier.fillMaxWidth())
                
                Box {
                    OutlinedTextField(
                        value = selectedAircraft?.name ?: "Select Aircraft",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Aircraft") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth().clickable { aircraftExpanded = true }
                    )
                    DropdownMenu(expanded = aircraftExpanded, onDismissRequest = { aircraftExpanded = false }) {
                        aircraftList.filter { it.category == "Aircraft" }.forEach { ac ->
                            DropdownMenuItem(
                                text = { Text("${ac.name} (${ac.serialNumber})") },
                                onClick = {
                                    selectedAircraft = ac
                                    capacity = ac.maxJumpers.toString()
                                    aircraftExpanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(value = capacity, onValueChange = { capacity = it }, label = { Text("Slot Capacity") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pilotName, onValueChange = { pilotName = it }, label = { Text("Pilot Name") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                val ac = selectedAircraft
                if (loadNum.isNotBlank() && ac != null && capacity.isNotBlank()) {
                    onConfirm(loadNum.toInt(), ac, capacity.toInt(), pilotName)
                }
            }) { Text("Create Load") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DzoProfileScreen(
    uiState: DzoUiState,
    onLogout: () -> Unit,
    onUpdateProfile: (UserEntity) -> Unit = {},
    onUpdatePrivacy: (UserPrivacySettingsEntity) -> Unit = {},
    onUpdateProfilePicture: (String) -> Unit = {},
    onUpdateBackgroundPicture: (String) -> Unit = {},
    onAddSchedule: (Long, String, Int, DzInventoryEntity, Int) -> Unit = { _, _, _, _, _ -> },
    onBookFlight: (Long) -> Unit = {},
    onAddFacility: (String, String, String?) -> Unit = { _, _, _ -> },
    onDeleteFacility: (DzFacilityEntity) -> Unit = {},
    onNavigateToFacilities: () -> Unit = {},
    onAddInventory: (DzInventoryEntity) -> Unit = { _ -> },
    onUpdateInventory: (DzInventoryEntity, String?) -> Unit = { _, _ -> },
    onDeleteInventory: (DzInventoryEntity) -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToAircraft: () -> Unit = {},
    onNavigateToWaivers: () -> Unit = {},
    onNavigateToSafety: () -> Unit = {},
    onNavigateToRentalSale: () -> Unit = {},
    onAddRating: (Int, String) -> Unit = { _, _ -> },
    onToggleFollow: () -> Unit = {},
    isPublicView: Boolean = false,
    navController: NavController? = null,
    onNavigateToMembership: () -> Unit = {}
) {
    val user = uiState.user ?: return
    val uriHandler = LocalUriHandler.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showRatingsDialog by remember { mutableStateOf(false) }
    var showAddRatingDialog by remember { mutableStateOf(false) }

    val photoPicker = rememberPhotoPickerLauncher { uri ->
        uri?.let { onUpdateProfilePicture(it) }
    }
    val backgroundPicker = rememberPhotoPickerLauncher { uri ->
        uri?.let { onUpdateBackgroundPicture(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.DarkGray)
                .clickable { backgroundPicker() }
        ) {
            user.backgroundPictureUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 0.dp)
                    .offset(y = 50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { photoPicker() },
                contentAlignment = Alignment.Center
            ) {
                if (user.profilePictureUrl != null) {
                    AsyncImage(
                        model = user.profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.White)
                }
            }
            
            IconButton(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = user.dzName ?: user.name ?: "Unknown DZ", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    Text(text = "DZ Operator: ${user.name}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SocialStat(uiState.followerCount.toString(), "Followers")
                        SocialStat(uiState.followingCount.toString(), "Following")
                    }

                    val detailedLocation = listOfNotNull(
                        user.dzStreet.takeIf { it?.isNotBlank() == true },
                        user.dzCity.takeIf { it?.isNotBlank() == true },
                        user.dzProvince.takeIf { it?.isNotBlank() == true },
                        user.dzCountry.takeIf { it?.isNotBlank() == true }
                    ).joinToString(", ")
                    
                    val mapQuery = detailedLocation.ifBlank { user.dzLocation ?: "" }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (detailedLocation.isNotBlank()) {
                            Text(text = detailedLocation, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        } else {
                            user.dzLocation?.let {
                                Text(text = it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }

                        if (mapQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View in Map",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable {
                                        val encodedQuery = mapQuery.replace(" ", "+")
                                        uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=$encodedQuery")
                                    }
                            )
                        }
                    }

                    if (user.dzWebsite?.isNotBlank() == true) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp).clickable { uriHandler.openUri(user.dzWebsite) }
                        ) {
                            Icon(Icons.Default.Language, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = user.dzWebsite,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Professional Operational Info Row
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${user.operatingDays ?: "Mon-Sun"}: ${user.operatingHours ?: "08:00 - 18:00"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        if (user.dzMobileNumber?.isNotBlank() == true) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = user.dzMobileNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    if (user.dzEmail?.isNotBlank() == true) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Email, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.dzEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showRatingsDialog = true }
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "%.1f".format(uiState.averageRating), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Text(text = "${uiState.dzRatings.size} Reviews", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    
                    if (isPublicView) {
                        Button(
                            onClick = { showAddRatingDialog = true },
                            modifier = Modifier.padding(top = 8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Rate DZ", fontSize = 10.sp)
                        }

                        Button(
                            onClick = onToggleFollow,
                            modifier = Modifier.padding(top = 4.dp),
                            colors = if (uiState.isFollowing) ButtonDefaults.buttonColors(containerColor = Color.Gray) else ButtonDefaults.buttonColors(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(if (uiState.isFollowing) "Following" else "Follow", fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Professional DZO Actions Row (Visible only to DZO)
            if (!isPublicView) {
                Text(text = "Dropzone Management", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryButton("Fleet", 2, onNavigateToAircraft)
                    CategoryButton("Waivers", 4, onNavigateToWaivers)
                    CategoryButton("Safety", 6, onNavigateToSafety)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryButton("Facilities", 8, onNavigateToFacilities)
                    CategoryButton("Inventory", 10, onNavigateToInventory)
                    CategoryButton("Market", 12, onNavigateToRentalSale)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Categorized Flight Schedules Section
            val masterSchedules = uiState.flightSchedules.filter { it.creationSource == "SCHEDULE" }
            val manifestLoads = uiState.flightSchedules.filter { it.creationSource == "MANIFEST" }

            FlightScheduleSection(
                title = "Master Flight Schedules",
                titleColor = MaterialTheme.colorScheme.primary,
                schedules = masterSchedules,
                inventory = uiState.dzInventory,
                onAddSchedule = if (isPublicView) null else onAddSchedule,
                onBookFlight = onBookFlight,
                isDzo = !isPublicView
            )

            Spacer(modifier = Modifier.height(24.dp))

            FlightScheduleSection(
                title = "Live Manifest Loads",
                titleColor = MaterialTheme.colorScheme.secondary,
                schedules = manifestLoads,
                inventory = uiState.dzInventory,
                onAddSchedule = null, // Read-only section for profile view
                onBookFlight = onBookFlight,
                isDzo = !isPublicView
            )

            Spacer(modifier = Modifier.height(24.dp))

            DzoProfileInventorySection(
                title = "Gear Rental / Sale",
                emptyText = "No rental or sale gear listed yet.",
                inventory = uiState.dzInventory.filter { it.isForRent || it.isForSale }
            )

            Spacer(modifier = Modifier.height(24.dp))

            DzoProfileInventorySection(
                title = "Inventory & Fleet",
                emptyText = "No public inventory listed yet.",
                inventory = uiState.dzInventory
            )

            if (!isPublicView) {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onNavigateToMembership,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Operational Membership")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showEditDialog) {
        EditDzoProfileDialog(
            user = user,
            privacySettings = uiState.privacySettings ?: com.V2Skydivejump.app.database.entities.UserPrivacySettingsEntity(user.userId),
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedUser, updatedPrivacy ->
                onUpdateProfile(updatedUser)
                onUpdatePrivacy(updatedPrivacy)
                showEditDialog = false
            }
        )
    }

    if (showRatingsDialog) {
        DzRatingsListDialog(
            ratings = uiState.dzRatings,
            onDismiss = { showRatingsDialog = false }
        )
    }

    if (showAddRatingDialog) {
        AddDzRatingDialog(
            onDismiss = { showAddRatingDialog = false },
            onConfirm = { stars, comment ->
                onAddRating(stars, comment)
                showAddRatingDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDzoProfileDialog(
    user: UserEntity,
    privacySettings: UserPrivacySettingsEntity,
    onDismiss: () -> Unit,
    onConfirm: (UserEntity, UserPrivacySettingsEntity) -> Unit
) {
    var name by remember { mutableStateOf(user.name ?: "") }
    var namePrivacy by remember { mutableStateOf(privacySettings.namePrivacy) }
    var dzName by remember { mutableStateOf(user.dzName ?: "") }
    var dzStreet by remember { mutableStateOf(user.dzStreet ?: "") }
    var dzCity by remember { mutableStateOf(user.dzCity ?: "") }
    var dzProvince by remember { mutableStateOf(user.dzProvince ?: "") }
    var dzCountry by remember { mutableStateOf(user.dzCountry ?: "") }
    var dzMobile by remember { mutableStateOf(user.dzMobileNumber ?: "") }
    var dzEmail by remember { mutableStateOf(user.dzEmail ?: "") }
    var dzWebsite by remember { mutableStateOf(user.dzWebsite ?: "") }
    var operatingDays by remember { mutableStateOf(user.operatingDays ?: "") }
    var operatingHours by remember { mutableStateOf(user.operatingHours ?: "") }
    var windLimit by remember { mutableStateOf(user.windLimitKts.toString()) }
    var studentWindLimit by remember { mutableStateOf(user.studentWindLimitKts.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Dropzone Profile") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Operator Name") }, modifier = Modifier.fillMaxWidth())
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Name Privacy:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = namePrivacy == "PUBLIC", onClick = { namePrivacy = "PUBLIC" })
                    Text("Public", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = namePrivacy == "ONLY_ME", onClick = { namePrivacy = "ONLY_ME" })
                    Text("Only Me", fontSize = 12.sp)
                }

                OutlinedTextField(value = dzName, onValueChange = { dzName = it }, label = { Text("Dropzone Name") }, modifier = Modifier.fillMaxWidth())
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Location Details", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = dzStreet, onValueChange = { dzStreet = it }, label = { Text("Street") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dzCity, onValueChange = { dzCity = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dzProvince, onValueChange = { dzProvince = it }, label = { Text("Province/State") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dzCountry, onValueChange = { dzCountry = it }, label = { Text("Country") }, modifier = Modifier.fillMaxWidth())
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Contact & Info", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = dzMobile, onValueChange = { dzMobile = it }, label = { Text("Contact Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dzEmail, onValueChange = { dzEmail = it }, label = { Text("Contact Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dzWebsite, onValueChange = { dzWebsite = it }, label = { Text("DZ Website") }, modifier = Modifier.fillMaxWidth())
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Operations", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = operatingDays, onValueChange = { operatingDays = it }, label = { Text("Operating Days (e.g. Mon-Sun)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = operatingHours, onValueChange = { operatingHours = it }, label = { Text("Operating Hours (e.g. 08:00-18:00)") }, modifier = Modifier.fillMaxWidth())

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("AI Safety Thresholds", style = MaterialTheme.typography.labelMedium, color = Color.Red)
                OutlinedTextField(
                    value = windLimit, 
                    onValueChange = { windLimit = it }, 
                    label = { Text("General Wind Limit (kts)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = studentWindLimit, 
                    onValueChange = { studentWindLimit = it }, 
                    label = { Text("Student Wind Limit (kts)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    user.copy(
                        name = name,
                        dzName = dzName,
                        dzStreet = dzStreet,
                        dzCity = dzCity,
                        dzProvince = dzProvince,
                        dzCountry = dzCountry,
                        dzMobileNumber = dzMobile,
                        dzEmail = dzEmail,
                        dzWebsite = dzWebsite,
                        operatingDays = operatingDays,
                        operatingHours = operatingHours,
                        windLimitKts = windLimit.toDoubleOrNull() ?: 25.0,
                        studentWindLimitKts = studentWindLimit.toDoubleOrNull() ?: 15.0
                    ),
                    privacySettings.copy(namePrivacy = namePrivacy)
                )
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DzoDashboardScreen(
    uiState: DzoUiState,
    onSetWeatherSource: (WeatherSource) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Operational Awareness", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 1. Weather Monitoring Card
        WeatherMonitoringCard(uiState.weatherData, uiState.selectedWeatherSource, onSetWeatherSource)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 2. Load KPIs Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricMiniCard("In Air", "0", Icons.Default.AirplanemodeActive, Modifier.weight(1f))
            MetricMiniCard("Efficiency", "22m", Icons.Default.Timer, Modifier.weight(1f))
            MetricMiniCard("Risk", "NORMAL", Icons.Default.Shield, Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 3. Finance Quick Glance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            KpiCard("Revenue", uiState.totalRevenue, Color(0xFF2E7D32), Modifier.weight(1f))
            KpiCard("Jumpers On Site", uiState.jumperVolume.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 4. Fuel & Fleet Section
        FuelStatusSection(uiState.dzInventory.filter { it.category == "Aircraft" })
    }
}

@Composable
fun WeatherMonitoringCard(
    weather: WeatherData,
    source: WeatherSource,
    onSetSource: (WeatherSource) -> Unit
) {
    var showSourceDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showSourceDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cloud, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Live Airfield Conditions", fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(source.name.replace("_", " "), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                CompactWeatherItem("${weather.windSpeedKts.toInt()}kts ${weather.windDirectionDeg}°", Icons.Default.Air)
                CompactWeatherItem("${weather.gustKts.toInt()}kts", Icons.Default.Storm)
                CompactWeatherItem("${weather.cloudBaseFt}ft", Icons.Default.CloudQueue)
                CompactWeatherItem("${weather.temperature.toInt()}°", Icons.Default.Thermostat)
            }
        }
    }

    if (showSourceDialog) {
        WeatherSourceDialog(source, { showSourceDialog = false }, { onSetSource(it); showSourceDialog = false })
    }
}

@Composable
fun CompactWeatherItem(label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WeatherSourceDialog(current: WeatherSource, onDismiss: () -> Unit, onSelect: (WeatherSource) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Weather Source") },
        text = {
            Column {
                WeatherSource.values().forEach { source ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(source) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == source, onClick = { onSelect(source) })
                        Spacer(Modifier.width(12.dp))
                        Text(source.name.replace("_", " "))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun FuelStatusSection(aircrafts: List<DzInventoryEntity>) {
    Text("Predictive Fuel Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    
    if (aircrafts.isEmpty()) {
        Text("No aircraft configured.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    } else {
        aircrafts.forEach { ac ->
            // In a real app, find active load for this aircraft
            AircraftStatusItemFromInventory(ac, null)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun MetricMiniCard(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun AircraftStatusItemFromInventory(ac: DzInventoryEntity, activeLoad: FlightLoad?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AirplanemodeActive, null, tint = if (ac.isGround) Color.Red else Color(0xFF2E7D32))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = ac.name, fontWeight = FontWeight.Bold)
                Text(text = "Fuel: ${ac.currentFuel.toInt()} gal | Status: ${if(ac.isGround) "GROUNDED" else "FLIGHT READY"}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            if (activeLoad != null) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) {
                    Text("ON LOAD #${activeLoad.id}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DzFacilitiesScreen(
    facilities: List<DzFacilityEntity>,
    onAddFacility: (String, String, String?) -> Unit,
    onUpdateFacility: (DzFacilityEntity, String?) -> Unit,
    onDeleteFacility: (DzFacilityEntity) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Facilities") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        if (facilities.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No facilities listed. Add your hangar or packing area.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(facilities) { facility ->
                    DzFacilityItem(facility, onUpdateFacility, onDeleteFacility) { /* Fullscreen */ }
                }
            }
        }
    }

    if (showAddDialog) {
        FacilityActionDialog(null, { showAddDialog = false }, onAddFacility)
    }
}

@Composable
fun DzFacilityItem(
    facility: DzFacilityEntity,
    onUpdate: (DzFacilityEntity, String?) -> Unit,
    onDelete: (DzFacilityEntity) -> Unit,
    onPhotoClick: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            facility.photoUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(150.dp).clickable { onPhotoClick(url) },
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = facility.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onDelete(facility) }) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                    }
                }
                Text(text = facility.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun FacilityActionDialog(facility: DzFacilityEntity?, onDismiss: () -> Unit, onConfirm: (String, String, String?) -> Unit) {
    var name by remember { mutableStateOf(facility?.name ?: "") }
    var desc by remember { mutableStateOf(facility?.description ?: "") }
    var photoUri by remember { mutableStateOf(facility?.photoUrl) }
    
    val photoPicker = rememberPhotoPickerLauncher { photoUri = it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (facility == null) "Add Facility" else "Edit Facility") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Facility Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { photoPicker() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PhotoCamera, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (photoUri == null) "Attach Photo" else "Change Photo")
                }
            }
        },
        confirmButton = {
            Button(onClick = { if(name.isNotBlank()) onConfirm(name, desc, photoUri) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DzInventoryScreen(
    inventory: List<DzInventoryEntity>,
    onAddInventory: (DzInventoryEntity) -> Unit,
    onUpdateInventory: (DzInventoryEntity, String?) -> Unit,
    onDeleteInventory: (DzInventoryEntity) -> Unit,
    onBack: () -> Unit,
    title: String? = null,
    showOnlyMarketable: Boolean = false
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedItemForEdit by remember { mutableStateOf<DzInventoryEntity?>(null) }
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }

    val filteredList = if (showOnlyMarketable) inventory.filter { it.isForSale || it.isForRent } else inventory

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title ?: "DZ Inventory & Fleet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!showOnlyMarketable) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }
    ) { padding ->
        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No inventory items found.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredList) { item ->
                    DzInventoryItem(
                        item = item,
                        onEdit = { selectedItemForEdit = it },
                        onDelete = onDeleteInventory,
                        onPhotoClick = { selectedPhotoUrl = it }
                    )
                }
            }
        }
    }

    if (showAddDialog || selectedItemForEdit != null) {
        InventoryActionDialog(
            selectedItemForEdit,
            inventory,
            "DZO",
            null,
            { showAddDialog = false; selectedItemForEdit = null }
        ) { inventoryItem, photoUri ->
            if (selectedItemForEdit == null) {
                onAddInventory(inventoryItem.copy(photoUrl = photoUri))
            } else {
                onUpdateInventory(inventoryItem, photoUri)
            }
        }
    }
    
    if (selectedPhotoUrl != null) {
        FullScreenPhotoViewer(selectedPhotoUrl!!) { selectedPhotoUrl = null }
    }
}

@Composable
fun DzInventoryItem(
    item: DzInventoryEntity,
    onEdit: (DzInventoryEntity) -> Unit,
    onDelete: (DzInventoryEntity) -> Unit,
    onPhotoClick: (String) -> Unit,
    readOnly: Boolean = false
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            item.photoUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(150.dp).clickable { onPhotoClick(url) },
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "${item.category} | ${item.serialNumber}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    if (!readOnly) {
                        Row {
                            IconButton(onClick = { onEdit(item) }) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { onDelete(item) }) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                
                if (item.category == "Aircraft") {
                    Text(text = "Hours: ${item.totalFlightHours}h | Maintenance at: ${item.maintenanceIntervalHours}h", style = MaterialTheme.typography.bodySmall)
                }
                
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.isForSale) Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) { Text("FOR SALE", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) }
                    if (item.isForRent) Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(4.dp)) { Text("RENTAL", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF1976D2), fontWeight = FontWeight.Bold) }
                    if (item.isForRent && item.rentalFee > 0.0) Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) { Text("\$${item.rentalFee.toInt()} rental", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun DzoProfileInventorySection(
    title: String,
    emptyText: String,
    inventory: List<DzInventoryEntity>
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (inventory.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = emptyText,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                inventory.sortedWith(compareBy<DzInventoryEntity> { it.category }.thenBy { it.name }).forEach { item ->
                    DzInventoryItem(
                        item = item,
                        onEdit = {},
                        onDelete = {},
                        onPhotoClick = {},
                        readOnly = true
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryActionDialog(
    item: DzInventoryEntity?,
    fullInventory: List<DzInventoryEntity>,
    userRole: String,
    userId: String?,
    onDismiss: () -> Unit,
    onConfirm: (DzInventoryEntity, String?) -> Unit
) {
    var category by remember { mutableStateOf(item?.category ?: "Aircraft") }
    var name by remember { mutableStateOf(item?.name ?: "") }
    var makeModel by remember { mutableStateOf(item?.makeModel ?: "") }
    var serialNumber by remember { mutableStateOf(item?.serialNumber ?: "") }
    var size by remember { mutableStateOf(item?.sizeSqft ?: "") }
    
    var forSale by remember { mutableStateOf(item?.isForSale ?: false) }
    var forRent by remember { mutableStateOf(item?.isForRent ?: false) }
    var rentalFee by remember { mutableStateOf(item?.rentalFee?.toString() ?: "0.0") }
    
    var photoUri by remember { mutableStateOf<String?>(null) }
    val photoPicker = rememberPhotoPickerLauncher { photoUri = it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add Asset" else "Edit Asset") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val categories = listOf("Aircraft", "Main Canopy", "Reserve Canopy", "Container", "AAD", "Parts")
                var catExpanded by remember { mutableStateOf(false) }
                
                Box {
                    OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth().clickable { catExpanded = true })
                    DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        categories.forEach { cat -> DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; catExpanded = false }) }
                    }
                }
                
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Asset Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = makeModel, onValueChange = { makeModel = it }, label = { Text("Make/Model") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = serialNumber, onValueChange = { serialNumber = it }, label = { Text("Serial Number") }, modifier = Modifier.fillMaxWidth())
                
                if (category.contains("Canopy")) {
                    OutlinedTextField(value = size, onValueChange = { size = it }, label = { Text("Size (sqft)") }, modifier = Modifier.fillMaxWidth())
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = forSale, onCheckedChange = { forSale = it })
                    Text("Available for Sale")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = forRent, onCheckedChange = { forRent = it })
                    Text("Available for Rental")
                }
                
                if (forRent) {
                    OutlinedTextField(value = rentalFee, onValueChange = { rentalFee = it }, label = { Text("Rental Fee ($)") }, modifier = Modifier.fillMaxWidth())
                }
                
                Button(onClick = { photoPicker() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PhotoCamera, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Attach Asset Photo")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val finalItem = (item ?: DzInventoryEntity(dzId = "", category = category)).copy(
                    category = category,
                    name = name,
                    makeModel = makeModel,
                    serialNumber = serialNumber,
                    sizeSqft = size,
                    isForSale = forSale,
                    isForRent = forRent,
                    rentalFee = rentalFee.toDoubleOrNull() ?: 0.0
                )
                onConfirm(finalItem, photoUri)
                onDismiss()
            }) { Text("Save Asset") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun InventoryDropdown(
    label: String,
    items: List<DzInventoryEntity>,
    selectedId: Long?,
    enabled: Boolean = true,
    onAddNew: () -> Unit,
    onClear: () -> Unit,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedItem = items.find { it.id == selectedId }

    Column {
        OutlinedTextField(
            value = selectedItem?.let { "${it.category}: ${it.makeModel} (${it.serialNumber})" } ?: "Select from Inventory",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            enabled = enabled,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { expanded = true }
        )
        
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("None / Use Personal Gear") },
                onClick = { onClear(); expanded = false }
            )
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text("${item.category}: ${item.makeModel} (${item.serialNumber})") },
                    onClick = { onSelect(item.id); expanded = false }
                )
            }
            Divider()
            DropdownMenuItem(
                text = { Text("+ Add New to Inventory", color = MaterialTheme.colorScheme.primary) },
                onClick = { onAddNew(); expanded = false }
            )
        }
    }
}

@Composable
fun FullScreenPhotoViewer(url: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun FlightScheduleSection(
    title: String,
    titleColor: Color,
    schedules: List<FlightScheduleEntity>,
    inventory: List<DzInventoryEntity>,
    onAddSchedule: ((Long, String, Int, DzInventoryEntity, Int) -> Unit)?,
    onBookFlight: (Long) -> Unit,
    isDzo: Boolean
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = titleColor)
            if (onAddSchedule != null) {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, null, tint = titleColor)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (schedules.isEmpty()) {
            Text(text = "No schedules listed.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                schedules.forEach { schedule ->
                    FlightScheduleItem(schedule, onBookFlight, isDzo)
                }
            }
        }
    }

    if (showAddDialog && onAddSchedule != null) {
        AddScheduleDialog(inventory, { showAddDialog = false }, onAddSchedule)
    }
}

@Composable
fun FlightScheduleItem(schedule: FlightScheduleEntity, onBook: (Long) -> Unit, isDzo: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = "Load #${schedule.loadNumber} • ${schedule.aircraftType}", fontWeight = FontWeight.Bold)
                Text(text = "${schedule.frequency} | Capacity: ${schedule.loadCapacity}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            if (!isDzo) {
                Button(onClick = { onBook(schedule.scheduleId) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) {
                    Text("Book", fontSize = 10.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleDialog(
    inventory: List<DzInventoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, Int, DzInventoryEntity, Int) -> Unit
) {
    var loadNum by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Daily") }
    var selectedAircraft by remember { mutableStateOf<DzInventoryEntity?>(null) }
    var capacity by remember { mutableStateOf("") }
    
    var acExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Recurring Flight") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = loadNum, onValueChange = { loadNum = it }, label = { Text("Load Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = frequency, onValueChange = { frequency = it }, label = { Text("Frequency (e.g. Daily, Weekends)") }, modifier = Modifier.fillMaxWidth())
                
                Box {
                    OutlinedTextField(value = selectedAircraft?.name ?: "Select Aircraft", onValueChange = {}, readOnly = true, label = { Text("Aircraft") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth().clickable { acExpanded = true })
                    DropdownMenu(expanded = acExpanded, onDismissRequest = { acExpanded = false }) {
                        inventory.filter { it.category == "Aircraft" }.forEach { ac ->
                            DropdownMenuItem(text = { Text(ac.name) }, onClick = { selectedAircraft = ac; capacity = ac.maxJumpers.toString(); acExpanded = false })
                        }
                    }
                }
                
                OutlinedTextField(value = capacity, onValueChange = { capacity = it }, label = { Text("Default Capacity") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val ac = selectedAircraft
                if (ac != null && loadNum.isNotBlank()) {
                    onConfirm(com.V2Skydivejump.app.TimeUtils.nowEpochMillis(), frequency, loadNum.toInt(), ac, capacity.toIntOrNull() ?: 10)
                    onDismiss()
                }
            }) { Text("Save Schedule") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun DzRatingsListDialog(ratings: List<DzRatingEntity>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dropzone Reviews") },
        text = {
            if (ratings.isEmpty()) {
                Text("No reviews yet.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(ratings) { rating ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) { i ->
                                    Icon(
                                        Icons.Default.Star, 
                                        null, 
                                        tint = if (i < rating.stars) Color(0xFFFFB300) else Color.LightGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = rating.userName ?: "Anonymous", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                            Text(text = rating.comment, style = MaterialTheme.typography.bodySmall)
                            Text(text = com.V2Skydivejump.app.TimeUtils.formatEpochMillis(rating.timestamp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Divider(modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun AddDzRatingDialog(onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var stars by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate this Dropzone") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    repeat(5) { i ->
                        IconButton(onClick = { stars = i + 1 }) {
                            Icon(
                                Icons.Default.Star, 
                                null, 
                                tint = if (i < stars) Color(0xFFFFB300) else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment, 
                    onValueChange = { comment = it }, 
                    label = { Text("Your Review") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(stars, comment) }) { Text("Submit Rating") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun KpiCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AircraftManagerScreen(
    uiState: DzoUiState,
    onAddAircraft: (String, String, String, Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Fleet Management") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        val aircrafts = uiState.dzInventory.filter { it.category == "Aircraft" }
        if (aircrafts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No aircraft registered.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                items(aircrafts) { ac ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AirplanemodeActive, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(text = ac.name, fontWeight = FontWeight.Bold)
                                Text(text = "${ac.makeModel} | ${ac.serialNumber}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(text = "Capacity: ${ac.maxJumpers} slots", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAircraftDialog({ showAddDialog = false }, onAddAircraft)
    }
}

@Composable
fun AddAircraftDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var tail by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var cap by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Aircraft to Fleet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Display Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tail, onValueChange = { tail = it }, label = { Text("Tail Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Aircraft Model") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cap, onValueChange = { cap = it }, label = { Text("Max Jumpers") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if(name.isNotBlank()) onConfirm(name, tail, model, cap.toIntOrNull() ?: 0); onDismiss() }) { Text("Register Aircraft") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveManifestScreen(
    uiState: DzoUiState,
    onManifestJumper: (ManifestJumper, Int) -> Unit,
    onUnmanifestJumper: (ManifestJumper, Int) -> Unit,
    onCheckIn: (ManifestJumper, Int) -> Unit,
    onBookFlight: (Long) -> Unit,
    onAddLoad: (Int, DzInventoryEntity, Int, String) -> Unit,
    onUpdateLoadStatus: (Int, LoadStatus) -> Unit,
    onOpenScanner: () -> Unit
) {
    var showAddLoadDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Live Manifest", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = onOpenScanner) { Icon(Icons.Default.QrCodeScanner, null, tint = MaterialTheme.colorScheme.primary) }
                Button(onClick = { showAddLoadDialog = true }) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Load")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Left Column: Staging Area (Unmanifested)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Staging Area", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.unmanifestedJumpers) { jumper ->
                        JumperManifestItem(jumper, { /* Drag start */ }, onBookFlight)
                    }
                }
            }
            
            // Right Column: Active Loads
            Column(modifier = Modifier.weight(2f)) {
                Text(text = "Active Flightline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(uiState.activeLoads) { load ->
                        LoadCard(load, { onManifestJumper(it, load.id) }, { onUnmanifestJumper(it, load.id) }, { onUpdateLoadStatus(load.id, it) })
                    }
                }
            }
        }
    }

    if (showAddLoadDialog) {
        AddLoadDialog(uiState.dzInventory, uiState.dzStaff, { showAddLoadDialog = false }, onAddLoad)
    }
}

@Composable
fun LoadCard(
    load: FlightLoad,
    onAddJumper: (ManifestJumper) -> Unit,
    onRemoveJumper: (ManifestJumper) -> Unit,
    onStatusChange: (LoadStatus) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Load #${load.id} - ${load.aircraft?.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(text = "Pilot: ${load.pilotName} | Status: ${load.status.name}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
                    Text(text = load.status.name, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Jumper List in Load
            load.jumpers.forEach { jumper ->
                LoadJumperRow(jumper, load.jumpers, { onRemoveJumper(jumper) }, { /* Verify */ })
            }
            
            if (load.jumpers.size < load.maxJumpers) {
                Text(text = "${load.maxJumpers - load.jumpers.size} slots remaining", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), modifier = Modifier.padding(top = 8.dp))
            } else {
                Text(text = "LOAD FULL", style = MaterialTheme.typography.labelSmall, color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (load.status == LoadStatus.PLANNING) Button(onClick = { onStatusChange(LoadStatus.BOARDING) }, modifier = Modifier.weight(1f)) { Text("CALL LOAD") }
                if (load.status == LoadStatus.BOARDING) Button(onClick = { onStatusChange(LoadStatus.TAXIING) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))) { Text("TAXI") }
                if (load.status == LoadStatus.TAXIING) Button(onClick = { onStatusChange(LoadStatus.IN_FLIGHT) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))) { Text("TAKEOFF") }
                if (load.status == LoadStatus.IN_FLIGHT) Button(onClick = { onStatusChange(LoadStatus.LANDED) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) { Text("LANDED") }
            }
        }
    }
}

@Composable
fun LoadJumperRow(jumper: ManifestJumper, allOnLoad: List<ManifestJumper>, onRemove: () -> Unit, onVerify: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(Modifier.width(8.dp))
            Text(text = jumper.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun JumperManifestItem(jumper: ManifestJumper, onDrag: () -> Unit, onBook: (Long) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onDrag() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = jumper.name, fontWeight = FontWeight.Bold)
                Text(text = "${jumper.jumpType.name} | ${jumper.license}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            if (!jumper.isWaiverSigned) Icon(Icons.Default.AssignmentLate, null, tint = Color.Red, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun DzoOperationsScreen(
    uiState: DzoUiState,
    onAddAircraft: (String, String, String, Int) -> Unit,
    onAddStaff: (String, String, String) -> Unit,
    onManifestJumper: (ManifestJumper, Int) -> Unit,
    onUnmanifestJumper: (ManifestJumper, Int) -> Unit,
    onCheckIn: (ManifestJumper, Int) -> Unit,
    onBookFlight: (Long) -> Unit,
    onAddLoad: (Int, DzInventoryEntity, Int, String) -> Unit,
    onUpdateLoadStatus: (Int, LoadStatus) -> Unit,
    onOpenScanner: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Manifest") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Bookings") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Fleet") })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Staff") })
        }
        
        when(selectedTab) {
            0 -> ActiveManifestScreen(uiState, onManifestJumper, onUnmanifestJumper, onCheckIn, onBookFlight, onAddLoad, onUpdateLoadStatus, onOpenScanner)
            1 -> BookingManagerSection(uiState)
            2 -> AircraftManagerScreen(uiState, onAddAircraft)
            3 -> StaffManagerSection(uiState, onAddStaff)
        }
    }
}

@Composable
fun BookingManagerSection(uiState: DzoUiState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Future Reservations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (uiState.bookings.isEmpty()) {
            Text(text = "No upcoming bookings.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.bookings) { booking ->
                    BookingItem(booking)
                }
            }
        }
    }
}

@Composable
fun BookingItem(booking: Booking) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = booking.customerName, fontWeight = FontWeight.Bold)
                Text(text = "${booking.jumpType.name} | ${com.V2Skydivejump.app.TimeUtils.formatEpochMillis(booking.preferredDate)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Surface(
                color = when(booking.status) {
                    "Confirmed" -> Color(0xFFE8F5E9)
                    "Pending" -> Color(0xFFFFF3E0)
                    else -> Color.LightGray
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(text = booking.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StaffManagerSection(uiState: DzoUiState, onAddStaff: (String, String, String) -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Professional Team", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Staff")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (uiState.dzStaff.isEmpty()) {
            Text(text = "No staff members added.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.dzStaff) { staff ->
                    StaffItem(staff)
                }
            }
        }
    }

    if (showAddDialog) {
        AddStaffDialog({ showAddDialog = false }, onAddStaff)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStaffDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("DZ_MANAGER") }
    var expanded by remember { mutableStateOf(false) }
    
    val roles = listOf(
        "PILOT", 
        "INSTRUCTOR", 
        "TANDEM_INSTRUCTOR",
        "MANIFEST", 
        "SAFETY_OFFICER", 
        "RIGGER", 
        "FINANCE_OFFICER",
        "ADMIN_OFFICER",
        "DZ_MANAGER", 
        "DZ_OWNER", 
        "OPERATION_OFFICER"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add DZ Staff Member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                
                // Robust Role Selection using ExposedDropdownMenuBox pattern
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRole.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.replace("_", " ")) },
                                onClick = {
                                    selectedRole = role
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank() && email.isNotBlank()) onConfirm(name, email, selectedRole) }) { Text("Create Account") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun StaffItem(staff: UserEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Text(text = staff.name?.take(1) ?: "S", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = staff.name ?: "Unknown Staff", fontWeight = FontWeight.Bold)
                Text(text = staff.role.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DzWaiverManagerScreen(
    dzoUser: UserEntity?,
    waivers: List<DzWaiverEntity>,
    onUpsert: (String, String, Long) -> Unit,
    onDeactivate: (DzWaiverEntity) -> Unit,
    onBack: () -> Unit
) {
    var selectedWaiverForEdit by remember { mutableStateOf<DzWaiverEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Waiver Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (waivers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No waivers found. Add your first legal template.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(waivers) { waiver ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = waiver.title, fontWeight = FontWeight.Bold)
                                    Text(text = "Updated: ${com.V2Skydivejump.app.TimeUtils.formatEpochMillis(waiver.lastUpdated)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                Row {
                                    IconButton(onClick = { selectedWaiverForEdit = waiver }) { Icon(Icons.Default.Edit, null) }
                                    IconButton(onClick = { onDeactivate(waiver) }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        WaiverEditDialog(null, { showAddDialog = false }, { t, c -> onUpsert(t, c, 0L); showAddDialog = false })
    }
    
    if (selectedWaiverForEdit != null) {
        WaiverEditDialog(selectedWaiverForEdit, { selectedWaiverForEdit = null }, { t, c -> onUpsert(t, c, selectedWaiverForEdit!!.id); selectedWaiverForEdit = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaiverEditDialog(
    waiver: DzWaiverEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(waiver?.title ?: "") }
    var content by remember { mutableStateOf(waiver?.content ?: "") }
    
    // Help users with placeholders
    val placeholders = listOf(
        "{{FULL_NAME}}", "{{DOB}}", "{{DATE}}", "{{LICENSE}}",
        "{{DZ_NAME}}", "{{EC_NAME}}", "{{EC_NUMBER}}"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (waiver == null) "New Waiver Template" else "Edit Waiver Template") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Document Title") }, modifier = Modifier.fillMaxWidth())
                
                Text(text = "Available Placeholders (Tap to Insert):", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    placeholders.forEach { p ->
                        Surface(
                            modifier = Modifier.clickable { content += " $p " },
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(text = p, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                OutlinedTextField(
                    value = content, 
                    onValueChange = { content = it }, 
                    label = { Text("Legal Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 10
                )
            }
        },
        confirmButton = {
            Button(onClick = { if(title.isNotBlank()) onSave(title, content) }) { Text("Save Document") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun SocialStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun CategoryButton(label: String, index: Int, onClick: () -> Unit) {
    val colors = listOf(
        Color(0xFF1976D2), Color(0xFF1976D2), // Fleet
        Color(0xFFE91E63), Color(0xFFE91E63), // Waivers
        Color(0xFF673AB7), Color(0xFF673AB7), // Safety
        Color(0xFF009688), Color(0xFF009688), // Facilities
        Color(0xFF795548), Color(0xFF795548), // Inventory
        Color(0xFFFF9800), Color(0xFFFF9800)  // Market
    )
    
    val color = colors.getOrElse(index) { MaterialTheme.colorScheme.primary }

    Surface(
        modifier = Modifier.height(50.dp).clickable { onClick() },
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when(label) {
                        "Fleet" -> Icons.Default.AirplanemodeActive
                        "Waivers" -> Icons.Default.Assignment
                        "Safety" -> Icons.Default.Shield
                        "Facilities" -> Icons.Default.Domain
                        "Inventory" -> Icons.Default.Inventory
                        "Market" -> Icons.Default.ShoppingCart
                        else -> Icons.Default.Settings
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = color
                )
                Spacer(Modifier.width(8.dp))
                Text(text = label, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectronicSignatureDialog(
    jumperName: String,
    waivers: List<DzWaiverEntity>,
    dzoUser: UserEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var signature by remember { mutableStateOf("") }
    
    // User data for placeholder replacement
    var fullName by remember { mutableStateOf(jumperName) }
    var dob by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    var licenseNumber by remember { mutableStateOf("") }
    var federation by remember { mutableStateOf("") }
    var totalJumps by remember { mutableStateOf("") }
    var highestLicense by remember { mutableStateOf("") }
    var lastJumpDate by remember { mutableStateOf("") }
    
    var gearMain by remember { mutableStateOf("") }
    var gearReserve by remember { mutableStateOf("") }
    var gearContainer by remember { mutableStateOf("") }
    var repackDate by remember { mutableStateOf("") }
    var gearAad by remember { mutableStateOf("") }
    var aadStatus by remember { mutableStateOf("") }
    
    var ecName by remember { mutableStateOf("") }
    var ecRelationship by remember { mutableStateOf("") }
    var ecNumber by remember { mutableStateOf("") }
    var ecAltNumber by remember { mutableStateOf("") }
    var ecAddress by remember { mutableStateOf("") }

    val currentWaiver = waivers.getOrNull(step - 1)

    fun processContent(raw: String): String {
        return raw
            .replace("{{FULL_NAME}}", fullName)
            .replace("{{DOB}}", dob)
            .replace("{{LICENSE}}", licenseNumber)
            .replace("{{DZ_NAME}}", dzoUser?.dzName ?: dzoUser?.name ?: "This Dropzone")
            .replace("{{EC_NAME}}", ecName)
            .replace("{{EC_RELATIONSHIP}}", ecRelationship)
            .replace("{{EC_NUMBER}}", ecNumber)
            .replace("{{EC_ALT_NUMBER}}", ecAltNumber)
            .replace("{{EC_ADDRESS}}", ecAddress)
            .replace("{{DZO_REP_NAME}}", dzoUser?.name ?: "DZ Representative")
            .replace("{{DZO_REP_TITLE}}", dzoUser?.role?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Manager")
            .replace("{{DATE}}", com.V2Skydivejump.app.TimeUtils.formatEpochMillis(com.V2Skydivejump.app.TimeUtils.nowEpochMillis()))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                if (step == 0) "Review & Edit Information" 
                else "Electronic Check-In: $fullName"
            ) 
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step == 0) {
                    // Step 0: Data Review
                    Text("Please verify and update your information before signing.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    
                    Text("Jumper Information", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("Date of Birth") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, modifier = Modifier.weight(0.5f))
                    }
                    OutlinedTextField(value = sex, onValueChange = { sex = it }, label = { Text("Sex") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nationality, onValueChange = { nationality = it }, label = { Text("Nationality") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = contactNumber, onValueChange = { contactNumber = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                    
                    HorizontalDivider()
                    Text("Credentials", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    OutlinedTextField(value = licenseNumber, onValueChange = { licenseNumber = it }, label = { Text("License #") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = federation, onValueChange = { federation = it }, label = { Text("Issuing Organization") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = totalJumps, onValueChange = { totalJumps = it }, label = { Text("Total Jumps") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = highestLicense, onValueChange = { highestLicense = it }, label = { Text("Highest License") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = lastJumpDate, onValueChange = { lastJumpDate = it }, label = { Text("Last Jump Date") }, modifier = Modifier.fillMaxWidth())
                    
                    HorizontalDivider()
                    Text("Equipment", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    OutlinedTextField(value = gearMain, onValueChange = { gearMain = it }, label = { Text("Main Canopy") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = gearReserve, onValueChange = { gearReserve = it }, label = { Text("Reserve Canopy") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = gearContainer, onValueChange = { gearContainer = it }, label = { Text("Container") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = repackDate, onValueChange = { repackDate = it }, label = { Text("Reserve Repack Date") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = gearAad, onValueChange = { gearAad = it }, label = { Text("AAD") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = aadStatus, onValueChange = { aadStatus = it }, label = { Text("AAD Status") }, modifier = Modifier.fillMaxWidth())
                    
                    HorizontalDivider()
                    Text("Emergency Contact", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    OutlinedTextField(value = ecName, onValueChange = { ecName = it }, label = { Text("Primary Contact Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ecRelationship, onValueChange = { ecRelationship = it }, label = { Text("Relationship") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ecNumber, onValueChange = { ecNumber = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ecAltNumber, onValueChange = { ecAltNumber = it }, label = { Text("Alternate Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ecAddress, onValueChange = { ecAddress = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())

                } else if (currentWaiver != null) {
                    Text(text = "Document $step of ${waivers.size}", style = MaterialTheme.typography.labelSmall)
                    Text(text = currentWaiver.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = processContent(currentWaiver.content), 
                            modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState()), 
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Type full name to sign:", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = signature, 
                        onValueChange = { signature = it }, 
                        placeholder = { Text(fullName) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("All documents reviewed and information verified. Proceed to check-in?")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step <= waivers.size) {
                        step++
                        signature = ""
                    } else {
                        onConfirm(signature)
                    }
                },
                enabled = (step == 0) || (signature.isNotBlank()) || (waivers.isEmpty() && step > 0)
            ) {
                Text(
                    when {
                        step == 0 -> "Proceed to Waiver"
                        step < waivers.size -> "Next Document"
                        else -> "Finalize Check-In"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DzoMembershipScreen(
    uiState: DzoUiState,
    onUpgradeClick: () -> Unit,
    onBack: () -> Unit
) {
    val isPro = uiState.user?.membershipTier == "PRO"
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Operational Membership") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.BusinessCenter,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = if (isPro) Color(0xFF1976D2) else Color.Gray
            )
            
            Text(
                text = if (isPro) "JUMP LOGBOOK PRO DROPZONE" else "JUMP LOGBOOK STANDARD DROPZONE",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = if (isPro) "High-Volume Automation Active" else "Basic Manifest Management",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Business Capability Matrix Header
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Operational Capacity", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Std", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                    Text("Pro", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                }
            }

            // Capability Comparison
            DzoMembershipBenefitRow("Daily Manifest Loads", "Up to 5", "Unlimited")
            DzoMembershipBenefitRow("Aircraft Fleet Capacity", "Up to 2", "Unlimited")
            DzoMembershipBenefitRow("Professional Staff Seats", "Up to 3", "Unlimited")
            DzoMembershipBenefitRow("Zero-Touch Jump Push", "❌ Manual", "✅ Auto-Sync")
            DzoMembershipBenefitRow("CFO Business Analytics", "❌ Basic", "✅ Full ROI")
            DzoMembershipBenefitRow("AI Safety Interlocks", "❌ Passive", "✅ Active")

            Spacer(modifier = Modifier.height(48.dp))

            if (!isPro) {
                Button(
                    onClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("UPGRADE TO PRO ($49.99/mo)", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = false
                ) {
                    Text("PRO STATUS ACTIVE")
                }
                
                Text(
                    text = "Operational scaling enabled.",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DzoMembershipBenefitRow(label: String, standardValue: String, proValue: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = standardValue, 
                style = MaterialTheme.typography.labelSmall, 
                color = if (standardValue.contains("❌")) Color.Red else Color.Gray,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = proValue, 
                style = MaterialTheme.typography.labelSmall, 
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
