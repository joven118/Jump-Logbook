package com.V2Skydivejump.app.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.V2Skydivejump.app.AppFooter
import com.V2Skydivejump.app.AppLogo
import com.V2Skydivejump.app.SessionViewModel
import com.V2Skydivejump.app.UserRole
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
        onTimeout()
    }
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = visible, enter = fadeIn(animationSpec = tween(1000))) {
            AppLogo(modifier = Modifier.size(250.dp))
        }
    }
}

@Composable
fun LoginScreen(
    sessionViewModel: SessionViewModel,
    onLogin: (String, String) -> Unit,
    onSignUp: (String, String, UserRole, String?, String?) -> Unit,
    onNavigateToDev: () -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        val isWide = this.maxWidth > 900.dp
        
        if (isWide) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    // Section 1: Login Features
                    Box(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                        LoginSection(sessionViewModel, onLogin, onSignUp, onNavigateToDev)
                    }
                    
                    // Section 2: Features Showcase (Logbook + DZ Ops)
                    Box(modifier = Modifier.weight(1.5f).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                        FeaturesShowcaseSection()
                    }
                    
                    // Section 3: Promo Slideshow
                    Box(modifier = Modifier.weight(1.3f).fillMaxHeight().background(Color.Black)) {
                        PromoSlideshowSection()
                    }
                }
                AppFooter()
            }
        } else {
            // Mobile: Vertical Stack
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                LoginSection(sessionViewModel, onLogin, onSignUp, onNavigateToDev)
                HorizontalDivider()
                FeaturesShowcaseSection()
                HorizontalDivider()
                Box(modifier = Modifier.height(300.dp).fillMaxWidth().background(Color.Black)) {
                    PromoSlideshowSection()
                }
                AppFooter()
            }
        }
    }
}

@Composable
fun LoginSection(
    sessionViewModel: SessionViewModel,
    onLogin: (String, String) -> Unit,
    onSignUp: (String, String, UserRole, String?, String?) -> Unit,
    onNavigateToDev: () -> Unit = {}
) {
    var email by remember { mutableStateOf(sessionViewModel.getRememberedEmail()) }
    var password by remember { mutableStateOf("") }
    var rememberEmail by remember { mutableStateOf(email.isNotEmpty()) }
    var isSignUp by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.JUMPER) }
    
    var dzName by remember { mutableStateOf("") }

    val error by sessionViewModel.error.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppLogo(modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isSignUp) "Join Jump Logbook" else "Welcome Back",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        if (error != null) {
            Text(text = error!!, color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isSignUp) {
            Row(modifier = Modifier.padding(bottom = 12.dp)) {
                UserRole.entries.filter { it == UserRole.JUMPER || it == UserRole.DZ_OPERATOR }.forEach { role ->
                    FilterChip(
                        selected = selectedRole == role,
                        onClick = { selectedRole = role },
                        label = { Text(role.name.replace("_", " ")) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            
            if (selectedRole == UserRole.DZ_OPERATOR) {
                OutlinedTextField(
                    value = dzName, 
                    onValueChange = { dzName = it }, 
                    label = { Text("Dropzone Name") }, 
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }
        }

        OutlinedTextField(
            value = email, 
            onValueChange = { email = it }, 
            label = { Text("Email") }, 
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                }
            }
        )
        
        if (!isSignUp) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = rememberEmail, onCheckedChange = { rememberEmail = it })
                Text("Remember Email", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isSignUp) {
                    onSignUp(email, password, selectedRole, if (selectedRole == UserRole.DZ_OPERATOR) dzName else null, null)
                } else {
                    if (rememberEmail) sessionViewModel.saveRememberedEmail(email) else sessionViewModel.clearRememberedEmail()
                    onLogin(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(if (isSignUp) "SIGN UP" else "LOGIN")
        }

        TextButton(onClick = { isSignUp = !isSignUp }) {
            Text(if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up")
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Secret Dev Mode Entry
        IconButton(
            onClick = onNavigateToDev,
            modifier = Modifier.size(24.dp),
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.LightGray.copy(alpha = 0.3f))
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
        }
    }
}

@Composable
fun FeaturesShowcaseSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        FeatureBlock(
            title = "Jump Logbook",
            icon = Icons.Default.MenuBook,
            features = listOf(
                "Automated Telemetry Sync (BLE/WiFi)",
                "Digital Syllabus & Instructor Sign-off",
                "Advanced Performance Analytics",
                "Global Season Leaderboards"
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        FeatureBlock(
            title = "DZ Operations",
            icon = Icons.Default.AirplanemodeActive,
            features = listOf(
                "Zero-Touch Manifest Automation",
                "AI Weather-Safety Interlock",
                "CFO Layer Business Analytics",
                "Real-time Fleet & Fuel Tracking"
            )
        )
    }
}

@Composable
fun FeatureBlock(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, features: List<String>) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        features.forEach { feature ->
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp).padding(top = 2.dp))
                Spacer(Modifier.width(12.dp))
                Text(feature, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun PromoSlideshowSection() {
    val promoImages = listOf(
        "https://images.unsplash.com/photo-1521033332975-840a1b3297fe?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1541339907198-e08756ebafe3?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1473960197011-2a9f4ea4c118?auto=format&fit=crop&w=800&q=80"
    )
    
    var currentImageIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentImageIndex = (currentImageIndex + 1) % promoImages.size
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = promoImages[currentImageIndex],
            transitionSpec = { fadeIn(tween(1000)) togetherWith fadeOut(tween(1000)) }
        ) { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = "Promo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp)
        ) {
            Text(
                "Aviation-Grade Dropzone Operations",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
