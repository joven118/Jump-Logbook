package com.V2Skydivejump.app.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.V2Skydivejump.app.UserRole
import com.V2Skydivejump.app.database.entities.SkydivingEvent
import com.V2Skydivejump.app.TimeUtils

@Composable
fun EventCalendarScreen(
    events: List<SkydivingEvent>,
    userRole: UserRole?,
    onPostEventClick: () -> Unit,
    onRsvpClick: (SkydivingEvent) -> Unit,
    onShareClick: (SkydivingEvent) -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            if (userRole == UserRole.DZ_OPERATOR) {
                ExtendedFloatingActionButton(
                    onClick = onPostEventClick,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Post Event") }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(events) { event ->
                EventCard(event, onRsvpClick, onShareClick, userRole == UserRole.DZ_OPERATOR)
            }
        }
    }
}

@Composable
fun EventCard(
    event: SkydivingEvent, 
    onRsvpClick: (SkydivingEvent) -> Unit,
    onShareClick: (SkydivingEvent) -> Unit,
    canShare: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = TimeUtils.formatEpochMillis(event.date), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    text = if (event.registrationFee > 0) "$${event.registrationFee}" else "FREE",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    if (canShare) {
                        IconButton(onClick = { onShareClick(event) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(onClick = { onRsvpClick(event) }) {
                        Text("RSVP / Buy Ticket")
                    }
                }
            }
        }
    }
}
