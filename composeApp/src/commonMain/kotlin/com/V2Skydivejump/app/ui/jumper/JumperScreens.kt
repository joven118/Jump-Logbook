package com.V2Skydivejump.app.ui.jumper

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.TimeUtils
import com.V2Skydivejump.app.database.entities.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.V2Skydivejump.app.utils.CountryUtils
import com.V2Skydivejump.app.ui.social.ShareSelection
import com.V2Skydivejump.app.ui.social.MediaGrid
import com.V2Skydivejump.app.ui.social.FullSizeMediaDialog
import com.V2Skydivejump.app.ui.social.ShareToFeedDialog
import com.V2Skydivejump.app.utils.CurrencyStatus
import com.V2Skydivejump.app.utils.ExternalShareManager
import com.V2Skydivejump.app.utils.getPdfManager
import com.V2Skydivejump.app.rememberPhotoPickerLauncher
import coil3.compose.AsyncImage

@Composable
fun JumperProfileScreen(
    uiState: JumperUiState,
    onUpdateProfile: (UserEntity) -> Unit,
    onUpdatePrivacy: (UserPrivacySettingsEntity) -> Unit,
    onAddRating: (UserRatingEntity) -> Unit,
    onDeleteRating: (UserRatingEntity) -> Unit,
    onVerifyRating: (UserRatingEntity, String, String) -> Unit,
    onAddFederation: (UserFederationEntity) -> Unit,
    onDeleteFederation: (UserFederationEntity) -> Unit,
    onAddFunds: (Double, String) -> Unit,
    onUpdateProfilePicture: (String) -> Unit,
    onUpdateBackgroundPicture: (String) -> Unit,
    onInfoClick: () -> Unit,
    onLicenseClick: () -> Unit,
    onGearClick: () -> Unit,
    onUsedDzClick: () -> Unit,
    onAwardsClick: () -> Unit,
    onMembershipClick: () -> Unit = {},
    onReferClick: () -> Unit = {},
    onSyllabusClick: () -> Unit = {},
    onNfcClick: () -> Unit = {},
    onLeaderboardClick: () -> Unit = {},
    onBoardingPassClick: () -> Unit = {},
    onBookFlight: (Long) -> Unit = {},
    hasStaffAccess: Boolean = false,
    onStaffClick: () -> Unit = {}
) {
    val user = uiState.user ?: return
    var showRatingsFederationsDialog by remember { mutableStateOf(false) }
    var showWalletDialog by remember { mutableStateOf(false) }
    var showExitWeightDialog by remember { mutableStateOf(false) }
    var showLicenseTracker by remember { mutableStateOf(false) }
    var showReferralDialog by remember { mutableStateOf(false) }
    var showBoardingPassDialog by remember { mutableStateOf(false) }

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
        // Background Image Section
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            if (user.backgroundPictureUrl != null) {
                AsyncImage(
                    model = user.backgroundPictureUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clickable { backgroundPicker() },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { backgroundPicker() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tap to add Background", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            // Edit Background Icon
            IconButton(
                onClick = { backgroundPicker() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Background", tint = Color.White)
            }

            // Currency Status Badge
            Surface(
                modifier = Modifier.padding(16.dp).align(Alignment.TopEnd),
                shape = RoundedCornerShape(16.dp),
                color = when (uiState.currencyStatus) {
                    CurrencyStatus.GREEN -> Color(0xFF2E7D32)
                    CurrencyStatus.YELLOW -> Color(0xFFFBC02D)
                    CurrencyStatus.RED -> Color.Red
                }
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(uiState.currencyStatus.name, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Profile Content with overlapping photo
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Profile Photo
                Box(modifier = Modifier.size(100.dp).offset(y = (-50).dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .clickable { photoPicker() },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        if (user.profilePictureUrl != null) {
                            AsyncImage(
                                model = user.profilePictureUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Edit Profile Photo Icon
                    IconButton(
                        onClick = { photoPicker() },
                        modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Edit Photo", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f).padding(bottom = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = user.name, style = MaterialTheme.typography.headlineMedium)
                        user.country?.let { country ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = CountryUtils.getFlagEmoji(country), fontSize = 24.sp)
                        }
                    }
                    Text(
                        text = user.screenName?.let { "@$it" } ?: "No screen name",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Credential Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { showRatingsFederationsDialog = true }
                    ) {
                        // Federation/Association First
                        uiState.userFederations.forEach { fed ->
                            Text(
                                text = fed.federationName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // License Number Second
                        Text(text = "License: ${user.licenseNumber}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

                        // Ratings Third
                        uiState.userRatings.forEach { rating ->
                            Spacer(modifier = Modifier.width(8.dp))
                            RatingChip(rating)
                        }
                    }
                }
            }

            // User Stats Section
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${uiState.followerCount} Followers", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "${uiState.followingCount} Following", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(label = "Jumps", value = uiState.jumps.size.toString())
                    StatItem(label = "Freefall", value = uiState.totalFreefallTimeHms)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showExitWeightDialog = true }
                    ) {
                        val displayWeight = user.exitWeight?.let { "%.2f".format(it) } ?: "N/A"
                        Text(text = displayWeight, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Exit Weight", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val displayWingLoad = uiState.wingLoad?.let { "%.2f".format(it) } ?: "N/A"
                        Text(text = displayWingLoad, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                        Text(text = "Wing Load", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showWalletDialog = true }
                    ) {
                        Text(text = "$${"%.2f".format(user.walletBalance)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                        Text(text = "Wallet", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                // Earned Badges & Awards Section - Positioned as requested
                if (uiState.userBadges.isNotEmpty()) {
                    Text(
                        text = "Earned Qualifications",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.userBadges.take(5).forEach { userBadge ->
                            val badge = uiState.allBadges.find { it.id == userBadge.badgeId }
                            badge?.let {
                                AwardIcon(it, isEarned = true)
                            }
                        }
                        if (uiState.userBadges.size > 5) {
                            BadgePlaceholder(label = "+${uiState.userBadges.size - 5}")
                        }
                    }
                }

                ProfileViewSection(
                    uiState = uiState,
                    onPersonalInfoClick = onInfoClick,
                    onLicenseTrackerClick = onLicenseClick,
                    onGearClick = onGearClick,
                    onUsedDzClick = onUsedDzClick,
                    onAwardsClick = onAwardsClick,
                    onMembershipClick = onMembershipClick,
                    onReferralClick = { showReferralDialog = true },
                    onSyllabusClick = onSyllabusClick,
                    onNfcClick = { /* Handle NFC Link */ },
                    onLeaderboardClick = onLeaderboardClick,
                    onBoardingPassClick = { showBoardingPassDialog = true },
                    onBookFlight = onBookFlight,
                    hasStaffAccess = hasStaffAccess,
                    onStaffClick = onStaffClick
                )

                // Community Impact Section
                Spacer(modifier = Modifier.height(24.dp))
                CommunityImpactSection(uiState)
                
                // Community Leaderboards Section
                Spacer(modifier = Modifier.height(24.dp))
                CommunityLeaderboardSection(uiState.leaderboardEntries, uiState.user?.userId ?: "")

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showBoardingPassDialog) {
        BoardingPassDialog(
            user = user,
            onDismiss = { showBoardingPassDialog = false }
        )
    }

    if (showReferralDialog) {
        ReferralDialog(
            user = user,
            onDismiss = { showReferralDialog = false }
        )
    }

    if (showRatingsFederationsDialog) {
        RatingsFederationsDialog(
            ratings = uiState.userRatings,
            federations = uiState.userFederations,
            onAddRating = onAddRating,
            onDeleteRating = onDeleteRating,
            onVerifyRating = onVerifyRating,
            onAddFederation = onAddFederation,
            onDeleteFederation = onDeleteFederation,
            onDismiss = { showRatingsFederationsDialog = false }
        )
    }

    if (showWalletDialog) {
        WalletDialog(
            balance = user.walletBalance,
            transactions = uiState.transactions,
            onAddFunds = onAddFunds,
            onDismiss = { showWalletDialog = false }
        )
    }

    if (showExitWeightDialog) {
        ExitWeightDialog(
            user = user,
            gearList = uiState.userGear,
            onSave = onUpdateProfile,
            onDismiss = { showExitWeightDialog = false }
        )
    }

    if (showLicenseTracker) {
        LicenseTrackerDialog(
            uiState = uiState,
            onDismiss = { showLicenseTracker = false }
        )
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingsFederationsDialog(
    ratings: List<UserRatingEntity>,
    federations: List<UserFederationEntity>,
    onAddRating: (UserRatingEntity) -> Unit,
    onDeleteRating: (UserRatingEntity) -> Unit,
    onVerifyRating: (UserRatingEntity, String, String) -> Unit,
    onAddFederation: (UserFederationEntity) -> Unit,
    onDeleteFederation: (UserFederationEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var newRatingName by remember { mutableStateOf("") }
    var newFedName by remember { mutableStateOf("") }
    var newMemNum by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Professional Qualifications") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Your Ratings", style = MaterialTheme.typography.titleMedium)
                ratings.forEach { rating ->
                    ListItem(
                        headlineContent = { Text(rating.ratingName) },
                        trailingContent = {
                            IconButton(onClick = { onDeleteRating(rating) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = newRatingName,
                        onValueChange = { newRatingName = it },
                        label = { Text("New Rating") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (newRatingName.isNotBlank()) {
                                onAddRating(UserRatingEntity(userId = "", ratingName = newRatingName))
                                newRatingName = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Federations / Associations", style = MaterialTheme.typography.titleMedium)
                federations.forEach { fed ->
                    ListItem(
                        headlineContent = { Text(fed.federationName) },
                        supportingContent = { Text("Member: ${fed.membershipNumber ?: ""}") },
                        trailingContent = {
                            IconButton(onClick = { onDeleteFederation(fed) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        }
                    )
                }

                Column(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = newFedName,
                        onValueChange = { newFedName = it },
                        label = { Text("Federation Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newMemNum,
                            onValueChange = { newMemNum = it },
                            label = { Text("Membership #") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (newFedName.isNotBlank()) {
                                    onAddFederation(UserFederationEntity(userId = "", federationName = newFedName, membershipNumber = newMemNum))
                                    newFedName = ""; newMemNum = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoDialog(
    user: UserEntity,
    privacySettings: UserPrivacySettingsEntity,
    currentRatings: List<UserRatingEntity>,
    currentFederations: List<UserFederationEntity>,
    onSave: (UserEntity, UserPrivacySettingsEntity, List<UserRatingEntity>, List<UserFederationEntity>) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var screenName by remember { mutableStateOf(user.screenName ?: "") }
    var licenseNumber by remember { mutableStateOf(user.licenseNumber) }
    var birthdateMillis by remember { mutableStateOf(user.birthdate ?: TimeUtils.nowEpochMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var nationality by remember { mutableStateOf(user.nationality ?: "United States") }
    var showNationalityDropdown by remember { mutableStateOf(false) }

    var city by remember { mutableStateOf(user.city ?: "") }
    var province by remember { mutableStateOf(user.province ?: "") }

    var country by remember { mutableStateOf(user.country ?: "United States") }
    var showCountryDropdown by remember { mutableStateOf(false) }

    var weight by remember { mutableStateOf(user.weight?.toString() ?: "") }
    var weightUnit by remember { mutableStateOf(user.weightUnit ?: "kg") }
    var gender by remember { mutableStateOf(user.gender ?: "") }
    var mobileNumber by remember { mutableStateOf(user.mobileNumber ?: "") }
    var email by remember { mutableStateOf(user.email ?: "") }
    var emergencyContactName by remember { mutableStateOf(user.emergencyContactName ?: "") }
    var emergencyContactNumber by remember { mutableStateOf(user.emergencyContactNumber ?: "") }

    val countries = com.V2Skydivejump.app.utils.CountryUtils.countries

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = birthdateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    birthdateMillis = datePickerState.selectedDateMillis ?: birthdateMillis
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Personal Information") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Basic Info", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = screenName, onValueChange = { screenName = it }, label = { Text("Screen Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = licenseNumber, onValueChange = { licenseNumber = it }, label = { Text("License #") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))
                Text("Demographics", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

                // Birthdate Field (Read-only trigger for DatePicker)
                OutlinedTextField(
                    value = TimeUtils.formatEpochMillis(birthdateMillis),
                    onValueChange = {},
                    label = { Text("Birthdate") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    }
                )

                OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth())

                // Nationality Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = nationality,
                        onValueChange = {},
                        label = { Text("Nationality") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showNationalityDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(expanded = showNationalityDropdown, onDismissRequest = { showNationalityDropdown = false }) {
                        countries.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { nationality = c; showNationalityDropdown = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Location", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = province, onValueChange = { province = it }, label = { Text("Province/State") }, modifier = Modifier.fillMaxWidth())

                // Country Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = country,
                        onValueChange = {},
                        label = { Text("Country") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showCountryDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(expanded = showCountryDropdown, onDismissRequest = { showCountryDropdown = false }) {
                        countries.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { country = c; showCountryDropdown = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Physical", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Body Weight") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text("Unit", style = MaterialTheme.typography.labelSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = weightUnit == "kg", onClick = { weightUnit = "kg" })
                            Text("kg", fontSize = 12.sp)
                            RadioButton(selected = weightUnit == "lbs", onClick = { weightUnit = "lbs" })
                            Text("lbs", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Contact Details", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = mobileNumber, onValueChange = { mobileNumber = it }, label = { Text("Mobile") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))
                Text("Emergency Contact", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = emergencyContactName, onValueChange = { emergencyContactName = it }, label = { Text("Contact Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = emergencyContactNumber, onValueChange = { emergencyContactNumber = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    user.copy(
                        name = name,
                        screenName = screenName,
                        licenseNumber = licenseNumber,
                        birthdate = birthdateMillis,
                        nationality = nationality,
                        city = city,
                        province = province,
                        country = country,
                        weight = weight.toDoubleOrNull(),
                        weightUnit = weightUnit,
                        gender = gender,
                        mobileNumber = mobileNumber,
                        email = email,
                        emergencyContactName = emergencyContactName,
                        emergencyContactNumber = emergencyContactNumber
                    ),
                    privacySettings,
                    currentRatings,
                    currentFederations
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGearItemDialog(gear: UserGearEntity, onSave: (UserGearEntity) -> Unit, onDismiss: () -> Unit) {
    var category by remember { mutableStateOf(gear.category) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var make by remember { mutableStateOf(gear.make) }
    var model by remember { mutableStateOf(gear.model) }
    var sizeSqFt by remember { mutableStateOf(gear.sizeSqFt) }
    var sizeMlw by remember { mutableStateOf(gear.sizeMlw) }
    var serialNumber by remember { mutableStateOf(gear.serialNumber) }
    var dom by remember { mutableStateOf(gear.dateOfManufacture) }
    var weight by remember { mutableStateOf(gear.weight?.toString() ?: "") }
    var weightUnit by remember { mutableStateOf(gear.weightUnit) }

    val categories = listOf(
        "Main Canopy", "Reserve Canopy", "Container", "AAD",
        "Helmet", "Altimeter", "Camera", "Jumpsuit", "Parts", "Other Equipment"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (gear.gearId.startsWith("new_")) "Add Gear" else "Edit ${gear.category}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Category Selector
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showCategoryDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(expanded = showCategoryDropdown, onDismissRequest = { showCategoryDropdown = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(value = make, onValueChange = { make = it }, label = { Text("Make") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") }, modifier = Modifier.fillMaxWidth())

                if (category.contains("Canopy")) {
                    OutlinedTextField(value = sizeSqFt, onValueChange = { sizeSqFt = it }, label = { Text("Size (sq ft)") }, modifier = Modifier.fillMaxWidth())
                }

                if (category == "Container") {
                    OutlinedTextField(value = sizeMlw, onValueChange = { sizeMlw = it }, label = { Text("Size (MLW)") }, modifier = Modifier.fillMaxWidth())
                }

                OutlinedTextField(value = serialNumber, onValueChange = { serialNumber = it }, label = { Text("Serial Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dom, onValueChange = { dom = it }, label = { Text("Date of Manufacture") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Unit", style = MaterialTheme.typography.labelSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = weightUnit == "lbs", onClick = { weightUnit = "lbs" })
                            Text("lbs", fontSize = 12.sp)
                            RadioButton(selected = weightUnit == "kg", onClick = { weightUnit = "kg" })
                            Text("kg", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(gear.copy(
                    category = category,
                    make = make,
                    model = model,
                    sizeSqFt = sizeSqFt,
                    sizeMlw = sizeMlw,
                    serialNumber = serialNumber,
                    dateOfManufacture = dom,
                    weight = weight.toDoubleOrNull(),
                    weightUnit = weightUnit
                ))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AwardIconWithHeadsUp(badge: BadgeEntity, isEarned: Boolean) {
    var showTooltip by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isEarned) Color(0xFF0D47A1) else Color.LightGray)
            .clickable { showTooltip = true },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = badge.badgeName,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }

    if (showTooltip) {
        AlertDialog(
            onDismissRequest = { showTooltip = false },
            title = { Text(badge.badgeName) },
            text = { Text(badge.description) },
            confirmButton = { TextButton(onClick = { showTooltip = false }) { Text("OK") } }
        )
    }
}

@Composable
fun AwardIcon(badge: BadgeEntity, isEarned: Boolean) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isEarned) Color(0xFF0D47A1) else Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = badge.badgeName,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun BadgePlaceholder(label: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 8.sp, color = Color.White, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JumpLogListScreen(
    uiState: JumperUiState,
    onBack: () -> Unit,
    onAddJumpClick: () -> Unit,
    onDeleteJump: (JumpLogEntity) -> Unit,
    onEditJump: (JumpLogEntity) -> Unit,
    onShareToFeed: (JumpLogEntity, ShareSelection) -> Unit
) {
    var selectedJumpForDetails by remember { mutableStateOf<JumpLogEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(" My Jumps") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddJumpClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Jump")
            }
        }
    ) { innerPadding ->
        if (uiState.jumps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Your logbook is empty. Add your first jump!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.jumps.sortedByDescending { it.jumpNumber }) { jump ->
                    JumpCard(jump = jump, onClick = { selectedJumpForDetails = jump })
                }
            }
        }
    }

    if (selectedJumpForDetails != null && uiState.user != null) {
        JumpDetailsDialog(
            user = uiState.user,
            uiState = uiState,
            jump = selectedJumpForDetails!!,
            onDismiss = { selectedJumpForDetails = null },
            onEdit = { 
                onEditJump(selectedJumpForDetails!!)
                selectedJumpForDetails = null
            },
            onShareToFeed = onShareToFeed,
            onDelete = {
                onDeleteJump(selectedJumpForDetails!!)
                selectedJumpForDetails = null
            }
        )
    }
}

@Composable
fun JumpCard(jump: JumpLogEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Jump #${jump.jumpNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = TimeUtils.formatEpochMillis(jump.date), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = jump.dzName ?: "Unknown DZ", style = MaterialTheme.typography.bodyLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = jump.disciplines ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                Text(text = "${jump.exitAltitudeAgl} ft", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JumpDetailsDialog(
    user: UserEntity,
    uiState: JumperUiState,
    jump: JumpLogEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onShareToFeed: (JumpLogEntity, ShareSelection) -> Unit,
    onDismiss: () -> Unit
) {
    var showShareToFeed by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jump #${jump.jumpNumber} Details") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DetailRow("Date", TimeUtils.formatEpochMillis(jump.date))
                DetailRow("Dropzone", jump.dzName ?: "N/A")
                DetailRow("Location", "${jump.dzLocation ?: ""}, ${jump.country ?: ""}")
                DetailRow("Aircraft", jump.aircraftType ?: "N/A")
                DetailRow("Equipment", "${jump.containerId ?: ""} / ${jump.mainCanopyId ?: ""} ${jump.deploymentAltitudeAgl}")
                DetailRow("Disciplines", jump.disciplines ?: "")
                DetailRow("Exit Altitude", "${jump.exitAltitudeAgl} ft")
                DetailRow("Deployment", "${jump.deploymentAltitudeAgl} ft")
                DetailRow("Freefall Time", "${jump.freefallTimeSeconds} sec")
                DetailRow("Landing Style", jump.landingStyles ?: "")

                var selectedFullMedia by remember { mutableStateOf<String?>(null) }
                val mediaList = remember(jump.photoUrls) {
                    jump.photoUrls?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
                }

                if (mediaList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Photos", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    MediaGrid(
                        mediaUrls = mediaList,
                        onMediaClick = { selectedFullMedia = it }
                    )
                }

                if (selectedFullMedia != null) {
                    FullSizeMediaDialog(
                        mediaUrl = selectedFullMedia!!,
                        onDismiss = { selectedFullMedia = null }
                    )
                }

                if (jump.jumpNotes?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Description", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(jump.jumpNotes ?: "", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions in Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                    IconButton(onClick = { showShareToFeed = true }) { Icon(Icons.Default.Share, contentDescription = "Share to Feed") }
                    IconButton(onClick = { 
                        if (uiState.isPro) {
                            val ratings = uiState.userRatings.joinToString(", ") { it.ratingName }
                            getPdfManager().generateJumpPdf(user, jump, ratings)
                        } else {
                            // Paywall trigger
                            onDismiss()
                            // In real app, we'd navigate to membership or show dialog
                        }
                    }) { 
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send, 
                            contentDescription = "Print PDF",
                            tint = if (uiState.isPro) LocalContentColor.current else Color.Gray
                        ) 
                    }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )

    if (showShareToFeed) {
        ShareToFeedDialog(
            jump = jump,
            onConfirm = { selection ->
                onShareToFeed(jump, selection)
                showShareToFeed = false
            },
            onDismiss = { showShareToFeed = false }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

fun getFlagEmoji(country: String): String = com.V2Skydivejump.app.utils.CountryUtils.getFlagEmoji(country)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JumperInfoScreen(
    uiState: JumperUiState,
    onUpdateProfile: (UserEntity) -> Unit,
    onUpdatePrivacy: (UserPrivacySettingsEntity) -> Unit,
    onAddRating: (UserRatingEntity) -> Unit,
    onDeleteRating: (UserRatingEntity) -> Unit,
    onAddFederation: (UserFederationEntity) -> Unit,
    onDeleteFederation: (UserFederationEntity) -> Unit,
    onBack: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val user = uiState.user ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Info")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoRow("Full Name", user.name)
            InfoRow("Screen Name", user.screenName ?: "Not set")
            InfoRow("License #", user.licenseNumber)
            InfoRow("Birthdate", user.birthdate?.let { TimeUtils.formatEpochMillis(it) } ?: "Not set")
            InfoRow("Nationality", user.nationality ?: "Not set")
            InfoRow("Gender", user.gender ?: "Not set")
            
            Divider()
            Text("Location", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            InfoRow("City", user.city ?: "Not set")
            InfoRow("Province", user.province ?: "Not set")
            InfoRow("Country", user.country ?: "Not set")

            Divider()
            Text("Physical", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            InfoRow("Weight", "${user.weight ?: "N/A"} ${user.weightUnit}")

            Divider()
            Text("Contact", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            InfoRow("Mobile", user.mobileNumber ?: "Not set")
            InfoRow("Email", user.email ?: "Not set")

            Divider()
            Text("Emergency Contact", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            InfoRow("Contact Name", user.emergencyContactName ?: "Not set")
            InfoRow("Contact Number", user.emergencyContactNumber ?: "Not set")
        }
    }

    if (showEditDialog) {
        PersonalInfoDialog(
            user = user,
            privacySettings = uiState.privacySettings,
            currentRatings = uiState.userRatings,
            currentFederations = uiState.userFederations,
            onSave = { updatedUser, _, _, _ ->
                onUpdateProfile(updatedUser)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun UsedDzScreen(
    uiState: JumperUiState,
    onBack: () -> Unit
) {
    val dzInfos = remember(uiState.jumps) {
        uiState.jumps
            .filter { it.dzName?.isNotBlank() == true }
            .groupBy { it.dzName ?: "Unknown" }
            .map { (name, jumps) ->
                val first = jumps.first()
                UsedDzInfo(
                    name = name,
                    location = first.dzLocation ?: "",
                    country = first.country ?: "",
                    usageCount = jumps.size
                )
            }
            .sortedByDescending { it.usageCount }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Used Dropzones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (dzInfos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No dropzone data found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dzInfos) { info ->
                    UsedDzCard(info)
                }
            }
        }
    }
}

data class UsedDzInfo(
    val name: String,
    val location: String,
    val country: String,
    val usageCount: Int
)

@Composable
fun UsedDzCard(info: UsedDzInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = info.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${info.location}, ${info.country}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ) {
                Text(
                    text = "${info.usageCount} jumps",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseTrackerDialog(
    uiState: JumperUiState,
    onDismiss: () -> Unit
) {
    val totalJumps = uiState.jumps.size
    // val totalFF = uiState.totalFreefallTimeHms // Need to parse back or use raw int

    val requirements = listOf(
        LicenseRequirement("A License", 25, 5, "Basic safety and skills."),
        LicenseRequirement("B License", 50, 30, "Advanced skills and coaching."),
        LicenseRequirement("C License", 200, 60, "High performance and instruction."),
        LicenseRequirement("D License", 500, 180, "Master skydiver status.")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("License Tracker") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                requirements.forEach { req ->
                    val jumpProgress = (totalJumps.toFloat() / req.jumpCount).coerceAtMost(1f)
                    
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(text = req.level, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { jumpProgress },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(8.dp).clip(CircleShape)
                        )
                        Text(
                            text = "Jumps: $totalJumps / ${req.jumpCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (totalJumps >= req.jumpCount) Color(0xFF2E7D32) else Color.Gray
                        )
                        Text(text = req.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Divider()
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

data class LicenseRequirement(
    val level: String,
    val jumpCount: Int,
    val freefallTimeMinutes: Int,
    val description: String
)

@Composable
fun ExitWeightDialog(
    user: UserEntity,
    gearList: List<UserGearEntity>,
    onSave: (UserEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var bodyWeight by remember { mutableStateOf(user.weight?.toString() ?: "0") }
    val selectedGearIds = remember { mutableStateListOf<String>().apply { 
        addAll(user.selectedGearIdsForWeight?.split(",")?.filter { it.isNotBlank() } ?: emptyList())
    } }

    fun calculateTotal(): Double {
        val bw = bodyWeight.toDoubleOrNull() ?: 0.0
        val gw = gearList.filter { it.gearId in selectedGearIds }.sumOf { it.weight ?: 0.0 }
        return bw + gw
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exit Weight Calculator") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = bodyWeight,
                    onValueChange = { bodyWeight = it },
                    label = { Text("Body Weight (${user.weightUnit})") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Gear for Exit Weight:", style = MaterialTheme.typography.titleSmall)
                
                gearList.forEach { gear ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                if (gear.gearId in selectedGearIds) selectedGearIds.remove(gear.gearId)
                                else selectedGearIds.add(gear.gearId)
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = gear.gearId in selectedGearIds,
                            onCheckedChange = { checked ->
                                if (checked) selectedGearIds.add(gear.gearId)
                                else selectedGearIds.remove(gear.gearId)
                            }
                        )
                        Column {
                            Text(text = "${gear.category}: ${gear.make} ${gear.model}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Weight: ${gear.weight ?: 0} ${gear.weightUnit}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL EXIT WEIGHT:", fontWeight = FontWeight.Bold)
                    Text("${calculateTotal()} ${user.weightUnit}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(user.copy(
                    weight = bodyWeight.toDoubleOrNull(),
                    exitWeight = calculateTotal(),
                    selectedGearIdsForWeight = selectedGearIds.joinToString(",")
                ))
                onDismiss()
            }) {
                Text("Save & Update Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDialog(
    balance: Double,
    transactions: List<LedgerEntity>,
    onAddFunds: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountToAdd by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("CREDIT_CARD") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jump Wallet") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Current Balance", style = MaterialTheme.typography.labelMedium)
                        Text("$${"%.2f".format(balance)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Add Funds", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = amountToAdd,
                    onValueChange = { amountToAdd = it },
                    label = { Text("Amount ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(selected = selectedMethod == "CREDIT_CARD", onClick = { selectedMethod = "CREDIT_CARD" }, label = { Text("Card") })
                    FilterChip(selected = selectedMethod == "PAYPAL", onClick = { selectedMethod = "PAYPAL" }, label = { Text("PayPal") })
                    FilterChip(selected = selectedMethod == "CRYPTO", onClick = { selectedMethod = "CRYPTO" }, label = { Text("Crypto") })
                }

                Button(
                    onClick = { 
                        val amt = amountToAdd.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onAddFunds(amt, selectedMethod)
                            amountToAdd = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Top Up Now")
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                transactions.take(10).forEach { tx ->
                    ListItem(
                        headlineContent = { Text(tx.category) },
                        supportingContent = { Text(TimeUtils.formatEpochMillis(tx.timestamp)) },
                        trailingContent = { 
                            Text(
                                text = "${if (tx.amount > 0) "+" else ""}$${"%.2f".format(tx.amount)}",
                                color = if (tx.amount > 0) Color(0xFF2E7D32) else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Divider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun BoardingPassDialog(user: UserEntity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My Boarding Pass") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(user.licenseNumber, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                
                Spacer(Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.QrCode2,
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Black
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    "Scan this code at the boarding area or manifest office for zero-touch check-in.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Spacer(Modifier.height(16.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "LOGBOOK ID: ${user.userId.take(8).uppercase()}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
fun ReferralDialog(user: UserEntity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Referral Program") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Invite friends to earn reward points and free slots!", style = MaterialTheme.typography.bodyMedium)
                
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("YOUR UNIQUE CODE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(user.referralCode ?: "GENERATECODE", fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineMedium, letterSpacing = 2.sp)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { /* ExternalShareManager.shareText(...) */ },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Share Code")
                        }
                    }
                }
                
                Text(
                    "Referrers receive points for each new user signup. New users receive a welcome discount.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun RatingChip(rating: UserRatingEntity) {
    Surface(
        color = if (rating.isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (rating.isVerified) Color(0xFF2E7D32) else Color(0xFFEF6C00))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (rating.isVerified) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF2E7D32))
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = rating.ratingName,
                style = MaterialTheme.typography.labelSmall,
                color = if (rating.isVerified) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CommunityLeaderboardSection(entries: List<SeasonLeaderboardEntry>, currentUserId: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300))
                Spacer(Modifier.width(8.dp))
                Text("Season Leaderboards", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            
            Text("Top Performers - S1 2024", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            entries.take(5).forEachIndexed { index, entry ->
                LeaderboardRow(index + 1, entry, entry.userId == currentUserId)
                if (index < entries.size - 1 && index < 4) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun LeaderboardRow(rank: Int, entry: SeasonLeaderboardEntry, isMe: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(if (isMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(24.dp)) {
                when (rank) {
                    1 -> Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    2 -> Icon(Icons.Default.Star, null, tint = Color(0xFFC0C0C0), modifier = Modifier.size(16.dp))
                    3 -> Icon(Icons.Default.Star, null, tint = Color(0xFFCD7F32), modifier = Modifier.size(16.dp))
                    else -> Text(rank.toString(), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isMe) "You" else entry.userName,
                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = "${entry.value.toInt()} Jumps",
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CommunityImpactSection(uiState: JumperUiState) {
    // In a real app, these counts would be calculated from the referrals list in uiState
    val totalReferrals = 34 
    val ambassadorRank = "Gold Ambassador"
    val referralPoints = 78
    val dzosReferred = 2

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Community Impact", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ImpactStatRow("Ambassador Rank", ambassadorRank, color = MaterialTheme.colorScheme.primary)
            ImpactStatRow("Referral Points", referralPoints.toString())
            ImpactStatRow("Total Referrals", totalReferrals.toString())
            ImpactStatRow("DZOs Referred", dzosReferred.toString(), isPremium = true)
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Detailed Breakdown", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniImpactStat("Jumpers", 25)
                MiniImpactStat("Instructors", 4)
                MiniImpactStat("Coaches", 2)
                MiniImpactStat("Riggers", 1)
            }
        }
    }
}

@Composable
fun ImpactStatRow(label: String, value: String, color: Color = Color.Unspecified, isPremium: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isPremium) Icon(Icons.Default.Star, null, modifier = Modifier.size(12.dp), tint = Color(0xFFFFB300))
            Spacer(Modifier.width(4.dp))
            Text(value, fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun MiniImpactStat(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun FederationPatch(fed: UserFederationEntity) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = fed.federationName,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun ProfileViewSection(
    uiState: JumperUiState,
    onPersonalInfoClick: () -> Unit,
    onLicenseTrackerClick: () -> Unit,
    onGearClick: () -> Unit,
    onUsedDzClick: () -> Unit,
    onAwardsClick: () -> Unit,
    onMembershipClick: () -> Unit = {},
    onReferralClick: () -> Unit = {},
    onSyllabusClick: () -> Unit = {},
    onNfcClick: () -> Unit = {},
    onLeaderboardClick: () -> Unit = {},
    onBoardingPassClick: () -> Unit = {},
    onBookFlight: (Long) -> Unit,
    hasStaffAccess: Boolean = false,
    onStaffClick: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onPersonalInfoClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.AccountBox, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Info", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }

            OutlinedButton(
                onClick = onLicenseTrackerClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("License", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }

            OutlinedButton(
                onClick = onGearClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gear", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }

            OutlinedButton(
                onClick = onUsedDzClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("DZs", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onAwardsClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Awards", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }

            OutlinedButton(
                onClick = onSyllabusClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Skills", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }

            OutlinedButton(
                onClick = onReferralClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.Outlined.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refer", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }

            OutlinedButton(
                onClick = onBoardingPassClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.Outlined.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Board", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }

            OutlinedButton(
                onClick = onNfcClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("NFC", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }

            OutlinedButton(
                onClick = onLeaderboardClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Icon(Icons.Outlined.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rank", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onMembershipClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Membership")
            }
        }
        
        if (hasStaffAccess) {
            Button(
                onClick = onStaffClick,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Go to Staff Dashboard")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JumperGearScreen(
    uiState: JumperUiState,
    onSaveGear: (UserGearEntity) -> Unit,
    onDeleteGear: (UserGearEntity) -> Unit,
    onBack: () -> Unit
) {
    var selectedGearToEdit by remember { mutableStateOf<UserGearEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gear Inventory") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedGearToEdit = UserGearEntity(
                    gearId = "new_${TimeUtils.nowEpochMillis()}",
                    userId = uiState.user?.userId ?: "",
                    category = "Main Canopy",
                    make = "",
                    model = "",
                    weightUnit = "lbs"
                )
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Gear")
            }
        }
    ) { innerPadding ->
        if (uiState.userGear.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No gear items found. Tap + to add.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.userGear) { gear ->
                    GearItemCard(
                        gear = gear,
                        onEdit = { selectedGearToEdit = gear },
                        onDelete = { onDeleteGear(gear) }
                    )
                }
            }
        }
    }

    if (selectedGearToEdit != null) {
        EditGearItemDialog(
            gear = selectedGearToEdit!!,
            onSave = {
                onSaveGear(it)
                selectedGearToEdit = null
            },
            onDismiss = { selectedGearToEdit = null }
        )
    }
}

@Composable
fun GearItemCard(
    gear: UserGearEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = gear.category,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }

            Text(
                text = "${gear.make} ${gear.model}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            if (gear.sizeSqFt.isNotBlank()) {
                Text(text = "Size: ${gear.sizeSqFt} sq ft", style = MaterialTheme.typography.bodyMedium)
            }

            if (gear.serialNumber.isNotBlank()) {
                Text(
                    text = "S/N: ${gear.serialNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            gear.weight?.let {
                Text(
                    text = "Weight: $it ${gear.weightUnit}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(
    uiState: JumperUiState,
    onUpgradeClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Membership") },
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
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = if (uiState.isPro) Color(0xFFFFB300) else Color.Gray
            )
            
            Text(
                text = if (uiState.isPro) "JUMP LOGBOOK PRO" else "JUMP LOGBOOK STANDARD",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            Text(
                text = if (uiState.isPro) "Active Professional Status" else "Core Logbook Access",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Capability Comparison Header
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Feature", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Std", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                    Text("Pro", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                }
            }

            // Capability Comparison
            MembershipBenefitRow("Unlimited Cloud Logbook", true, true)
            MembershipBenefitRow("Marketplace Creation", true, true)
            MembershipBenefitRow("Individual Jump PDF Export", false, true)
            MembershipBenefitRow("AI Performance Analytics", false, true)
            MembershipBenefitRow("Global Season Leaderboards", false, true)
            MembershipBenefitRow("Priority Community Support", false, true)

            Spacer(modifier = Modifier.height(48.dp))

            if (!uiState.isPro) {
                Button(
                    onClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("UPGRADE TO PRO ($9.99/mo)", fontWeight = FontWeight.Bold)
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
                    text = "Subscription active until: 2027-01-01",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MembershipBenefitRow(label: String, standard: Boolean, pro: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(
                imageVector = if (standard) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (standard) Color(0xFF2E7D32) else Color.Red,
                modifier = Modifier.size(20.dp)
            )
            Icon(
                imageVector = if (pro) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (pro) Color(0xFF2E7D32) else Color.Red,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
