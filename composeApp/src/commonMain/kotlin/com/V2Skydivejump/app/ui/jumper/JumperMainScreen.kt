package com.V2Skydivejump.app.ui.jumper

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.V2Skydivejump.app.AppFooter
import com.V2Skydivejump.app.AppLogo
import com.V2Skydivejump.app.BluetoothPermissionHandler
import com.V2Skydivejump.app.SkydiveTopAppBar
import com.V2Skydivejump.app.UserRole
import com.V2Skydivejump.app.database.AppDatabase
import com.V2Skydivejump.app.ui.calendar.EventCalendarScreen
import com.V2Skydivejump.app.ui.communication.ChatDetailScreen
import com.V2Skydivejump.app.ui.communication.ChatListScreen
import com.V2Skydivejump.app.ui.communication.NotificationScreen
import com.V2Skydivejump.app.ui.directory.DropzoneDirectoryScreen
import com.V2Skydivejump.app.ui.social.GlobalFeedScreen
import com.V2Skydivejump.app.ui.marketplace.CreateListingScreen
import com.V2Skydivejump.app.ui.marketplace.MarketplaceScreen

import com.V2Skydivejump.app.data.*

import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JumperMainScreen(
    database: AppDatabase, 
    viewModel: JumperViewModel,
    onLogout: () -> Unit,
    hasStaffAccess: Boolean = false,
    onSwitchToStaff: () -> Unit = {},
    onNavigateToFaq: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var currentTab by rememberSaveable { mutableStateOf(0) }
    var showLicenseTracker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    val screenTitle = when(currentTab) {
        0 -> "Logbook"
        1 -> "Market"
        2 -> "Directory"
        3 -> "Events"
        4 -> "Performance"
        13 -> "Booking"
        5 -> "Flight Jacket"
        6 -> "Altimeter"
        7 -> "Profile"
        8 -> "Badges & Awards"
        9 -> "Messages"
        10 -> "Notifications"
        12 -> "AI Insights"
        14 -> "Membership"
        15 -> "Jumpers"
        else -> "Jump Logbook"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SkydiveTopAppBar(
                title = screenTitle,
                actions = {
                    // About Icon
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(Icons.Default.Info, contentDescription = "About App")
                    }

                    // FAQ Icon
                    IconButton(onClick = onNavigateToFaq) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "FAQs")
                    }

                    IconButton(onClick = { currentTab = 15; navController.navigate("users") }) {
                        Icon(Icons.Default.Groups, contentDescription = "Registered Users")
                    }

                    // Chat Icon
                    IconButton(onClick = { currentTab = 9; navController.navigate("chat_list") }) {
                        BadgedBox(
                            badge = {
                                // Potentially show unread count
                            }
                        ) {
                            Icon(Icons.Default.Email, contentDescription = "Messages")
                        }
                    }

                    // Notification Icon
                    IconButton(onClick = { currentTab = 10; navController.navigate("notifications") }) {
                        BadgedBox(
                            badge = {
                                if (uiState.unreadNotifications > 0) {
                                    Badge { Text(uiState.unreadNotifications.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }

                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0; navController.navigate("log") },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Log") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1; navController.navigate("market") },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                        label = { Text("Market") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2; navController.navigate("directory") },
                        icon = { Icon(Icons.Default.Place, contentDescription = null) },
                        label = { Text("DZ") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3; navController.navigate("calendar") },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        label = { Text("Events") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 13,
                        onClick = { currentTab = 13; navController.navigate("booking") },
                        icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                        label = { Text("Book") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 11,
                        onClick = { currentTab = 11; navController.navigate("feed") },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        label = { Text("Feed") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 12,
                        onClick = { currentTab = 12; navController.navigate("ai_insights") },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text("AI") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 7,
                        onClick = { 
                            if (currentTab != 7) {
                                currentTab = 7
                                navController.navigate("profile") {
                                    popUpTo("log") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Me") }
                    )
                }
                AppFooter()
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "log",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("log") {
                JumpLogListScreen(
                    uiState = uiState,
                    onBack = { currentTab = 7; navController.navigate("profile") },
                    onAddJumpClick = { navController.navigate("add_jump") },
                    onDeleteJump = { jump -> viewModel.deleteJump(jump) },
                    onEditJump = { jump -> viewModel.updateJump(jump) },
                    onShareToFeed = { jump, selection -> viewModel.shareJump(jump, selection) }
                )
            }
            composable("add_jump") {
                AddJumpLogScreen(
                    uiState = uiState,
                    onSave = { jump ->
                        viewModel.addJump(jump)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable("feed") {
                GlobalFeedScreen(
                    posts = uiState.feedPosts,
                    onLikePost = { viewModel.likePost(it) },
                    onAddComment = { postId, text -> viewModel.addComment(postId, text) },
                    getComments = { viewModel.getCommentsForPost(it) }
                )
            }
            composable("users") {
                UserFollowScreen(
                    currentUserId = uiState.user?.userId,
                    users = uiState.registeredUsers,
                    isFollowing = { viewModel.isFollowing(it) },
                    onToggleFollow = { viewModel.toggleFollow(it) }
                )
            }
            composable("market") {
                MarketplaceScreen(
                    listings = uiState.marketplaceListings,
                    onCreateListingClick = { navController.navigate("create_listing") },
                    onListingClick = { /* Handle details */ }
                )
            }
            composable("create_listing") {
                CreateListingScreen(
                    uiState = uiState,
                    onSave = { listing ->
                        viewModel.addListing(listing)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable("directory") {
                DropzoneDirectoryScreen(
                    dropzones = uiState.dropzones,
                    onDzClick = { dzId -> navController.navigate("dzo_profile/$dzId") }
                )
            }
            composable("dzo_profile/{dzId}") { backStackEntry ->
                val dzId = backStackEntry.arguments?.getString("dzId") ?: ""
                val dz = uiState.dropzones.find { it.id == dzId }
                
                // Reusing DZO Profile screen components but in a viewer-friendly way
                Column(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    
                    if (dz != null) {
                        // We simulate a DzoUiState for the viewer
                        val mockDzoUiState = com.V2Skydivejump.app.ui.dzo.DzoUiState(
                            user = com.V2Skydivejump.app.database.entities.UserEntity(
                                userId = dz.id,
                                name = dz.dzName,
                                role = "DZ_OPERATOR",
                                licenseNumber = "",
                                dzName = dz.dzName,
                                dzLocation = dz.location,
                                dzCity = dz.city,
                                dzProvince = dz.province,
                                dzCountry = dz.country,
                                dzWebsite = dz.website,
                                dzMobileNumber = dz.contactNumbers,
                                dzEmail = dz.emailAddress,
                                country = dz.country
                            ),
                            flightSchedules = uiState.flightSchedules.filter { it.dzId == dz.id },
                            aircrafts = uiState.aircrafts,
                            dzInventory = uiState.dzInventory.filter { it.dzId == dz.id }
                        )
                        
                        com.V2Skydivejump.app.ui.dzo.DzoProfileScreen(
                            uiState = mockDzoUiState,
                            onLogout = {}, // Hide or ignore in this view
                            onBookFlight = { scheduleId -> viewModel.bookFlight(scheduleId) },
                            isPublicView = true
                        )
                    }
                }
            }
            composable("calendar") {
                EventCalendarScreen(
                    events = uiState.events,
                    userRole = UserRole.JUMPER,
                    onPostEventClick = { },
                    onRsvpClick = { }
                )
            }
            composable("booking") {
                JumperBookingScreen(
                    bookingGroups = uiState.bookingGroups,
                    onBookClick = { scheduleId -> 
                        viewModel.selectScheduleForRental(scheduleId)
                        navController.navigate("equipment_requirements")
                    }
                )
            }
            composable("equipment_requirements") {
                val dzId = uiState.selectedScheduleId?.let { id -> 
                    uiState.flightSchedules.find { it.scheduleId == id }?.dzId 
                } ?: ""
                
                EquipmentRequirementsScreen(
                    availableGear = uiState.dzInventory.filter { it.dzId == dzId && it.isForRent },
                    onBack = { navController.popBackStack() },
                    onConfirm = { items, total ->
                        viewModel.finalizeBookingWithRental(items, total)
                        navController.navigate("booking_review")
                    }
                )
            }
            composable("booking_review") {
                val schedule = uiState.flightSchedules.find { it.scheduleId == uiState.selectedScheduleId }
                // Use a heuristic for base price if not explicitly in schedule
                val basePrice = if (uiState.user?.role == "STUDENT") 5000.0 else 1200.0
                
                BookingReviewScreen(
                    user = uiState.user,
                    basePrice = basePrice,
                    rentalItems = uiState.currentRentalSelection,
                    rentalTotal = uiState.currentRentalTotal,
                    availablePromos = uiState.promotions,
                    convenienceFeeType = uiState.convenienceFeeType,
                    convenienceFeeRate = uiState.convenienceFeeRate,
                    onBack = { navController.popBackStack() },
                    onConfirm = { promo, total, points ->
                        viewModel.confirmBooking(promo, total, points)
                        navController.navigate("log") {
                            popUpTo("booking") { inclusive = true }
                        }
                    }
                )
            }
            composable("ai_insights") {
                AiPerformanceScreen(uiState = uiState)
            }
            composable("matrix") {
                PerformanceMatrixScreen(
                    uiState = uiState,
                    onBackClick = { 
                        currentTab = 0
                        navController.navigate("log") 
                    }
                )
            }
            composable("awards") {
                BadgesAwardsScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onClaimBadge = { badgeId, docUrl -> viewModel.claimMilitaryBadge(badgeId, docUrl) }
                )
            }
            composable("jacket") {
                BadgesAwardsScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onClaimBadge = { badgeId, docUrl -> viewModel.claimMilitaryBadge(badgeId, docUrl) }
                )
            }
            composable("altimeter") {
                BluetoothPermissionHandler {
                    DigitalLinkHubScreen(
                        viewModel = remember { DigitalLinkViewModel(database) },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable("profile") {
                JumperProfileScreen(
                    uiState = uiState,
                    onUpdateProfile = { user -> viewModel.updateUserProfile(user) },
                    onUpdatePrivacy = { privacy -> viewModel.updatePrivacySettings(privacy) },
                    onAddRating = { rating -> viewModel.addRating(rating) },
                    onDeleteRating = { rating -> viewModel.deleteRating(rating) },
                    onVerifyRating = { rating, name, lic -> viewModel.verifyRating(rating, name, lic) },
                    onAddFederation = { fed -> viewModel.addFederation(fed) },
                    onDeleteFederation = { fed -> viewModel.deleteFederation(fed) },
                    onAddFunds = { amount, method -> viewModel.addFunds(amount, method) },
                    onUpdateProfilePicture = { url -> 
                        viewModel.uploadProfileMedia("profile", url)
                    },
                    onUpdateBackgroundPicture = { url -> 
                        viewModel.uploadProfileMedia("background", url)
                    },
                    onInfoClick = { navController.navigate("jumper_info") },
                    onLicenseClick = { showLicenseTracker = true },
                    onGearClick = { navController.navigate("jumper_gear") },
                    onUsedDzClick = { navController.navigate("used_dz") },
                    onAwardsClick = { navController.navigate("awards") },
                    onMembershipClick = { currentTab = 14; navController.navigate("membership") },
                    onSyllabusClick = { navController.navigate("syllabus") },
                    onNfcClick = { navController.navigate("altimeter") },
                    onBookFlight = { scheduleId -> viewModel.bookFlight(scheduleId) },
                    hasStaffAccess = hasStaffAccess,
                    onStaffClick = onSwitchToStaff
                )
            }
            composable("jumper_info") {
                JumperInfoScreen(
                    uiState = uiState,
                    onUpdateProfile = { user -> viewModel.updateUserProfile(user) },
                    onUpdatePrivacy = { privacy -> viewModel.updatePrivacySettings(privacy) },
                    onAddRating = { rating -> viewModel.addRating(rating) },
                    onDeleteRating = { rating -> viewModel.deleteRating(rating) },
                    onAddFederation = { fed -> viewModel.addFederation(fed) },
                    onDeleteFederation = { fed -> viewModel.deleteFederation(fed) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("used_dz") {
                UsedDzScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("membership") {
                MembershipScreen(
                    uiState = uiState,
                    onUpgradeClick = { /* Link to stripe/in-app purchase in future */ },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("jumper_gear") {
                JumperGearScreen(
                    uiState = uiState,
                    onSaveGear = { gear -> viewModel.saveGearItem(gear) },
                    onDeleteGear = { gear -> viewModel.deleteGearItem(gear) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("awards") {
                BadgesAwardsScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("syllabus") {
                StudentSyllabusScreen(
                    userName = uiState.user?.name ?: "Student",
                    skills = uiState.studentSkills,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("instructor_verify/{studentId}/{studentName}") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
                val studentName = backStackEntry.arguments?.getString("studentName") ?: "Student"
                
                // Fetch student specific skills
                // For demo, we filter the local state, in real app fetch from DB
                InstructorSkillMatrix(
                    studentName = studentName,
                    skills = uiState.studentSkills.filter { it.userId == studentId },
                    onToggleSkill = { viewModel.toggleStudentSkill(it) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("chat_list") {
                ChatListScreen(
                    users = uiState.registeredUsers,
                    currentUserId = uiState.user?.userId,
                    onChatClick = { userId -> navController.navigate("chat_detail/$userId") }
                )
            }
            composable("chat_detail/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val recipient = uiState.registeredUsers.find { it.userId == userId }
                val recipientName = recipient?.let { user ->
                    user.name.ifBlank { user.screenName ?: userId }
                } ?: userId
                val messages by viewModel.getChatMessages(userId).collectAsState(emptyList())
                ChatDetailScreen(
                    recipientName = recipientName,
                    currentUserId = uiState.user?.userId,
                    messages = messages,
                    onSendMessage = { text -> viewModel.sendMessage(userId, text) }
                )
            }
            composable("notifications") {
                NotificationScreen(
                    notifications = uiState.notifications,
                    onMarkAsRead = { viewModel.markNotificationAsRead(it) }
                )
            }
        }
    }

    if (showLicenseTracker) {
        LicenseTrackerDialog(
            uiState = uiState,
            onDismiss = { showLicenseTracker = false }
        )
    }
}
