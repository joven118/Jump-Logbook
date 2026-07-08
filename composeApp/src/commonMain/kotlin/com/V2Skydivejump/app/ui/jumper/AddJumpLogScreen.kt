package com.V2Skydivejump.app.ui.jumper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.TimeUtils
import com.V2Skydivejump.app.rememberPhotoPickerLauncher
import com.V2Skydivejump.app.rememberVideoPickerLauncher
import com.V2Skydivejump.app.database.entities.JumpLogEntity
import com.V2Skydivejump.app.database.entities.UserGearEntity
import com.V2Skydivejump.app.utils.CountryUtils
import com.V2Skydivejump.app.utils.MediaUploadService
import com.V2Skydivejump.app.utils.MediaPipeline
import com.V2Skydivejump.app.utils.TelemetryParser
import com.V2Skydivejump.app.utils.WeatherUtils
import kotlinx.coroutines.launch
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJumpLogScreen(
    uiState: JumperUiState,
    editJump: JumpLogEntity? = null,
    onSave: (JumpLogEntity) -> Unit,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var dateMillis by remember { mutableStateOf(editJump?.date ?: TimeUtils.nowEpochMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val jumpNumber = remember(dateMillis, uiState.jumps) {
        if (editJump != null && editJump.date == dateMillis) {
            editJump.jumpNumber.toString()
        } else {
            // Dynamic preview of what the jump number will be
            (uiState.jumps.count { it.date < dateMillis } + 1).toString()
        }
    }

    var dzName by remember { mutableStateOf(editJump?.dzName ?: "") }
    var dzLocation by remember { mutableStateOf(editJump?.dzLocation ?: "") }
    var country by remember { mutableStateOf(editJump?.country ?: "United States") }
    var showCountryDropdown by remember { mutableStateOf(false) }
    
    var aircraftType by remember { mutableStateOf(editJump?.aircraftType ?: "") }
    var aircraftTailNumber by remember { mutableStateOf(editJump?.aircraftTailNumber ?: "") }
    
    var exitAlt by remember { mutableStateOf(editJump?.exitAltitudeAgl?.toString() ?: "13500") }
    var deployAlt by remember { mutableStateOf(editJump?.deploymentAltitudeAgl?.toString() ?: "3000") }
    var freefallTimeSeconds by remember { mutableStateOf(editJump?.freefallTimeSeconds?.toString() ?: "60") }
    var averageSpeed by remember { mutableStateOf(editJump?.averageSpeedMph?.toString() ?: "") }
    
    var jumpType by remember { mutableStateOf(editJump?.jumpType ?: "Fun Jump") }
    var showJumpTypeDropdown by remember { mutableStateOf(false) }
    
    val countries = CountryUtils.countries
    val jumpTypes = listOf("Fun Jump", "AFF", "Tandem", "Coaching", "Competition", "Training Jump", "Demonstration Jump")
    val formationSizes = listOf("2-Way", "4-Way", "4+ Way", "8-Way", "8+ Way", "16-Way")
    val disciplines = listOf("Belly", "Sit Fly", "Head-Up", "Head-Down", "Tracking", "Angle", "Free Fly", "Wingsuit", "CRW", "Hop & Pop", "HAHO", "Unstable", "Double Canopy", "Canopy Malfunction", "Cut-away")
    val landingStyles = listOf("run-out", "knee slide", "butt slide", "hard landing", "Off DZ", "obstacle landing", "Accuracy landing", "Water Landing", "Tree Landing", "Powerline Landing")

    val selectedDisciplines = remember { 
        mutableStateListOf<String>().apply { 
            if (editJump != null) addAll(editJump.disciplines?.split(",")?.filter { it.isNotBlank() && !it.contains("-Way") } ?: emptyList()) 
        } 
    }
    var selectedFormationSize by remember { 
        mutableStateOf(editJump?.disciplines?.split(",")?.find { it.contains("-Way") } ?: "") 
    }
    var showFormationDropdown by remember { mutableStateOf(false) }
    val selectedLandingStyles = remember { 
        mutableStateListOf<String>().apply { 
            if (editJump != null) addAll(editJump.landingStyles?.split(",")?.filter { it.isNotBlank() } ?: emptyList()) 
        } 
    }
    
    val selectedPhotoUris = remember { 
        mutableStateListOf<String>().apply { 
            if (editJump != null) addAll(editJump.photoUrls?.split(",")?.filter { it.isNotBlank() } ?: emptyList()) 
        } 
    }
    val selectedVideoUris = remember { 
        mutableStateListOf<String>().apply { 
            if (editJump != null) addAll(editJump.videoUrls?.split(",")?.filter { it.isNotBlank() } ?: emptyList())
        } 
    }
    
    var selectedMainCanopy by remember { mutableStateOf(uiState.userGear.find { it.gearId == editJump?.mainCanopyId } ?: uiState.userGear.find { it.category == "Main Canopy" }) }
    var selectedReserveCanopy by remember { mutableStateOf(uiState.userGear.find { it.gearId == editJump?.reserveCanopyId } ?: uiState.userGear.find { it.category == "Reserve Canopy" }) }
    var selectedContainer by remember { mutableStateOf(uiState.userGear.find { it.gearId == editJump?.containerId } ?: uiState.userGear.find { it.category == "Container" }) }
    var selectedHelmet by remember { mutableStateOf(uiState.userGear.find { it.gearId == editJump?.helmetId } ?: uiState.userGear.find { it.category == "Helmet" }) }
    var selectedAltimeter by remember { mutableStateOf(uiState.userGear.find { it.gearId == editJump?.altimeterId } ?: uiState.userGear.find { it.category == "Altimeter" }) }
    var selectedParts by remember { mutableStateOf(uiState.userGear.find { it.gearId == editJump?.partsId } ?: uiState.userGear.find { it.category == "Parts" }) }
    var selectedCamera by remember { mutableStateOf(uiState.userGear.find { it.gearId == editJump?.cameraId } ?: uiState.userGear.find { it.category == "Camera" }) }
    var selectedSuit by remember { mutableStateOf(uiState.userGear.find { it.gearId == editJump?.suitId } ?: uiState.userGear.find { it.category == "Jumpsuit/Wingsuit" }) }
    var selectedOtherGear by remember { mutableStateOf(uiState.userGear.find { it.gearId == editJump?.otherGearId } ?: uiState.userGear.find { it.category == "Other Equipment" }) }

    var weather by remember { mutableStateOf(editJump?.weatherCondition ?: "") }
    var selfPacked by remember { mutableStateOf(editJump?.selfPacked ?: false) }
    var jumpNotes by remember { mutableStateOf(editJump?.jumpNotes ?: "") }

    var showBleSyncDialog by remember { mutableStateOf(false) }
    var showCsvImportDialog by remember { mutableStateOf(false) }
    
    var showVerificationDialog by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(editJump?.isVerified ?: false) }
    var verifierName by remember { mutableStateOf(editJump?.verifierName) }
    var verifierLicense by remember { mutableStateOf(editJump?.verifierLicense) }
    var verificationMethod by remember { mutableStateOf(editJump?.verificationMethod) }
    var electronicSignature by remember { mutableStateOf(editJump?.electronicSignature) }
    var verificationRequestPending by remember { mutableStateOf(false) }

    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberPhotoPickerLauncher { uri ->
        if (uri != null) selectedPhotoUris.add(uri)
    }
    val videoPicker = rememberVideoPickerLauncher { uri ->
        if (uri != null) selectedVideoUris.add(uri)
    }

    if (showVerificationDialog) {
        VerificationDialog(
            onVerified = { name, license, method, signature ->
                verifierName = name
                verifierLicense = license
                verificationMethod = method
                electronicSignature = signature
                isVerified = (method != "CHAT_REQUEST" && method != "EMAIL_REQUEST")
                verificationRequestPending = (method == "CHAT_REQUEST" || method == "EMAIL_REQUEST")
                showVerificationDialog = false
            },
            onDismiss = { showVerificationDialog = false }
        )
    }

    if (showBleSyncDialog) {
        BleSyncDialog(
            currentUserId = uiState.user?.userId,
            onDataReceived = { data: JumpLogEntity ->
                if (data.date > 0) dateMillis = data.date
                dzName = data.dzName ?: dzName
                dzLocation = data.dzLocation ?: dzLocation
                country = data.country ?: country
                aircraftType = data.aircraftType ?: aircraftType
                exitAlt = data.exitAltitudeAgl.toString()
                deployAlt = data.deploymentAltitudeAgl.toString()
                freefallTimeSeconds = data.freefallTimeSeconds.toString()
                averageSpeed = data.averageSpeedMph.toString()
                jumpNotes = data.jumpNotes ?: jumpNotes
                
                data.disciplines?.split(",")?.let { list ->
                    selectedFormationSize = list.find { it.contains("-Way") } ?: selectedFormationSize
                    val otherDiscs = list.filter { it.isNotBlank() && !it.contains("-Way") }
                    selectedDisciplines.clear()
                    selectedDisciplines.addAll(otherDiscs)
                }

                showBleSyncDialog = false
            },
            onDismiss = { showBleSyncDialog = false }
        )
    }

    if (showCsvImportDialog) {
        CsvImportDialog(
            onDataParsed = { data: Map<String, String> ->
                data["date"]?.toLongOrNull()?.let { dateMillis = it }
                data["dzName"]?.let { dzName = it }
                data["dzLocation"]?.let { dzLocation = it }
                data["country"]?.let { country = it }
                data["aircraft"]?.let { aircraftType = it }
                data["exitAlt"]?.let { exitAlt = it }
                data["deployAlt"]?.let { deployAlt = it }
                data["freefallTime"]?.let { freefallTimeSeconds = it }
                data["avgSpeed"]?.let { averageSpeed = it }
                data["notes"]?.let { jumpNotes = it }
                
                data["disciplines"]?.split("|")?.let { list ->
                    selectedFormationSize = list.find { it.contains("-Way") } ?: selectedFormationSize
                    val otherDiscs = list.filter { it.isNotBlank() && !it.contains("-Way") }
                    selectedDisciplines.clear()
                    selectedDisciplines.addAll(otherDiscs)
                }

                showCsvImportDialog = false
            },
            onDismiss = { showCsvImportDialog = false }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateMillis = datePickerState.selectedDateMillis ?: dateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (editJump != null) "Edit Jump" else "Add Jump", style = MaterialTheme.typography.headlineMedium)
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { showBleSyncDialog = true }) {
                    Icon(Icons.Default.Bluetooth, contentDescription = "Alti Sync", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showCsvImportDialog = true }) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Import CSV", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = jumpNumber,
                onValueChange = { /* Automatically assigned, no manual change allowed */ },
                label = { Text("Jump #") },
                modifier = Modifier.weight(1f),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1.5f).height(56.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(TimeUtils.formatEpochMillis(dateMillis), style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }

            Box(modifier = Modifier.weight(1.5f)) {
                OutlinedCard(onClick = { showJumpTypeDropdown = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(jumpType, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = showJumpTypeDropdown, onDismissRequest = { showJumpTypeDropdown = false }) {
                    jumpTypes.forEach { type ->
                        DropdownMenuItem(text = { Text(type) }, onClick = { jumpType = type; showJumpTypeDropdown = false })
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = dzName, onValueChange = { dzName = it }, label = { Text("DZ Name") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = dzLocation, onValueChange = { dzLocation = it }, label = { Text("City/Location") }, modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedCard(onClick = { showCountryDropdown = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(country, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = showCountryDropdown, onDismissRequest = { showCountryDropdown = false }) {
                    countries.forEach { c ->
                        DropdownMenuItem(text = { Text(c) }, onClick = { country = c; showCountryDropdown = false })
                    }
                }
            }
            OutlinedTextField(value = aircraftType, onValueChange = { aircraftType = it }, label = { Text("Aircraft Type") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = aircraftTailNumber, onValueChange = { if (it.length <= 20) aircraftTailNumber = it }, label = { Text("Tail #") }, modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = exitAlt, onValueChange = { exitAlt = it }, label = { Text("Exit Alt") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = deployAlt, onValueChange = { deployAlt = it }, label = { Text("Deploy Alt") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = freefallTimeSeconds, onValueChange = { freefallTimeSeconds = it }, label = { Text("FF Sec") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = averageSpeed, onValueChange = { averageSpeed = it }, label = { Text("Avg Speed") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Self-packed Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp).clickable { selfPacked = !selfPacked }
            ) {
                Checkbox(
                    checked = selfPacked,
                    onCheckedChange = { selfPacked = it }
                )
                Text("Self-packed", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedTextField(
                value = weather,
                onValueChange = { weather = it },
                label = { Text("Weather") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                scope.launch {
                    weather = WeatherUtils.fetchWeather(dateMillis, dzName)
                }
            }) {
                Icon(Icons.Default.Info, contentDescription = "Fetch Weather")
            }
        }

        Text("Gear Selection", style = MaterialTheme.typography.titleMedium)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val weight = 1f
            CompactGearSelector(Modifier.weight(weight), "Main", selectedMainCanopy, uiState.userGear.filter { it.category == "Main Canopy" }) { selectedMainCanopy = it }
            CompactGearSelector(Modifier.weight(weight), "Resv", selectedReserveCanopy, uiState.userGear.filter { it.category == "Reserve Canopy" }) { selectedReserveCanopy = it }
            CompactGearSelector(Modifier.weight(weight), "Cont", selectedContainer, uiState.userGear.filter { it.category == "Container" }) { selectedContainer = it }
            CompactGearSelector(Modifier.weight(weight), "Helm", selectedHelmet, uiState.userGear.filter { it.category == "Helmet" }) { selectedHelmet = it }
            CompactGearSelector(Modifier.weight(weight), "Alti", selectedAltimeter, uiState.userGear.filter { it.category == "Altimeter" }) { selectedAltimeter = it }
            CompactGearSelector(Modifier.weight(weight), "Part", selectedParts, uiState.userGear.filter { it.category == "Parts" }) { selectedParts = it }
            CompactGearSelector(Modifier.weight(weight), "Cam", selectedCamera, uiState.userGear.filter { it.category == "Camera" }) { selectedCamera = it }
            CompactGearSelector(Modifier.weight(weight), "Suit", selectedSuit, uiState.userGear.filter { it.category == "Jumpsuit/Wingsuit" }) { selectedSuit = it }
            CompactGearSelector(Modifier.weight(weight), "Oth", selectedOtherGear, uiState.userGear.filter { it.category == "Other Equipment" }) { selectedOtherGear = it }
        }

        Text("Disciplines", style = MaterialTheme.typography.titleMedium)
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            // Formation Dropdown instead of "Formation" chip
            Box(modifier = Modifier.padding(2.dp)) {
                FilterChip(
                    selected = selectedFormationSize.isNotEmpty(),
                    onClick = { showFormationDropdown = true },
                    label = { Text(if (selectedFormationSize.isEmpty()) "Formation" else selectedFormationSize, fontSize = 10.sp) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                DropdownMenu(expanded = showFormationDropdown, onDismissRequest = { showFormationDropdown = false }) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = { selectedFormationSize = ""; showFormationDropdown = false }
                    )
                    formationSizes.forEach { size ->
                        DropdownMenuItem(
                            text = { Text(size) },
                            onClick = { selectedFormationSize = size; showFormationDropdown = false }
                        )
                    }
                }
            }

            disciplines.forEach { disc ->
                FilterChip(
                    selected = selectedDisciplines.contains(disc),
                    onClick = { if (selectedDisciplines.contains(disc)) selectedDisciplines.remove(disc) else selectedDisciplines.add(disc) },
                    label = { Text(disc, fontSize = 10.sp) },
                    modifier = Modifier.padding(2.dp)
                )
            }
        }

        Text("Landing Style", style = MaterialTheme.typography.titleMedium)
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            landingStyles.forEach { style ->
                FilterChip(
                    selected = selectedLandingStyles.contains(style),
                    onClick = { if (selectedLandingStyles.contains(style)) selectedLandingStyles.remove(style) else selectedLandingStyles.add(style) },
                    label = { Text(style, fontSize = 10.sp) },
                    modifier = Modifier.padding(2.dp)
                )
            }
        }

        OutlinedTextField(
            value = jumpNotes,
            onValueChange = { jumpNotes = it },
            label = { Text("Jump Notes") },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )

        Text("Media", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = { photoPicker() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Photo")
            }
            OutlinedButton(
                onClick = { videoPicker() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Video")
            }
        }

        if (selectedPhotoUris.isNotEmpty() || selectedVideoUris.isNotEmpty()) {
            FlowRow(modifier = Modifier.fillMaxWidth()) {
                selectedPhotoUris.forEach { uri ->
                    MediaThumbnail(uri, isVideo = false) { selectedPhotoUris.remove(uri) }
                }
                selectedVideoUris.forEach { uri ->
                    MediaThumbnail(uri, isVideo = true) { selectedVideoUris.remove(uri) }
                }
            }
        }

        Text("Verification", style = MaterialTheme.typography.titleMedium)
        OutlinedCard(
            onClick = { showVerificationDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (isVerified) Color(0xFFE8F5E9) else if (verificationRequestPending) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        isVerified -> Icons.Default.CheckCircle
                        verificationRequestPending -> Icons.Default.Send
                        else -> Icons.Default.ThumbUp
                    },
                    contentDescription = null,
                    tint = when {
                        isVerified -> Color(0xFF2E7D32)
                        verificationRequestPending -> Color(0xFFEF6C00)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = when {
                            isVerified -> "Verified by $verifierName"
                            verificationRequestPending -> "Request Sent to $verifierName"
                            else -> "Unverified Jump"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            isVerified -> "Method: $verificationMethod"
                            verificationRequestPending -> "Waiting for response via ${if(verificationMethod == "EMAIL_REQUEST") "Email" else "Chat"}"
                            else -> "Tap to verify or request verification"
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        uploadError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                enabled = !isUploading,
                onClick = {
                    scope.launch {
                        isUploading = true
                        uploadError = null
                        
                        // Execute Media Pipeline for all selected photos
                        val uploadedPhotos = try {
                            selectedPhotoUris.map { uri ->
                            if (uri.startsWith("http")) uri to uri // Already uploaded
                            else {
                                println("DIAGNOSTIC: AddJumpLog - Processing photo: $uri")
                                val result = MediaPipeline.processAndUpload(uri, (editJump?.jumpId ?: 0L))
                                println("DIAGNOSTIC: AddJumpLog - Photo processed: ${result.fullResUrl}")
                                result.fullResUrl to result.thumbnailUrl
                            }
                            }
                        } catch (e: Exception) {
                            println("DIAGNOSTIC ERROR: Media processing failed: ${e.message}")
                            e.printStackTrace()
                            uploadError = e.message ?: "Could not upload one or more photos."
                            isUploading = false
                            return@launch
                        }

                        val uploadedVideos = try {
                            selectedVideoUris.mapIndexed { index, uri ->
                                if (uri.startsWith("http")) uri
                                else MediaUploadService.uploadLocalUri(
                                    localUri = uri,
                                    bucketName = "jump-media",
                                    entityType = "jump_video",
                                    entityId = "${editJump?.jumpId ?: TimeUtils.nowEpochMillis()}_$index",
                                    mediaKind = "video"
                                ).publicUrl
                            }
                        } catch (e: Exception) {
                            println("DIAGNOSTIC ERROR: Video upload failed: ${e.message}")
                            e.printStackTrace()
                            uploadError = e.message ?: "Could not upload one or more videos."
                            isUploading = false
                            return@launch
                        }

                        val jump = (editJump ?: JumpLogEntity(
                            userId = uiState.user?.userId ?: return@launch,
                            jumpNumber = 0,
                            date = 0,
                            location = "",
                            aircraft = ""
                        )).copy(
                            jumpNumber = jumpNumber.toIntOrNull() ?: 0,
                            date = dateMillis,
                            dzName = dzName,
                            dzLocation = dzLocation,
                            country = country,
                            location = dzName,
                            aircraft = aircraftType,
                            aircraftType = aircraftType,
                            aircraftTailNumber = aircraftTailNumber,
                            exitAltitudeAgl = exitAlt.toIntOrNull() ?: 0,
                            deploymentAltitudeAgl = deployAlt.toIntOrNull() ?: 0,
                            freefallTimeSeconds = freefallTimeSeconds.toIntOrNull() ?: 0,
                            averageSpeedMph = averageSpeed.toIntOrNull() ?: 0,
                            jumpType = jumpType,
                            disciplines = (selectedDisciplines + listOfNotNull(selectedFormationSize.ifEmpty { null })).joinToString(","),
                            landingStyles = selectedLandingStyles.joinToString(","),
                            mainCanopyId = selectedMainCanopy?.gearId,
                            reserveCanopyId = selectedReserveCanopy?.gearId,
                            containerId = selectedContainer?.gearId,
                            helmetId = selectedHelmet?.gearId,
                            altimeterId = selectedAltimeter?.gearId,
                            partsId = selectedParts?.gearId,
                            cameraId = selectedCamera?.gearId,
                            suitId = selectedSuit?.gearId,
                            otherGearId = selectedOtherGear?.gearId,
                            photoUrls = uploadedPhotos.joinToString(",") { it.first },
                            thumbnailUrls = uploadedPhotos.joinToString(",") { it.second },
                            videoUrls = uploadedVideos.joinToString(","),
                            isVerified = isVerified,
                            verifierName = verifierName,
                            verifierLicense = verifierLicense,
                            verificationMethod = verificationMethod,
                            electronicSignature = electronicSignature,
                            jumpNotes = jumpNotes,
                            selfPacked = selfPacked
                        )
                        onSave(jump)
                        isUploading = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Uploading...")
                } else {
                    Text(if (editJump != null) "Save Changes" else "Save Jump")
                }
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun MediaThumbnail(uri: String, isVideo: Boolean, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isVideo) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.DarkGray
                )
                Text(
                    text = "Video",
                    fontSize = 10.sp,
                    color = Color.DarkGray
                )
            }
        } else {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
        
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun CompactGearSelector(
    modifier: Modifier,
    label: String,
    selectedGear: UserGearEntity?,
    options: List<UserGearEntity>,
    onSelected: (UserGearEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (selectedGear != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = selectedGear?.model ?: "N/A",
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { gear ->
                DropdownMenuItem(
                    text = { Text("${gear.make} ${gear.model}", fontSize = 12.sp) },
                    onClick = { onSelected(gear); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(modifier = modifier) {
        content()
    }
}

@Composable
fun BleSyncDialog(
    currentUserId: String?,
    onDataReceived: (JumpLogEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var isScanning by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Device Digital Link") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (isScanning) {
                    Text("Searching for nearby altimeters...", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                    
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        isScanning = false
                    }
                } else {
                    Icon(Icons.Default.BluetoothConnected, null, modifier = Modifier.size(48.dp), tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("SonoAlti 2 Found!", fontWeight = FontWeight.Bold)
                    Text("Ready to sync latest jump data.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(onClick = {
                        val userId = currentUserId ?: return@Button
                        onDataReceived(JumpLogEntity(
                            userId = userId,
                            jumpNumber = 123,
                            date = TimeUtils.nowEpochMillis(),
                            dzName = "Skydive Lake Elsinore",
                            dzLocation = "Lake Elsinore, CA",
                            country = "United States",
                            aircraftType = "Super Otter",
                            exitAltitudeAgl = 13500,
                            deploymentAltitudeAgl = 3200,
                            freefallTimeSeconds = 62,
                            maxSpeedMph = 118,
                            disciplines = "4-Way,Belly,Tracking",
                            jumpNotes = "Great formation with the team. Sync via BLE."
                        ))
                    }, enabled = currentUserId != null) {
                        Text("Sync Latest Jump")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CsvImportDialog(
    onDataParsed: (Map<String, String>) -> Unit,
    onDismiss: () -> Unit
) {
    var isUploading by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import CSV Data") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Select your altimeter CSV file to import jump data.", fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isUploading) {
                    CircularProgressIndicator()
                    Text("Reading file...", modifier = Modifier.padding(top = 8.dp))
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1500)
                        // Simulate parsed data from a file
                        onDataParsed(mapOf(
                            "date" to TimeUtils.nowEpochMillis().toString(),
                            "dzName" to "Skydive Perris",
                            "dzLocation" to "Perris, CA",
                            "country" to "United States",
                            "aircraft" to "Skyvan",
                            "exitAlt" to "12500",
                            "deployAlt" to "3150",
                            "freefallTime" to "63",
                            "avgSpeed" to "118",
                            "disciplines" to "8-Way|Sit Fly",
                            "notes" to "First jump at this DZ!"
                        ))
                    }
                } else {
                    OutlinedButton(onClick = { isUploading = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Upload CSV File")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Supports standard altimeter formats.", fontSize = 10.sp, color = Color.Gray)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun VerificationDialog(
    onVerified: (String, String, String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var method by remember { mutableStateOf("SIGNATURE") }
    var name by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var qrScanning by remember { mutableStateOf(false) }
    var requestSent by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jump Verification") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                FlowRow(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = method == "SIGNATURE",
                        onClick = { method = "SIGNATURE" },
                        label = { Text("Signature") },
                        modifier = Modifier.padding(2.dp)
                    )
                    FilterChip(
                        selected = method == "QR_CODE",
                        onClick = { method = "QR_CODE" },
                        label = { Text("QR Code") },
                        modifier = Modifier.padding(2.dp)
                    )
                    FilterChip(
                        selected = method == "LOOKUP",
                        onClick = { method = "LOOKUP" },
                        label = { Text("App Chat") },
                        modifier = Modifier.padding(2.dp)
                    )
                    FilterChip(
                        selected = method == "EMAIL",
                        onClick = { method = "EMAIL" },
                        label = { Text("Email") },
                        modifier = Modifier.padding(2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (method) {
                    "SIGNATURE" -> {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Verifier Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = license, onValueChange = { license = it }, label = { Text("License #") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Draw Signature Here", color = Color.Gray)
                        }
                    }
                    "QR_CODE" -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            if (qrScanning) {
                                CircularProgressIndicator()
                                Text("Scanning QR Code...", modifier = Modifier.padding(top = 8.dp))
                                LaunchedEffect(Unit) {
                                    kotlinx.coroutines.delay(2000)
                                    onVerified("DZ Operator Alpha", "D-99999", "QR_CODE", null)
                                }
                            } else {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp))
                                Button(onClick = { qrScanning = true }) {
                                    Text("Open Scanner")
                                }
                            }
                        }
                    }
                    "LOOKUP" -> {
                        OutlinedTextField(value = license, onValueChange = { license = it }, label = { Text("Search Jumper ID/License") }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = { name = "John Verifier"; license = "C-54321" }, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Lookup User")
                        }
                        if (name.isNotEmpty()) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Text("Found: $name ($license)", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { 
                                        requestSent = true
                                        onVerified(name, license, "CHAT_REQUEST", null)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Send Verification Request via Chat")
                                }
                            }
                        }
                    }
                    "EMAIL" -> {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Verifier Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Verifier Email") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                requestSent = true
                                onVerified(name, email, "EMAIL_REQUEST", null)
                            },
                            enabled = name.isNotBlank() && email.contains("@"),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Request Verification via Email")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (method == "SIGNATURE" || (method == "LOOKUP" && name.isNotEmpty() && !requestSent)) {
                Button(
                    onClick = { onVerified(name, license, method, if(method == "SIGNATURE") "base64_placeholder" else null) },
                    enabled = name.isNotEmpty() && license.isNotEmpty()
                ) {
                    Text("Verify Locally")
                }
            }
        },
        dismissButton = {
            if (!requestSent) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
