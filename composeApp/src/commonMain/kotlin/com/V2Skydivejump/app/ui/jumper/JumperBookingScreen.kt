package com.V2Skydivejump.app.ui.jumper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.utils.CountryUtils

@Composable
fun JumperBookingScreen(
    bookingGroups: List<DzBookingGroup>,
    onBookClick: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Book Your Next Jump",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold
        )

        if (bookingGroups.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No flight schedules available at the moment.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(bookingGroups) { group ->
                    DzBookingGroupItem(group, onBookClick)
                }
            }
        }
    }
}

@Composable
fun DzBookingGroupItem(
    group: DzBookingGroup,
    onBookClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row 1: Country Flag, DZ Name, DZ Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = group.flagEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = group.dzName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = group.detailedLocation,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))

            // Row 2: Flight schedules
            Text(
                text = "Available Loads",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            group.schedules.forEach { schedule ->
                ScheduleBookingRow(schedule, onBookClick)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ScheduleBookingRow(
    schedule: FlightScheduleBookingInfo,
    onBookClick: (Long) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AirplanemodeActive, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Load #${schedule.loadNumber} • ${schedule.aircraftType}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Alt: ${schedule.exitAltitude} | Slots: ${schedule.availableSlots}/${schedule.maxJumpers}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            if (schedule.isBooked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp), tint = Color(0xFF388E3C))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Booked", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = { onBookClick(schedule.scheduleId) },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp),
                    enabled = schedule.availableSlots > 0
                ) {
                    Text(if (schedule.availableSlots > 0) "Book" else "Full", fontSize = 12.sp)
                }
            }
        }
    }
}
