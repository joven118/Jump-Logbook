package com.V2Skydivejump.app.ui.dzo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.TimeUtils

@Composable
fun ManifestTvScreen(
    uiState: DzoUiState,
    onClose: () -> Unit
) {
    val currentTime = TimeUtils.formatEpochMillis(TimeUtils.nowEpochMillis())
    
    Scaffold(
        containerColor = Color(0xFF121212), // Dark "Cinema" theme for high contrast
        topBar = {
            Surface(color = Color.Black, contentColor = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.user?.dzName ?: "SKYDIVING CENTER", 
                            fontSize = 32.sp, 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.width(24.dp))
                        Surface(color = Color(0xFF388E3C), shape = RoundedCornerShape(4.dp)) {
                            Text("LIVE MANIFEST", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(currentTime, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Weather: ${uiState.weatherData.condition}, ${uiState.weatherData.windSpeedKts.toInt()}kts", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
        }
    ) { padding ->
        Row(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Main Load Grid (Left 70%)
            Column(modifier = Modifier.weight(0.7f).padding(16.dp)) {
                Text("ACTIVE LOADS", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                if (uiState.activeLoads.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("NO ACTIVE LOADS", color = Color.DarkGray, fontSize = 48.sp, fontWeight = FontWeight.Black)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.activeLoads.sortedBy { it.id }) { load ->
                            TvLoadCard(load)
                        }
                    }
                }
            }
            
            // Side Panel: Fleet & Info (Right 30%)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color.DarkGray)
            ) {}
            
            Column(modifier = Modifier.weight(0.3f).padding(16.dp)) {
                Text("FLEET STATUS", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                val fleet = uiState.dzInventory.filter { it.category == "Aircraft" }
                fleet.forEach { ac ->
                    TvAircraftRow(ac, uiState.activeLoads.find { it.aircraft?.id == ac.id })
                    Spacer(Modifier.height(12.dp))
                }
                
                Spacer(Modifier.weight(1f))
                
                // Footer Brand
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("POWERED BY", color = Color.Gray, fontSize = 10.sp)
                    Text("V2 SKYDIVE JUMP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, "Close TV View", tint = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
fun TvLoadCard(load: FlightLoad) {
    val statusColor = when(load.status) {
        LoadStatus.BOARDING -> Color(0xFF1976D2)
        LoadStatus.TAXIING -> Color(0xFFFFA000)
        LoadStatus.IN_FLIGHT -> Color(0xFFD32F2F)
        else -> Color.Gray
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, statusColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("LOAD #${load.id}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    Text(load.aircraft?.name ?: "Unknown AC", color = Color.Gray, fontSize = 16.sp)
                }
                Surface(color = statusColor, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = load.status.name, 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Jumper List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                load.jumpers.take(10).forEach { jumper ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(if(jumper.isCheckedIn) Color(0xFF388E3C) else Color.Gray, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.width(12.dp))
                        Text(jumper.name.uppercase(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.weight(1f))
                        Text(jumper.jumpType.name.take(3), color = Color.DarkGray, fontSize = 12.sp)
                    }
                }
                if (load.jumpers.size > 10) {
                    Text("+ ${load.jumpers.size - 10} MORE", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                if (load.jumpers.isEmpty()) {
                    Text("OPEN SLOTS AVAILABLE", color = Color(0xFF388E3C), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Timer / Call
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("EST. TAKEOFF: 15 MIN", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun TvAircraftRow(aircraft: com.V2Skydivejump.app.database.entities.DzInventoryEntity, currentLoad: FlightLoad?) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.Black)) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AirplanemodeActive, null, tint = if(currentLoad != null) Color(0xFF1976D2) else Color.Gray)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(aircraft.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = currentLoad?.let { "In Air - Load #${it.id}" } ?: "On Ground - Ready",
                    color = if(currentLoad != null) Color(0xFF1976D2) else Color(0xFF388E3C),
                    fontSize = 11.sp
                )
            }
        }
    }
}
