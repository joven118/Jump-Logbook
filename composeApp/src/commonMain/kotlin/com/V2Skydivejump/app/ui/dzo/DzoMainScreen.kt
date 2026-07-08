package com.V2Skydivejump.app.ui.dzo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.V2Skydivejump.app.AppFooter
import com.V2Skydivejump.app.SkydiveTopAppBar
import com.V2Skydivejump.app.database.AppDatabase
import com.V2Skydivejump.app.database.entities.*
import com.V2Skydivejump.app.data.UserRepository
import com.V2Skydivejump.app.ui.dzo.SafetyAlert
import com.V2Skydivejump.app.ui.dzo.SafetyLevel
import com.V2Skydivejump.app.ui.dzo.DzoFinancialDashboard
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DzoMainScreen(
    viewModel: DzoViewModel,
    onLogout: () -> Unit,
    onNavigateToFaq: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    var currentTab by remember { mutableStateOf(0) }

    val screenTitle = when(currentTab) {
        0 -> "Dashboard"
        1 -> "Operations"
        10 -> "Staff Terminal"
        11 -> "Boarding Scanner"
        2 -> "Directory"
        3 -> "Events"
        4 -> "Finance"
        9 -> "AI Insights"
        8 -> "Promotions"
        5 -> "Profile"
        6 -> "Messages"
        7 -> "Notifications"
        else -> "DZO Dashboard"
    }

    Scaffold(
        topBar = {
            SkydiveTopAppBar(
                title = screenTitle,
                actions = {
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(Icons.Default.Info, contentDescription = "About App")
                    }
                    IconButton(onClick = onNavigateToFaq) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "FAQs")
                    }
                    IconButton(onClick = { navController.navigate("chat") }) {
                        Icon(Icons.Default.Email, contentDescription = "Chat")
                    }
                    IconButton(onClick = { navController.navigate("notifications") }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
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
                        onClick = { currentTab = 0; navController.navigate("dashboard") },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1; navController.navigate("operations") },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Ops") }
                    )
                    
                    val userRole = uiState.user?.role ?: "JUMPER"
                    if (userRole == "PILOT" || userRole == "INSTRUCTOR" || userRole == "DZ_OPERATOR") {
                        NavigationBarItem(
                            selected = currentTab == 10,
                            onClick = { currentTab = 10; navController.navigate("staff_duty") },
                            icon = { Icon(Icons.Default.Engineering, contentDescription = null) },
                            label = { Text("Duty") }
                        )
                        NavigationBarItem(
                            selected = currentTab == 11,
                            onClick = { currentTab = 11; navController.navigate("qr_scanner") },
                            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                            label = { Text("Scan") }
                        )
                    }

                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2; navController.navigate("directory") },
                        icon = { Icon(Icons.Default.Place, contentDescription = null) },
                        label = { Text("Directory") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3; navController.navigate("calendar") },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        label = { Text("Events") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 4,
                        onClick = { currentTab = 4; navController.navigate("finance") },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                        label = { Text("Finance") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 8,
                        onClick = { currentTab = 8; navController.navigate("promotions") },
                        icon = { Icon(Icons.Default.LocalOffer, contentDescription = null) },
                        label = { Text("Promo") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 9,
                        onClick = { currentTab = 9; navController.navigate("ai_insights") },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                        label = { Text("AI") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 5,
                        onClick = { currentTab = 5; navController.navigate("profile") },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Profile") }
                    )
                }
                AppFooter()
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DzoDashboardScreen(
                    uiState = uiState,
                    onSetWeatherSource = { viewModel.setWeatherSource(it) }
                )
            }
            composable("operations") {
                DzoOperationsScreen(
                    uiState = uiState,
                    onAddAircraft = { n, t, m, c -> viewModel.addAircraft(n, t, m, c) },
                    onAddStaff = { n, e, r -> viewModel.addDzStaff(n, e, r) },
                    onManifestJumper = { j, l -> viewModel.manifestJumper(j, l) },
                    onUnmanifestJumper = { j, l -> viewModel.unmanifestJumper(j, l) },
                    onCheckIn = { j, l -> viewModel.checkInJumper(j, l) },
                    onBookFlight = { viewModel.bookFlight(it) },
                    onAddLoad = { num, ac, slots, pilot -> viewModel.addLoad(num, ac, slots, pilot) },
                    onUpdateLoadStatus = { id, status -> viewModel.updateLoadStatus(id, status) },
                    onOpenScanner = { navController.navigate("qr_scanner") }
                )
            }
            composable("inventory") {
                DzInventoryScreen(
                    inventory = uiState.dzInventory,
                    onAddInventory = { viewModel.addDzInventory(it) },
                    onUpdateInventory = { item, uri -> viewModel.updateDzInventory(item, uri) },
                    onDeleteInventory = { viewModel.deleteDzInventory(it) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("aircraft_list") {
                DzInventoryScreen(
                    inventory = uiState.dzInventory,
                    onAddInventory = { viewModel.addDzInventory(it) },
                    onUpdateInventory = { item, uri -> viewModel.updateDzInventory(item, uri) },
                    onDeleteInventory = { viewModel.deleteDzInventory(it) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("waivers") {
                DzWaiverManagerScreen(
                    dzoUser = uiState.user,
                    waivers = uiState.dzWaivers,
                    onUpsert = { t, c, id -> viewModel.upsertWaiver(t, c, id) },
                    onDeactivate = { viewModel.deactivateWaiver(it) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("facilities") {
                DzFacilitiesScreen(
                    facilities = uiState.dzFacilities,
                    onAddFacility = { n, d, p -> viewModel.addDzFacility(n, d, p) },
                    onUpdateFacility = { f, p -> viewModel.updateDzFacility(f, p) },
                    onDeleteFacility = { viewModel.deleteDzFacility(it) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("safety") {
                SafetyComplianceModule(
                    uiState = uiState,
                    onAddIncident = { viewModel.upsertIncidentReport(it) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("rigger_seal") {
                uiState.user?.let { user ->
                    RiggerSealGeneratorScreen(
                        user = user,
                        onSaveSeal = { symbol, license -> 
                            viewModel.updateRiggerSeal(symbol, license)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable("repack_queue") {
                GearRepackLogScreen(
                    gearList = uiState.dzInventory.filter { it.category == "Parts" }.map { 
                        // Real app: we'd have a proper mapper from DzInventory to UserGear or a unified type
                        UserGearEntity(gearId = it.id.toString(), userId = "", category = it.subCategory ?: "Parts", make = it.makeModel, model = it.makeModel, serialNumber = it.serialNumber)
                    },
                    riggerName = uiState.user?.name ?: "Rigger",
                    onVerifyRepack = { /* Handled in VM */ },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("profile") {
                DzoProfileScreen(
                    uiState = uiState,
                    onLogout = onLogout,
                    onUpdateProfile = { viewModel.updateProfile(it) },
                    onUpdatePrivacy = { viewModel.updatePrivacySettings(it) },
                    onUpdateProfilePicture = { viewModel.uploadProfileMedia("profile", it) },
                    onUpdateBackgroundPicture = { viewModel.uploadProfileMedia("background", it) },
                    onAddSchedule = { d, f, l, a, s -> viewModel.addFlightSchedule(d, f, l, a, s) },
                    onBookFlight = { viewModel.bookFlight(it) },
                    onAddFacility = { n, d, p -> viewModel.addDzFacility(n, d, p) },
                    onDeleteFacility = { viewModel.deleteDzFacility(it) },
                    onNavigateToFacilities = { navController.navigate("facilities") },
                    onAddInventory = { viewModel.addDzInventory(it) },
                    onUpdateInventory = { item, uri -> viewModel.updateDzInventory(item, uri) },
                    onDeleteInventory = { viewModel.deleteDzInventory(it) },
                    onNavigateToInventory = { navController.navigate("inventory") },
                    onNavigateToAircraft = { navController.navigate("aircraft_list") },
                    onNavigateToWaivers = { navController.navigate("waivers") },
                    onNavigateToSafety = { navController.navigate("safety") },
                    onNavigateToRentalSale = { navController.navigate("rental_sale") },
                    onAddRating = { s, c -> viewModel.addDzRating(s, c) },
                    onToggleFollow = { viewModel.toggleFollow() },
                    navController = navController
                )
            }
            composable("rental_sale") {
                DzInventoryScreen(
                    inventory = uiState.dzInventory,
                    onAddInventory = { viewModel.addDzInventory(it) },
                    onUpdateInventory = { item, uri -> viewModel.updateDzInventory(item, uri) },
                    onDeleteInventory = { viewModel.deleteDzInventory(it) },
                    onBack = { navController.popBackStack() },
                    showOnlyMarketable = true
                )
            }
            composable("directory") {
                // ...
            }
            composable("calendar") {
                // ...
            }
            composable("finance") {
                DzoFinancialDashboard(
                    uiState = uiState,
                    onUpdateFinance = { viewModel.updateFinanceReport(it) },
                    onUpdateCashFlow = { viewModel.updateCashFlowReport(it) },
                    onUpdateBalanceSheet = { viewModel.updateBalanceSheetReport(it) }
                )
            }
            composable("promotions") {
                PromoManagementModule(
                    uiState = uiState,
                    onUpsertPromo = { viewModel.upsertPromotion(it) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("ai_insights") {
                DzoAiInsightsScreen(uiState)
            }
            composable("staff_duty") {
                StaffDashboardScreen(
                    uiState = uiState,
                    onUpdateLoadStatus = { id, status -> viewModel.updateLoadStatus(id, status) },
                    onVerifyStudent = { id, name -> navController.navigate("instructor_verify/$id/$name") },
                    onManageSeal = { navController.navigate("rigger_seal") },
                    onOpenRepackQueue = { navController.navigate("repack_queue") },
                    onNavigateToFinance = { currentTab = 4; navController.navigate("finance") },
                    onNavigateToSafety = { navController.navigate("safety") },
                    onNavigateToFacilities = { navController.navigate("facilities") },
                    onNavigateToInventory = { navController.navigate("inventory") }
                )
            }
            composable("qr_scanner") {
                BoardingScannerScreen(
                    uiState = uiState,
                    onBoardJumper = { jumper: ManifestJumper, loadId: Int -> viewModel.checkInJumper(jumper, loadId) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("manifest_tv") {
                ManifestTvScreen(
                    uiState = uiState,
                    onClose = { navController.popBackStack() }
                )
            }
            composable("chat") {
                ChatListScreen(
                    onBack = { navController.popBackStack() },
                    onChatSelected = { userId -> /* Navigate to detail */ }
                )
            }
            composable("notifications") {
                NotificationScreen(
                    notifications = uiState.safetyAlerts, // Or actual notifications
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(onBack: () -> Unit, onChatSelected: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize(), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No active conversations", color = Color.Gray)
            Text("Start messaging jumpers or staff.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(notifications: List<SafetyAlert>, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No notifications", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(notifications) { notification ->
                    ListItem(
                        headlineContent = { Text(notification.message) },
                        supportingContent = { Text(com.V2Skydivejump.app.TimeUtils.formatEpochMillis(notification.timestamp)) },
                        leadingContent = {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (notification.level == SafetyLevel.DANGER) Color.Red else Color.Unspecified
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

