package com.V2Skydivejump.app.ui.directory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.V2Skydivejump.app.database.entities.DropzoneEntity
import com.V2Skydivejump.app.utils.CountryUtils

@Composable
fun DropzoneDirectoryScreen(
    dropzones: List<DropzoneEntity>,
    onDzClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredDropzones = dropzones.filter { 
        it.dzName.contains(searchQuery, ignoreCase = true) || 
        it.location.contains(searchQuery, ignoreCase = true) ||
        it.city.contains(searchQuery, ignoreCase = true) ||
        it.province.contains(searchQuery, ignoreCase = true) ||
        it.country.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Dropzone Directory",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            placeholder = { Text("Search by name, city, or country...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Text(
                    text = "Registered Dropzones (${filteredDropzones.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(filteredDropzones) { dz ->
                DropzoneCard(dz, onDzClick)
            }
        }
    }
}

@Composable
fun DropzoneCard(
    dz: DropzoneEntity,
    onDzClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dz.dzName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onDzClick(dz.id) }
                )
                Text(
                    text = CountryUtils.getFlagEmoji(dz.country),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            Text(
                text = listOfNotNull(
                    dz.city.takeIf { it.isNotBlank() },
                    dz.province.takeIf { it.isNotBlank() },
                    dz.country.takeIf { it.isNotBlank() }
                ).joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (dz.location.isNotBlank() && dz.location != dz.city) {
                Text(
                    text = dz.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = Color.LightGray)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Aircraft: ${dz.aircraftFleet}",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "Status: Registered",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF388E3C),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
