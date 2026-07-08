package com.V2Skydivejump.app

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.V2Skydivejump.app.data.*
import com.V2Skydivejump.app.database.AppDatabase
import com.V2Skydivejump.app.database.getDatabaseBuilder
import com.V2Skydivejump.app.ui.theme.SkydiveJumpTheme
import com.V2Skydivejump.app.ui.auth.LoginScreen
import com.V2Skydivejump.app.ui.auth.SplashScreen
import com.V2Skydivejump.app.ui.dzo.*
import com.V2Skydivejump.app.ui.admin.*
import com.V2Skydivejump.app.ui.jumper.JumperMainScreen
import com.V2Skydivejump.app.ui.jumper.JumperViewModel
import com.V2Skydivejump.app.ui.support.AboutScreen
import com.V2Skydivejump.app.ui.support.FaqScreen
import com.V2Skydivejump.app.testing.TestDzoInitializer
import com.V2Skydivejump.app.testing.DataManagementScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.painterResource

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository(scope) }
    val jumpLogRepository = remember { JumpLogRepository() }
    val gearRepository = remember { GearRepository() }
    val badgeRepository = remember { BadgeRepository() }
    val professionalRepository = remember { ProfessionalRepository() }
    val offlineSyncRepository = remember { OfflineSyncRepository(userRepository, gearRepository, professionalRepository) }

    val database = remember {
        getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    val syncManager = remember {
        SyncManager(
            database,
            userRepository,
            jumpLogRepository,
            gearRepository,
            professionalRepository,
            offlineSyncRepository,
            scope
        )
    }

    val sessionViewModel = remember { SessionViewModel(userRepository) }
    val testInitializer = remember { TestDzoInitializer(scope, database, userRepository, professionalRepository) }

    val dzoViewModel = remember { DzoViewModel(database, userRepository, professionalRepository, jumpLogRepository, offlineSyncRepository) }
    val jumperViewModel = remember { 
        JumperViewModel(
            database, 
            userRepository,
            jumpLogRepository,
            gearRepository,
            badgeRepository,
            professionalRepository,
            offlineSyncRepository
        ) 
    }

    val sessionStatus by sessionViewModel.sessionStatus.collectAsState()
    val currentUser by sessionViewModel.currentUser.collectAsState()
    val memberships by sessionViewModel.memberships.collectAsState()

    // Automatic Cloud-to-Local Data Hydration on Authentication
    LaunchedEffect(sessionStatus, currentUser?.userId) {
        if (sessionStatus is SessionStatus.Authenticated && currentUser != null) {
            syncManager.performFullHydration()
        }
    }

    SkydiveJumpTheme {
        val navController = rememberNavController()

        // CRITICAL FIX: Reactive Navigation Guard
        // This monitors role changes and redirects the user if they are on the wrong main screen
        LaunchedEffect(currentUser, memberships) {
            if (sessionStatus is SessionStatus.Authenticated) {
                val hasDzoAccess = currentUser?.role == "DZ_OPERATOR" || memberships.isNotEmpty()
                val currentRoute = navController.currentDestination?.route
                
                if (hasDzoAccess && currentRoute == "jumper_main") {
                    println("App: Redirecting to DZO Main due to role confirmation")
                    navController.navigate("dzo_main") {
                        popUpTo("jumper_main") { inclusive = true }
                    }
                }
            }
        }
        
        // Root logic to determine initial screen on app startup
        val rootStartDestination = remember(sessionStatus, currentUser, memberships) {
            println("App DIAGNOSTIC: Recalculating Root Destination. Status=$sessionStatus, Role=${currentUser?.role}, HasMemberships=${memberships.isNotEmpty()}")
            
            if (sessionStatus is SessionStatus.Authenticated) {
                // If data is still loading (no currentUser yet), stay on splash or show loading
                if (currentUser == null && memberships.isEmpty()) {
                    "splash"
                } else {
                    val role = currentUser?.role
                    when {
                        role == "ADMIN" -> "admin_main"
                        role == "DZ_OPERATOR" || memberships.isNotEmpty() -> "dzo_main"
                        else -> "jumper_main"
                    }
                }
            } else if (sessionStatus is SessionStatus.Unauthenticated) {
                "login"
            } else {
                "splash"
            }
        }

        NavHost(
            navController = navController,
            startDestination = rootStartDestination
        ) {
            composable("splash") {
                SplashScreen(onTimeout = {
                    navController.navigate(if (sessionStatus is SessionStatus.Authenticated) {
                        if (currentUser?.role == "DZ_OPERATOR" || memberships.isNotEmpty()) "dzo_main" else "jumper_main"
                    } else "login") {
                        popUpTo("splash") { inclusive = true }
                    }
                })
            }
            
            composable("login") {
                LoginScreen(
                    sessionViewModel = sessionViewModel,
                    onLogin = { email, pass -> sessionViewModel.signIn(email, pass) },
                    onSignUp = { email, pass, role, name, loc -> 
                        sessionViewModel.signUp(email, pass, role, name, loc) 
                    },
                    onNavigateToDev = { navController.navigate("dev_tools") }
                )
            }

            composable("dzo_main") {
                LaunchedEffect(memberships) {
                    if (currentUser?.role != "DZ_OPERATOR" && memberships.isNotEmpty()) {
                        dzoViewModel.setTargetDzId(memberships.first().dzId)
                    }
                }
                DzoMainScreen(
                    viewModel = dzoViewModel,
                    onLogout = {
                        sessionViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("dzo_main") { inclusive = true }
                        }
                    },
                    onNavigateToFaq = {
                        navController.navigate("faq")
                    },
                    onNavigateToAbout = {
                        navController.navigate("about")
                    }
                )
            }

            composable("jumper_main") {
                JumperMainScreen(
                    database = database,
                    viewModel = jumperViewModel,
                    onLogout = {
                        sessionViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("jumper_main") { inclusive = true }
                        }
                    },
                    hasStaffAccess = memberships.isNotEmpty(),
                    onSwitchToStaff = {
                        navController.navigate("dzo_main")
                    },
                    onNavigateToFaq = {
                        navController.navigate("faq")
                    },
                    onNavigateToAbout = {
                        navController.navigate("about")
                    }
                )
            }

            composable("faq") {
                FaqScreen(onBack = { navController.popBackStack() })
            }

            composable("about") {
                AboutScreen(onBack = { navController.popBackStack() })
            }

            composable("admin_main") {
                val adminViewModel = remember { AdminViewModel(database, AdminRepository()) }
                val adminUiState by adminViewModel.uiState.collectAsState()
                
                AdminDashboardScreen(
                    uiState = adminUiState,
                    onVerifyUser = { adminViewModel.verifyUser(it) },
                    onVerifyBadge = { adminViewModel.verifyBadge(it) },
                    onAddAdmin = { adminViewModel.addAdmin(it) },
                    onUpdateConfig = { adminViewModel.updateConfig(it) },
                    onLogout = {
                        sessionViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("admin_main") { inclusive = true }
                        }
                    }
                )
            }

            composable("dev_tools") {
                DataManagementScreen(
                    initializer = testInitializer,
                    sessionViewModel = sessionViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
