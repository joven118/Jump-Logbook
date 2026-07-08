package com.V2Skydivejump.app.ui.jumper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.SkydiveTopAppBar
import com.V2Skydivejump.app.utils.CurrencyStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceMatrixScreen(
    uiState: JumperUiState,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SkydiveTopAppBar(
                title = "Performance Matrix",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Section 1: Currency & Readiness
            item {
                SectionHeader("Currency & Readiness")
                CurrencyStatusCard(uiState)
            }

            // Section 2: Gear Maintenance
            item {
                SectionHeader("Gear Maintenance")
                GearMaintenanceList(uiState)
            }

            // Section 3: Telemetry Insights
            item {
                SectionHeader("Telemetry Insights")
                TelemetryInsightsPlaceholders()
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun CurrencyStatusCard(uiState: JumperUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val color = when (uiState.currencyStatus) {
                    CurrencyStatus.GREEN -> Color(0xFF2E7D32)
                    CurrencyStatus.YELLOW -> Color(0xFFF9A825)
                    CurrencyStatus.RED -> Color(0xFFC62828)
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Status: ${uiState.currencyStatus.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Last jump: 12 days ago", // This would ideally come from TimeUtils based on uiState.jumps
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Next currency check required in 18 days.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun GearMaintenanceList(uiState: JumperUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // In a real app, this data would come from a list of Gear objects in uiState
        GearItem(name = "Main Canopy (Saber 3)", jumps = 120, status = "Good", statusColor = Color(0xFF2E7D32))
        GearItem(name = "Container (Javelin Odyssey)", jumps = 450, status = "Inspection Due", statusColor = Color(0xFFF9A825))
        GearItem(name = "AAD (Cypres 2)", jumps = 0, status = "Service in 2yrs", statusColor = Color.Gray)
        
        if (uiState.canopyAlert.isNotEmpty()) {
            Surface(
                color = if (uiState.canopyAlert.contains("Warning")) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.canopyAlert,
                    modifier = Modifier.padding(12.dp),
                    color = if (uiState.canopyAlert.contains("Warning")) Color(0xFFC62828) else Color.Unspecified,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun GearItem(name: String, jumps: Int, status: String, statusColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = name, fontWeight = FontWeight.SemiBold)
                Text(text = "$jumps Jumps", style = MaterialTheme.typography.bodySmall)
            }
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = status.uppercase(),
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TelemetryInsightsPlaceholders() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InsightPlaceholderCard("Avg. Deployment Altitude (Last 50)")
        InsightPlaceholderCard("Vertical Speed Profile")
    }
}

@Composable
private fun InsightPlaceholderCard(title: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title, style = MaterialTheme.typography.bodySmall)
                Text(text = "[ Graph Placeholder ]", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            }
        }
    }
}
