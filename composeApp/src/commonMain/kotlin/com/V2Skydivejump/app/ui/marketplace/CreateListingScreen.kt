package com.V2Skydivejump.app.ui.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.database.entities.GearListingEntity
import com.V2Skydivejump.app.TimeUtils
import com.V2Skydivejump.app.rememberPhotoPickerLauncher
import com.V2Skydivejump.app.rememberVideoPickerLauncher
import com.V2Skydivejump.app.ui.jumper.MediaThumbnail
import com.V2Skydivejump.app.ui.jumper.JumperUiState
import com.V2Skydivejump.app.ui.jumper.FlowRow
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    uiState: JumperUiState,
    onSave: (GearListingEntity) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contactDetails by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Canopy") }
    var expanded by remember { mutableStateOf(false) }
    
    val selectedPhotoUris = remember { mutableStateListOf<String>() }
    val selectedVideoUris = remember { mutableStateListOf<String>() }
    
    val photoPicker = rememberPhotoPickerLauncher { uri ->
        if (uri != null) selectedPhotoUris.add(uri)
    }
    val videoPicker = rememberVideoPickerLauncher { uri ->
        if (uri != null) selectedVideoUris.add(uri)
    }

    val categories = listOf("Canopy", "Container", "Altimeter", "AAD", "Helmet", "Jumpsuit", "Parts/Accessories")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create Gear Listing", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Item Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Box {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = { },
                label = { Text("Category") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price ($)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = condition,
                onValueChange = { condition = it },
                label = { Text("Condition") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location (City, State/Country)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contactDetails,
            onValueChange = { contactDetails = it },
            label = { Text("Contact Info (Phone, WhatsApp, or Email)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            minLines = 3
        )

        Text("Media", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = { photoPicker() }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Photo")
            }
            OutlinedButton(onClick = { videoPicker() }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Video")
            }
        }

        if (selectedPhotoUris.isNotEmpty() || selectedVideoUris.isNotEmpty()) {
            FlowRow(modifier = Modifier.fillMaxWidth()) {
                selectedPhotoUris.forEach { uri ->
                    MediaThumbnail(uri, isVideo = false) { selectedPhotoUris.remove(uri) }
                }
                selectedVideoUris.forEach { uri ->
                    MediaThumbnail(uri, isVideo = true) { selectedVideoUris.remove(uri) }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    val listing = GearListingEntity(
                        id = "listing_${TimeUtils.nowEpochMillis()}",
                        sellerId = uiState.user?.userId ?: "me",
                        title = title,
                        category = selectedCategory,
                        condition = condition,
                        price = price.toDoubleOrNull() ?: 0.0,
                        description = description,
                        datePosted = TimeUtils.nowEpochMillis(),
                        location = location,
                        contactDetails = contactDetails,
                        photoUrls = selectedPhotoUris.joinToString(","),
                        videoUrls = selectedVideoUris.joinToString(",")
                    )
                    onSave(listing)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Post Listing")
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun ListingDetailDialog(
    listing: GearListingEntity,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(listing.title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val allPhotos = listing.photoUrls?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                if (allPhotos.isNotEmpty()) {
                    AsyncImage(
                        model = allPhotos.first(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Text(text = "$${listing.price}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = "Category: ${listing.category}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Condition: ${listing.condition}", style = MaterialTheme.typography.bodySmall)
                
                if (!listing.location.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = listing.location!!, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Description", style = MaterialTheme.typography.titleSmall)
                Text(text = listing.description, style = MaterialTheme.typography.bodyMedium)

                if (!listing.contactDetails.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Text(text = "Seller Contact", style = MaterialTheme.typography.titleSmall)
                            Text(text = listing.contactDetails!!, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share to Social Media")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
