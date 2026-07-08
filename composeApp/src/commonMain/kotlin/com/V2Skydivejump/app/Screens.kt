package com.V2Skydivejump.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import v2skydivejump.composeapp.generated.resources.Res
import v2skydivejump.composeapp.generated.resources.logo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkydiveTopAppBar(
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Displaying your logo.png in the App Bar
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // This displays your logo.png file with native transparency
        Image(
            painter = painterResource(Res.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
    }
}

@Composable
fun AppFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp), thickness = 0.5.dp, color = Color.LightGray)
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Jump Logbook",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Book. Jump. Log. Connect.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "jumplogbook.com // jumplogbook@gmail.com",
            style = MaterialTheme.typography.labelSmall,
            color = Color.DarkGray
        )

        Text(
            text = "© 2026 Jump Logbook. All Rights Reserved.",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 8.sp,
            color = Color.LightGray
        )
    }
}

@Composable
fun RoleSelectionScreen(onRoleSelected: (UserRole) -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppLogo(modifier = Modifier.size(200.dp))
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(text = "Blue Skies! Welcome to Jump logbook", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(text = "Sign up as:", color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { onRoleSelected(UserRole.JUMPER) },
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Login as Jumper", color = Color.White, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { onRoleSelected(UserRole.DZ_OPERATOR) },
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(text = "Login as DZ Operator", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.weight(1f))
            AppFooter()
        }
    }
}

@Composable
fun JumperDashboardScreen(onLogout: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Jumper Dashboard", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "View your logs and upcoming jumps here.")
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onLogout) {
                Text(text = "Logout")
            }
        }
    }
}

@Composable
fun DzoDashboardScreen(onLogout: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "DZ Operator Dashboard", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Manage manifests and aircraft operations.")
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onLogout) {
                Text(text = "Logout")
            }
        }
    }
}
