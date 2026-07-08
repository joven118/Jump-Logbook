package com.V2Skydivejump.app.ui.jumper

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.TimeUtils
import com.V2Skydivejump.app.database.entities.*
import com.V2Skydivejump.app.rememberPhotoPickerLauncher
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesAwardsScreen(
    uiState: JumperUiState,
    onBack: () -> Unit,
    onClaimBadge: (String, String) -> Unit = { _, _ -> }
) {
    val categories = listOf(
        "JUMP_MILESTONES",
        "FORMATION",
        "FREEFLY",
        "WINGSUIT",
        "CANOPY_PILOTING",
        "COMPETITION",
        "SAFETY",
        "TRAVEL",
        "MILITARY",
        "SPECIAL_ACHIEVEMENT",
        "SPECIAL"
    )

    var selectedBadgeToClaim by remember { mutableStateOf<BadgeEntity?>(null) }

    val featuredBadge = remember(uiState.userBadges, uiState.allBadges) {
        val earnedIds = uiState.userBadges
            .filter { it.verificationStatus == BadgeVerificationStatus.VERIFIED }
            .map { it.badgeId }.toSet()
        uiState.allBadges
            .filter { it.id in earnedIds }
            .maxByOrNull { it.prestigeScore }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aviation Qualifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Featured Badge Section
            if (featuredBadge != null) {
                val isNew = uiState.userBadges.any { it.badgeId == featuredBadge.id && it.isNew }
                FeaturedBadgeCard(featuredBadge, isNew = isNew)
            }

            // Category Sections
            categories.forEach { cat ->
                BadgeCategoryCard(cat, uiState, onClaimClick = { selectedBadgeToClaim = it })
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (selectedBadgeToClaim != null) {
        ClaimBadgeDialog(
            badge = selectedBadgeToClaim!!,
            onConfirm = { docUrl ->
                onClaimBadge(selectedBadgeToClaim!!.id, docUrl)
                selectedBadgeToClaim = null
            },
            onDismiss = { selectedBadgeToClaim = null }
        )
    }
}

@Composable
fun ClaimBadgeDialog(
    badge: BadgeEntity,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var docUri by remember { mutableStateOf<String?>(null) }
    val photoPicker = rememberPhotoPickerLauncher { uri ->
        docUri = uri
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Claim ${badge.badgeName}") },
        text = {
            Column {
                Text("To verify this qualification, please upload supporting documentation (e.g., Certificates, Training Records, or Logbook entries).", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                if (docUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    ) {
                        AsyncImage(
                            model = docUri,
                            contentDescription = "Document Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { docUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { photoPicker() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Document Image")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("An Admin will review your proof. Verification usually takes 24-48 hours.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = { docUri?.let { onConfirm(it) } }, enabled = docUri != null) {
                Text("Submit for Verification")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun FeaturedBadgeCard(badge: BadgeEntity, isNew: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("PRIMARY QUALIFICATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            AviationBadge(badge, scale = 1.5f, isNew = isNew, isEarned = true)
            Spacer(modifier = Modifier.height(16.dp))
            Text(badge.badgeName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Text(badge.description, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = Color.Gray)
        }
    }
}

@Composable
fun BadgeCategoryCard(
    category: String, 
    uiState: JumperUiState,
    onClaimClick: (BadgeEntity) -> Unit = {}
) {
    val categoryName = category.replace("_", " ").lowercase().capitalizeWordsExtension()
    val earnedInCat = remember(uiState.userBadges, uiState.allBadges) {
        val userBadges = uiState.userBadges
        uiState.allBadges
            .filter { it.category == category }
            .filter { badge -> userBadges.any { it.badgeId == badge.id && it.verificationStatus == BadgeVerificationStatus.VERIFIED } }
            .sortedByDescending { it.level }
    }
    
    val highestBadge = earnedInCat.firstOrNull()
    var expanded by remember { mutableStateOf(false) }

    // Whitelist categories that should always show even if unearned
    val isCoreCategory = category in listOf(
        "JUMP_MILESTONES", "FORMATION", "TRAVEL", "MILITARY", 
        "SAFETY", "CANOPY_PILOTING", "FREEFLY", "SPECIAL_ACHIEVEMENT", "WINGSUIT"
    )

    if (highestBadge == null && !isCoreCategory) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween, 
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(categoryName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (highestBadge == null || category == "MILITARY" || category == "SPECIAL_ACHIEVEMENT") {
                        TextButton(onClick = { 
                            val nextToClaim = uiState.allBadges
                                .filter { it.category == category }
                                .firstOrNull { badge -> uiState.userBadges.none { it.badgeId == badge.id } }
                            if (nextToClaim != null) onClaimClick(nextToClaim)
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Claim Qualification", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }
            }
            
            if (!expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                if (highestBadge != null) {
                    val isHighestNew = uiState.userBadges.any { it.badgeId == highestBadge.id && it.isNew }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AviationBadge(highestBadge, isNew = isHighestNew, isEarned = true)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(highestBadge.badgeName, fontWeight = FontWeight.ExtraBold)
                            
                            // Progress Logic (Summary View)
                            val nextBadge = uiState.allBadges
                                .filter { it.category == category && it.level > highestBadge.level }
                                .minByOrNull { it.level }

                            if (nextBadge != null) {
                                when (category) {
                                    "JUMP_MILESTONES" -> {
                                        val totalJumps = uiState.jumps.size
                                        val progress = (totalJumps.toFloat() / nextBadge.criteriaValue).coerceAtMost(1f)
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(6.dp).clip(CircleShape)
                                        )
                                        Text("Next: ${nextBadge.badgeName} (${totalJumps}/${nextBadge.criteriaValue})", style = MaterialTheme.typography.labelSmall)
                                    }
                                    "FORMATION" -> {
                                        val fsJumps = uiState.jumps.count { it.disciplines?.contains("Formation", ignoreCase = true) == true || it.disciplines?.contains("Belly", ignoreCase = true) == true }
                                        if (nextBadge.criteriaType == BadgeCriteriaType.AUTO_JUMPS) {
                                            val progress = (fsJumps.toFloat() / nextBadge.criteriaValue).coerceAtMost(1f)
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(6.dp).clip(CircleShape)
                                            )
                                            Text("Next: ${nextBadge.badgeName} (${fsJumps}/${nextBadge.criteriaValue})", style = MaterialTheme.typography.labelSmall)
                                        } else {
                                            Text("Next: ${nextBadge.badgeName} (Req: ${nextBadge.description})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                    "TRAVEL" -> {
                                        val current = when (nextBadge.criteriaType) {
                                            BadgeCriteriaType.AUTO_DZS -> uiState.jumps.mapNotNull { it.dzName }.filter { it.isNotBlank() }.distinct().size
                                            BadgeCriteriaType.AUTO_COUNTRIES -> uiState.jumps.mapNotNull { it.country }.filter { it.isNotBlank() }.distinct().size
                                            else -> 0
                                        }
                                        if (nextBadge.criteriaValue > 0) {
                                            val progress = (current.toFloat() / nextBadge.criteriaValue).coerceAtMost(1f)
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(6.dp).clip(CircleShape)
                                            )
                                            val unit = if (nextBadge.criteriaType == BadgeCriteriaType.AUTO_DZS) "DZs" else "Countries"
                                            Text("Next: ${nextBadge.badgeName} ($current/${nextBadge.criteriaValue} $unit)", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    "WINGSUIT" -> {
                                        val wsJumps = uiState.jumps.count { it.disciplines?.contains("Wingsuit", ignoreCase = true) == true }
                                        if (nextBadge.criteriaType == BadgeCriteriaType.AUTO_JUMPS) {
                                            val progress = (wsJumps.toFloat() / nextBadge.criteriaValue).coerceAtMost(1f)
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(6.dp).clip(CircleShape)
                                            )
                                            Text("Next: ${nextBadge.badgeName} ($wsJumps/${nextBadge.criteriaValue} Wingsuit Jumps)", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    "CANOPY_PILOTING" -> {
                                        val cpJumps = uiState.jumps.count { 
                                            it.disciplines?.contains("Canopy", ignoreCase = true) == true || it.disciplines?.contains("Accuracy", ignoreCase = true) == true || 
                                            it.landingStyles?.contains("Accuracy", ignoreCase = true) == true || it.landingStyles?.contains("Swoop", ignoreCase = true) == true
                                        }
                                        if (nextBadge.criteriaType == BadgeCriteriaType.AUTO_JUMPS) {
                                            val progress = (cpJumps.toFloat() / nextBadge.criteriaValue).coerceAtMost(1f)
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(6.dp).clip(CircleShape)
                                            )
                                            Text("Next: ${nextBadge.badgeName} ($cpJumps/${nextBadge.criteriaValue} Canopy Jumps)", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    "FREEFLY" -> {
                                        val ffJumps = uiState.jumps.count { 
                                            it.disciplines?.contains("Freefly", ignoreCase = true) == true || it.disciplines?.contains("Sit Fly", ignoreCase = true) == true ||
                                            it.disciplines?.contains("Head-Up", ignoreCase = true) == true || it.disciplines?.contains("Head-Down", ignoreCase = true) == true
                                        }
                                        if (nextBadge.criteriaType == BadgeCriteriaType.AUTO_JUMPS) {
                                            val progress = (ffJumps.toFloat() / nextBadge.criteriaValue).coerceAtMost(1f)
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(6.dp).clip(CircleShape)
                                            )
                                            Text("Next: ${nextBadge.badgeName} ($ffJumps/${nextBadge.criteriaValue} Freefly Jumps)", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    "SAFETY" -> {
                                        val totalJumps = uiState.jumps.size
                                        if (nextBadge.criteriaType == BadgeCriteriaType.AUTO_JUMPS) {
                                            val progress = (totalJumps.toFloat() / nextBadge.criteriaValue).coerceAtMost(1f)
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(6.dp).clip(CircleShape)
                                            )
                                            Text("Next: ${nextBadge.badgeName} ($totalJumps/${nextBadge.criteriaValue} Safe Jumps)", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Empty state (no earned badges) - show first badge progress
                    val firstBadge = uiState.allBadges.filter { it.category == category }.minByOrNull { it.level }
                    if (firstBadge != null) {
                        Column {
                            Text("Progress toward ${firstBadge.badgeName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            // Reuse progress logic here if needed, or simple text
                            Text("Requirement: ${firstBadge.description}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            } else {
                // Expanded View: Show ALL badges in category
                val allInCat = uiState.allBadges
                    .filter { it.category == category || (category == "MILITARY" && it.category == "SPECIAL_MILITARY") }
                    .sortedBy { it.level }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 3-column Grid
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    allInCat.chunked(3).forEach { rowBadges ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            rowBadges.forEach { badge ->
                                val userBadge = uiState.userBadges.find { it.badgeId == badge.id }
                                val isEarned = userBadge != null && userBadge.verificationStatus == BadgeVerificationStatus.VERIFIED
                                
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        AviationBadge(
                                            badge = badge,
                                            scale = 0.8f,
                                            isNew = userBadge?.isNew ?: false,
                                            isEarned = isEarned
                                        )
                                        if (isEarned) {
                                            Text(
                                                TimeUtils.formatEpochMillis(userBadge!!.dateEarned),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF2E7D32),
                                                fontSize = 8.sp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            repeat(3 - rowBadges.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AviationBadge(badge: BadgeEntity, scale: Float = 1.0f, isNew: Boolean = false, isEarned: Boolean = true) {
    var showDetails by remember { mutableStateOf(false) }

    val wingBrush = if (isEarned) {
        when {
            badge.level >= 13 -> Brush.linearGradient(listOf(Color(0xFFE5E4E2), Color(0xFFFFD700), Color(0xFFB9F2FF))) // Diamond/Gold/Platinum
            badge.level >= 11 -> Brush.linearGradient(listOf(Color(0xFFE5E4E2), Color(0xFFFFD700))) // Gold/Platinum
            badge.level >= 9 -> Brush.linearGradient(listOf(Color(0xFFE5E4E2), Color(0xFFE5E4E2))) // Pure Platinum
            badge.level >= 7 -> Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B))) // Gold
            badge.level >= 4 -> Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFE5E4E2))) // Gold-Platinum
            badge.level >= 2 -> Brush.linearGradient(listOf(Color(0xFFE5E4E2), Color(0xFF1976D2))) // Silver-Blue/Gold
            else -> Brush.linearGradient(listOf(Color.LightGray, Color.DarkGray)) // Silver
        }
    } else {
        Brush.linearGradient(listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))) // Grayed out
    }

    val glowColor = if (isNew && isEarned) Color(0xFFFFD700).copy(alpha = 0.5f) else Color.Transparent
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val medallionBorder = if (isEarned) {
        when {
            badge.level >= 14 -> Brush.verticalGradient(listOf(Color(0xFFB9F2FF), Color(0xFFFFD700)))
            badge.level >= 10 -> Brush.verticalGradient(listOf(Color(0xFFE5E4E2), Color(0xFFFFD700)))
            else -> Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B)))
        }
    } else {
        Brush.verticalGradient(listOf(Color(0xFFCCCCCC), Color(0xFF999999)))
    }

    val icon = when {
        badge.id.contains("sl_jm") -> Icons.Default.Info
        badge.id.contains("pathfinder") -> Icons.Default.LocationOn
        badge.id.contains("combat") -> Icons.Default.Star
        badge.id.contains("spec_ops") -> Icons.Default.Build
        badge.id.contains("dist") || badge.id.contains("honor") -> Icons.Default.ThumbUp
        badge.id.contains("mff") -> Icons.Default.Send
        badge.id.contains("halo") || badge.id.contains("haho") -> Icons.Default.KeyboardArrowUp
        
        badge.id.startsWith("cp_") -> {
            when {
                badge.id.contains("accuracy") || badge.id.contains("precision") -> Icons.Default.LocationOn
                badge.id.contains("swoop") -> Icons.Default.KeyboardArrowDown
                badge.id.contains("champ") || badge.id.contains("med") -> Icons.Default.Star
                badge.id.contains("coach") -> Icons.Default.Info
                badge.id.contains("world") -> Icons.Default.Share
                else -> Icons.Default.KeyboardArrowUp
            }
        }
        badge.id.startsWith("ff_") -> {
            when {
                badge.id.contains("initiate") -> Icons.Default.Info
                badge.id.contains("sit") -> Icons.Default.Person
                badge.id.contains("headup") -> Icons.Default.KeyboardArrowUp
                badge.id.contains("headdown") -> Icons.Default.KeyboardArrowDown
                badge.id.contains("advanced") -> Icons.Default.Refresh
                badge.id.contains("vfs") -> Icons.Default.AccountBox
                badge.id.contains("organizer") -> Icons.Default.Star
                badge.id.contains("champ") || badge.id.contains("med") -> Icons.Default.Star
                else -> Icons.Default.KeyboardArrowUp
            }
        }
        badge.id.startsWith("ws_") -> {
            when {
                badge.id.contains("ff") -> Icons.Default.Send
                badge.id.contains("pilot") -> Icons.Default.ArrowForward
                badge.id.contains("advanced") -> Icons.Default.KeyboardArrowRight
                badge.id.contains("formation") -> Icons.Default.AccountBox
                badge.id.contains("performance") -> Icons.Default.Add
                badge.id.contains("organizer") -> Icons.Default.Star
                badge.id.contains("coach") -> Icons.Default.Info
                badge.id.contains("world") -> Icons.Default.Share
                else -> Icons.Default.Send
            }
        }
        badge.id.startsWith("sf_") -> {
            when {
                badge.id.contains("mentor") -> Icons.Default.Info
                badge.id.contains("risk") -> Icons.Default.LocationOn
                badge.id.contains("leader") -> Icons.Default.Star
                badge.id.contains("2500") || badge.id.contains("5000") -> Icons.Default.CheckCircle
                badge.id.contains("lifetime") -> Icons.Default.Star
                else -> Icons.Default.CheckCircle
            }
        }
        badge.category == "JUMP_MILESTONES" -> Icons.Default.Star
        badge.category == "FORMATION" -> Icons.Default.AccountBox
        badge.category == "TRAVEL" -> Icons.Default.LocationOn
        badge.category == "SPECIAL_ACHIEVEMENT" -> {
            when {
                badge.id.contains("hof") || badge.id.contains("lifetime") -> Icons.Default.Favorite
                badge.id.contains("safety") -> Icons.Default.CheckCircle
                badge.id.contains("service") -> Icons.Default.Info
                badge.id.contains("innovation") -> Icons.Default.Settings
                else -> Icons.Default.Star
            }
        }
        else -> Icons.Default.AccountBox
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { showDetails = true }
    ) {
        Box(
            modifier = Modifier
                .size((80 * scale).dp)
                .then(
                    if (isNew && isEarned) Modifier.border(
                        BorderStroke((4 * scale).dp, glowColor.copy(alpha = glowAlpha)),
                        CircleShape
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .size((34 * scale).dp, (20 * scale).dp)
                    .graphicsLayer { rotationZ = -10f }
                    .background(wingBrush, GenericShape { size: androidx.compose.ui.geometry.Size, _ ->
                        moveTo(size.width, size.height)
                        quadraticTo(0f, size.height, 0f, 0f)
                        lineTo(size.width, 0f)
                        close()
                    })
                )
                Spacer(modifier = Modifier.width((12 * scale).dp))
                Box(modifier = Modifier
                    .size((34 * scale).dp, (20 * scale).dp)
                    .graphicsLayer { rotationZ = 10f }
                    .background(wingBrush, GenericShape { size: androidx.compose.ui.geometry.Size, _ ->
                        moveTo(0f, size.height)
                        quadraticTo(size.width, size.height, size.width, 0f)
                        lineTo(0f, 0f)
                        close()
                    })
                )
            }
            
            Surface(
                modifier = Modifier
                    .size((42 * scale).dp)
                    .offset(y = (-5 * scale).dp),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke((2 * scale).dp, medallionBorder)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        if (isEarned) Brush.radialGradient(listOf(Color(0xFFFDFDFD), Color(0xFFD1D1D1)))
                        else Brush.radialGradient(listOf(Color(0xFFF5F5F5), Color(0xFFE0E0E0)))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon, 
                        contentDescription = null, 
                        tint = if (!isEarned) Color.LightGray else if (badge.level >= 4) Color(0xFFB8860B) else Color.DarkGray,
                        modifier = Modifier.size((24 * scale).dp)
                    )
                }
            }
        }
        
        Surface(
            modifier = Modifier
                .width((70 * scale).dp)
                .offset(y = (-10 * scale).dp),
            color = if (isEarned) Color.DarkGray.copy(alpha = 0.8f) else Color.LightGray.copy(alpha = 0.5f),
            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
        ) {
            Text(
                text = badge.badgeName.uppercase(),
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
                fontSize = (6 * scale).sp,
                color = if (isEarned) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = (7 * scale).sp
            )
        }
    }

    if (showDetails) {
        AlertDialog(
            onDismissRequest = { showDetails = false },
            title = { Text(badge.badgeName) },
            text = {
                Column {
                    Text(badge.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Requirement:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    val requirementText = when (badge.criteriaType) {
                        BadgeCriteriaType.AUTO_JUMPS -> "${badge.criteriaValue} total jumps"
                        BadgeCriteriaType.AUTO_FREEFALL -> "${badge.criteriaValue} minutes of freefall"
                        BadgeCriteriaType.AUTO_DZS -> "${badge.criteriaValue} unique dropzones"
                        BadgeCriteriaType.AUTO_COUNTRIES -> "${badge.criteriaValue} different countries"
                        BadgeCriteriaType.AUTO_CONTINENTS -> "${badge.criteriaValue} continents"
                        BadgeCriteriaType.AUTO_AIRCRAFTS -> "${badge.criteriaValue} different aircraft types"
                        BadgeCriteriaType.MANUAL -> "Manually awarded/claimed"
                        BadgeCriteriaType.VERIFIED -> "Requires manual verification"
                        else -> badge.description
                    }
                    Text(requirementText, style = MaterialTheme.typography.bodySmall)

                    if (isEarned) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Status: Earned", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Status: Not Earned", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetails = false }) { Text("Close") }
            }
        )
    }
}

fun String.capitalizeWordsExtension(): String = 
    split(" ").joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
