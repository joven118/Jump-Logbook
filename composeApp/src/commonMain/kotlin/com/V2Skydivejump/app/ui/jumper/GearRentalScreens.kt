package com.V2Skydivejump.app.ui.jumper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.database.entities.DzInventoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentRequirementsScreen(
    availableGear: List<DzInventoryEntity>,
    onBack: () -> Unit,
    onConfirm: (List<DzInventoryEntity>, Double) -> Unit
) {
    val categories = listOf(
        "Container System", "Main Canopy", "Reserve Canopy", "AAD", 
        "Helmet", "Visual Altimeter", "Audible Altimeter", 
        "Jumpsuit", "Radio", "Lead Weights", "Camera Equipment"
    )

    // State for selected gear per category
    val selectedGear = remember { mutableStateMapOf<String, DzInventoryEntity?>() }
    val selectionType = remember { mutableStateMapOf<String, String>() } // "My Equipment", "Rent", "Not Required"

    val rentalTotal = selectedGear.values.filterNotNull().sumOf { it.rentalFee }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Equipment Requirements") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Rental Total", style = MaterialTheme.typography.labelSmall)
                        Text("$${rentalTotal.toInt()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = { onConfirm(selectedGear.values.filterNotNull(), rentalTotal) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Review Reservation")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Specify your equipment needs for this jump.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            categories.forEach { category ->
                EquipmentCategoryCard(
                    category = category,
                    availableOptions = availableGear.filter { it.category.contains(category, ignoreCase = true) || it.subCategory?.contains(category, ignoreCase = true) == true },
                    selectedType = selectionType[category] ?: "Not Required",
                    selectedItem = selectedGear[category],
                    onTypeSelected = { type -> 
                        selectionType[category] = type
                        if (type != "Rent") selectedGear[category] = null
                    },
                    onGearSelected = { gear -> selectedGear[category] = gear }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EquipmentCategoryCard(
    category: String,
    availableOptions: List<DzInventoryEntity>,
    selectedType: String,
    selectedItem: DzInventoryEntity?,
    onTypeSelected: (String) -> Unit,
    onGearSelected: (DzInventoryEntity) -> Unit
) {
    var showGearPicker by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(category, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("My Equipment", "Rent", "Not Required").forEach { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTypeSelected(type) },
                        label = { Text(type, fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (selectedType == "Rent") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedCard(
                    onClick = { showGearPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (selectedItem != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                    )
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (selectedItem != null) {
                                Text("${selectedItem.makeModel} (${selectedItem.serialNumber})", fontWeight = FontWeight.Bold)
                                Text("Fee: $${selectedItem.rentalFee.toInt()}", style = MaterialTheme.typography.labelSmall)
                            } else {
                                Text("Select Rental Item", color = MaterialTheme.colorScheme.primary)
                                Text("${availableOptions.size} items available", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                }
            }
        }
    }

    if (showGearPicker) {
        GearPickerBottomSheet(
            category = category,
            options = availableOptions,
            onDismiss = { showGearPicker = false },
            onSelected = { 
                onGearSelected(it)
                showGearPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GearPickerBottomSheet(
    category: String,
    options: List<DzInventoryEntity>,
    onDismiss: () -> Unit,
    onSelected: (DzInventoryEntity) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.6f)) {
            Text("Select $category", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (options.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No rental items available for this category.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(options) { gear ->
                        val isAirworthy = gear.rentalStatus == "Available"
                        ListItem(
                            modifier = Modifier.clickable(enabled = isAirworthy) { onSelected(gear) },
                            headlineContent = { Text(gear.makeModel) },
                            supportingContent = { 
                                Column {
                                    Text("SN: ${gear.serialNumber} | Size: ${gear.sizeSqft}")
                                    if (!isAirworthy) Text("STATUS: ${gear.rentalStatus}", color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                            },
                            trailingContent = { Text("$${gear.rentalFee.toInt()}", fontWeight = FontWeight.Bold) },
                            leadingContent = {
                                Icon(
                                    imageVector = if (isAirworthy) Icons.Default.CheckCircle else Icons.Default.Block,
                                    tint = if (isAirworthy) Color(0xFF4CAF50) else Color.Red,
                                    contentDescription = null
                                )
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
