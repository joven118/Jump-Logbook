package com.V2Skydivejump.app.ui.dzo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DzoAiInsightsScreen(uiState: DzoUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AiHeaderSection()
        
        if (uiState.aiRecommendations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Generating real-time insights...", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(uiState.aiRecommendations) { rec ->
                    AiRecommendationCard(rec)
                }
            }
        }
    }
}

@Composable
fun AiHeaderSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("V2 AI Operations Manager", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text("Analyzing your DZ data for real-time improvements.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AiRecommendationCard(rec: AiRecommendation) {
    val accentColor = when (rec.category) {
        "OPERATIONAL" -> Color(0xFF1976D2)
        "FINANCIAL" -> Color(0xFF2E7D32)
        "SAFETY" -> Color(0xFFD32F2F)
        "MARKETING" -> Color(0xFFFFA000)
        else -> Color.Gray
    }

    val icon = when (rec.category) {
        "OPERATIONAL" -> Icons.Default.Speed
        "FINANCIAL" -> Icons.Default.TrendingUp
        "SAFETY" -> Icons.Default.Shield
        "MARKETING" -> Icons.Default.AdsClick
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(rec.title, fontWeight = FontWeight.Bold, color = accentColor, style = MaterialTheme.typography.titleMedium)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Impact: ${rec.impactScore}/10",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = rec.description, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { /* Dismiss */ }) {
                    Text("Dismiss", color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { /* Execute */ },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(rec.actionLabel, fontSize = 12.sp)
                }
            }
        }
    }
}
