package com.V2Skydivejump.app.ui.dzo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.database.entities.PromotionEntity

@Composable
fun PromoManagementModule(
    uiState: DzoUiState,
    onUpsertPromo: (PromotionEntity) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedPromoForEdit by remember { mutableStateOf<PromotionEntity?>(null) }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Promotions & Campaigns") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, null) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            PromoSummaryCards(uiState.metrics)
            
            if (uiState.promotions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active promotions. Create one to boost jumps!", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(uiState.promotions) { promo ->
                        PromoCard(promo, onClick = { selectedPromoForEdit = promo })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        PromoEditDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { onUpsertPromo(it); showAddDialog = false }
        )
    }

    selectedPromoForEdit?.let { promo ->
        PromoEditDialog(
            promo = promo,
            onDismiss = { selectedPromoForEdit = null },
            onConfirm = { onUpsertPromo(it); selectedPromoForEdit = null }
        )
    }
}

@Composable
fun PromoSummaryCards(metrics: DzOperationalMetrics) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard("Active Promos", metrics.activePromos.toString(), MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
        SummaryCard("Redemptions Today", metrics.promoRedemptionsToday.toString(), Color(0xFFE8F5E9), Modifier.weight(1f))
        SummaryCard("Promo Revenue", "$${metrics.promoRevenue.toInt()}", Color(0xFFFFF3E0), Modifier.weight(1f))
    }
}

@Composable
fun PromoCard(promo: PromotionEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(promo.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Code: ${promo.code}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(promo.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Target: ${promo.eligibilityRules}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.TrendingUp, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Used: ${promo.currentRedemptions}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = when(promo.promoType) {
                        "PERCENTAGE" -> Color(0xFFE3F2FD)
                        "FIXED" -> Color(0xFFF1F8E9)
                        "FREE_SLOT" -> Color(0xFFFFF3E0)
                        else -> Color.LightGray
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    val valueText = when(promo.promoType) {
                        "PERCENTAGE" -> "${promo.value.toInt()}% OFF"
                        "FIXED" -> "$${promo.value.toInt()} OFF"
                        "FREE_SLOT" -> "FREE SLOT"
                        else -> "BUNDLE"
                    }
                    Text(valueText, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (promo.isActive) "ACTIVE" else "INACTIVE",
                    color = if (promo.isActive) Color(0xFF2E7D32) else Color.Red,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoEditDialog(
    promo: PromotionEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (PromotionEntity) -> Unit
) {
    var name by remember { mutableStateOf(promo?.name ?: "") }
    var code by remember { mutableStateOf(promo?.code ?: "") }
    var description by remember { mutableStateOf(promo?.description ?: "") }
    var promoType by remember { mutableStateOf(promo?.promoType ?: "PERCENTAGE") }
    var value by remember { mutableStateOf(promo?.value?.toString() ?: "") }
    var eligibility by remember { mutableStateOf(promo?.eligibilityRules ?: "ALL") }
    var isUnlimited by remember { mutableStateOf(promo?.isUnlimited ?: true) }
    var maxRedemptions by remember { mutableStateOf(promo?.maxRedemptions?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (promo == null) "New Promotion" else "Edit Promotion") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Promo Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Promo Code (e.g. SKYDIVE10)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                
                Text("Type & Value", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var expandedType by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = promoType, onValueChange = {}, readOnly = true, label = { Text("Type") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.clickable { expandedType = true })
                        DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                            listOf("PERCENTAGE", "FIXED", "FREE_SLOT", "BUNDLE").forEach { t ->
                                DropdownMenuItem(text = { Text(t) }, onClick = { promoType = t; expandedType = false })
                            }
                        }
                    }
                    OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Value") }, modifier = Modifier.weight(1f), placeholder = { Text(if (promoType == "PERCENTAGE") "%" else "$") })
                }

                Text("Eligibility", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                var expandedEli by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(value = eligibility, onValueChange = {}, readOnly = true, label = { Text("Target Audience") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth().clickable { expandedEli = true })
                    DropdownMenu(expanded = expandedEli, onDismissRequest = { expandedEli = false }) {
                        listOf("ALL", "FIRST_TIME", "LICENSED", "STUDENT", "MEMBER").forEach { e ->
                            DropdownMenuItem(text = { Text(e) }, onClick = { eligibility = e; expandedEli = false })
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isUnlimited, onCheckedChange = { isUnlimited = it })
                    Text("Unlimited Redemptions")
                }
                
                if (!isUnlimited) {
                    OutlinedTextField(value = maxRedemptions, onValueChange = { maxRedemptions = it }, label = { Text("Max Redemptions") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && code.isNotBlank()) {
                    onConfirm(
                        PromotionEntity(
                            id = promo?.id ?: 0,
                            dzId = promo?.dzId ?: "",
                            name = name,
                            code = code,
                            description = description,
                            promoType = promoType,
                            value = value.toDoubleOrNull() ?: 0.0,
                            startDate = promo?.startDate ?: com.V2Skydivejump.app.TimeUtils.nowEpochMillis(),
                            endDate = promo?.endDate ?: (com.V2Skydivejump.app.TimeUtils.nowEpochMillis() + 2592000000L),
                            isUnlimited = isUnlimited,
                            maxRedemptions = maxRedemptions.toIntOrNull() ?: 0,
                            eligibilityRules = eligibility,
                            isActive = promo?.isActive ?: true
                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
