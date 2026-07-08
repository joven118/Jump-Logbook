package com.V2Skydivejump.app.ui.jumper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.utils.PerformanceAnalyticsEngine

@Composable
fun AiPerformanceScreen(uiState: JumperUiState) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AI Performance Insights",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Insight 1: Progression Analysis
        val progressionInsight = PerformanceAnalyticsEngine.getProgressionInsight(uiState.jumps)
        InsightCard(
            title = "Skill Progression",
            content = progressionInsight,
            icon = Icons.Default.Star,
            color = Color(0xFF1976D2)
        )

        // Insight 2: Milestone Prediction
        val milestoneInsight = PerformanceAnalyticsEngine.getMilestonePrediction(uiState.jumps)
        InsightCard(
            title = "Milestone Prediction",
            content = milestoneInsight,
            icon = Icons.Default.Star,
            color = Color(0xFFFBC02D)
        )

        // Insight 3: Safety & Stability
        val safetyInsight = PerformanceAnalyticsEngine.getSafetyInsight(uiState.jumps)
        InsightCard(
            title = "Safety & Stability",
            content = safetyInsight,
            icon = Icons.Default.Info,
            color = Color(0xFF2E7D32)
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Note: These insights are generated based on your logged jump patterns and general skydiving progression trends.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun InsightCard(
    title: String,
    content: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = content, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
