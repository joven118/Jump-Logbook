package com.V2Skydivejump.app.ui.support

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import v2skydivejump.composeapp.generated.resources.Res
import v2skydivejump.composeapp.generated.resources.logo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About V2 Skydive Jump") },
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
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp))
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "JUMP LOGBOOK",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Version 2.0.56 (Aviation Grade)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            Spacer(Modifier.height(32.dp))

            AboutSection(
                title = "Our Mission",
                description = "To elevate the skydiving experience through intelligent automation, ensuring maximum safety and operational efficiency for both jumpers and dropzone operators worldwide."
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Core Pillars",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))

            PillarItem(Icons.Default.Sync, "Zero-Touch Manifest", "Automated load management, QR/NFC boarding, and real-time sync with pilot controls.")
            PillarItem(Icons.Default.Shield, "AI Safety Interlock", "Predictive weather monitoring and automated manifest holds based on real-time environmental data.")
            PillarItem(Icons.Default.AccountBalance, "CFO Layer Analytics", "Deep business intelligence, asset ROI tracking, and dynamic pricing models for DZO growth.")
            PillarItem(Icons.Default.MenuBook, "Digital Training Syllabus", "Comprehensive skill tracking for AFF and licensing, replacing manual paper records.")
            PillarItem(Icons.Default.Bluetooth, "Digital Link Hub", "Seamless telemetry synchronization from industry-leading altimeters directly to your logbook.")

            Spacer(Modifier.height(48.dp))
            
            Text(
                text = "Developed for the global skydiving community.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Text(
                text = "© 2024 V2 Skydive Technologies",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun AboutSection(title: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun PillarItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
