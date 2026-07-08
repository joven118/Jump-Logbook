package com.V2Skydivejump.app.ui.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.V2Skydivejump.app.utils.ExternalShareManager
import coil3.compose.AsyncImage

@Composable
fun MarketplaceScreen(
    listings: List<GearListingEntity>,
    onCreateListingClick: () -> Unit,
    onListingClick: (GearListingEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedListingForDetails by remember { mutableStateOf<GearListingEntity?>(null) }
    val categories = listOf("All", "Canopy", "Container", "Altimeter", "AAD", "Helmet", "Jumpsuit", "Parts/Accessories")

    val filteredListings = if (selectedCategory == "All") {
        listings
    } else {
        listings.filter { it.category == selectedCategory }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateListingClick) {
                Icon(Icons.Default.Add, contentDescription = "Create Listing")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = { Text(category) }
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredListings) { listing ->
                    GearListingCard(listing, onClick = { selectedListingForDetails = listing })
                }
            }
        }
    }

    if (selectedListingForDetails != null) {
        ListingDetailDialog(
            listing = selectedListingForDetails!!,
            onDismiss = { selectedListingForDetails = null },
            onShare = {
                ExternalShareManager.shareText(
                    "Check out this ${selectedListingForDetails!!.title} on Jump Logbook!\n" +
                    "Price: $${selectedListingForDetails!!.price}\n" +
                    "Location: ${selectedListingForDetails!!.location ?: "Not specified"}"
                )
            }
        )
    }
}

@Composable
fun GearListingCard(listing: GearListingEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val firstPhoto = listing.photoUrls?.split(",")?.firstOrNull { it.isNotBlank() }
            
            if (firstPhoto != null) {
                AsyncImage(
                    model = firstPhoto,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(MaterialTheme.shapes.small)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(bottom = 8.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", fontSize = 10.sp, color = Color.Gray)
                }
            }
            
            Text(
                text = listing.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$${listing.price}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.extraSmall,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = listing.condition.uppercase(),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp
                )
            }
        }
    }
}
