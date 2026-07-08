package com.V2Skydivejump.app.ui.jumper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.database.entities.DzInventoryEntity
import com.V2Skydivejump.app.database.entities.PromotionEntity
import com.V2Skydivejump.app.database.entities.UserEntity
import com.V2Skydivejump.app.utils.PromoEngine
import com.V2Skydivejump.app.utils.PromoValidationResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingReviewScreen(
    user: UserEntity?,
    basePrice: Double,
    rentalItems: List<DzInventoryEntity>,
    rentalTotal: Double,
    availablePromos: List<PromotionEntity>,
    convenienceFeeType: String = "FIXED",
    convenienceFeeRate: Double = 0.0,
    onBack: () -> Unit,
    onConfirm: (PromotionEntity?, Double, Int) -> Unit
) {
    var promoCode by remember { mutableStateOf("") }
    var appliedPromo by remember { mutableStateOf<PromotionEntity?>(null) }
    var promoError by remember { mutableStateOf<String?>(null) }

    val membershipDiscountPercent = user?.let { PromoEngine.getMembershipDiscount(it.membershipLevel) } ?: 0.0
    val subTotal = basePrice + rentalTotal
    val membershipDiscount = subTotal * membershipDiscountPercent
    
    val convenienceFee = if (convenienceFeeType == "PERCENTAGE") {
        subTotal * (convenienceFeeRate / 100.0)
    } else {
        convenienceFeeRate
    }

    val promoDiscount = appliedPromo?.let { PromoEngine.calculateDiscount(subTotal - membershipDiscount, it) } ?: 0.0
    val finalTotal = subTotal - membershipDiscount - promoDiscount + convenienceFee
    
    val pointsToEarn = PromoEngine.calculateLoyaltyPoints(finalTotal, "BOOKING")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Reservation") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total to Pay", style = MaterialTheme.typography.labelSmall)
                            Text("$${finalTotal.toInt()}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = { onConfirm(appliedPromo, finalTotal, pointsToEarn) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Confirm Booking")
                        }
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
            // Summary Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Reservation Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    
                    SummaryRow("Jump Slot", "$${basePrice.toInt()}")
                    if (rentalItems.isNotEmpty()) {
                        SummaryRow("Equipment Rental", "$${rentalTotal.toInt()}")
                        rentalItems.forEach { item ->
                            Text("• ${item.category}: ${item.makeModel}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SummaryRow("Subtotal", "$${subTotal.toInt()}", isBold = true)
                    
                    if (membershipDiscount > 0) {
                        SummaryRow("${user?.membershipLevel} Member Discount", "-$${membershipDiscount.toInt()}", color = Color(0xFF2E7D32))
                    }

                    if (convenienceFee > 0) {
                        SummaryRow("V2 Convenience Fee", "$${"%.2f".format(convenienceFee)}")
                    }
                }
            }

            // Promo Code Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Promotions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = promoCode,
                            onValueChange = { promoCode = it; promoError = null },
                            label = { Text("Promo Code") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = promoError != null
                        )
                        Button(
                            onClick = {
                                val promo = availablePromos.find { it.code.equals(promoCode, ignoreCase = true) }
                                if (promo != null && user != null) {
                                    val result = PromoEngine.validatePromo(promo, user)
                                    if (result is PromoValidationResult.Valid) {
                                        appliedPromo = result.promo
                                        promoError = null
                                    } else if (result is PromoValidationResult.Invalid) {
                                        promoError = result.reason
                                        appliedPromo = null
                                    }
                                } else {
                                    promoError = "Invalid promo code."
                                    appliedPromo = null
                                }
                            },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Apply")
                        }
                    }
                    
                    if (promoError != null) {
                        Text(promoError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    
                    if (appliedPromo != null) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Promo Applied: ${appliedPromo!!.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                                    Text("Discount: -$${promoDiscount.toInt()}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }
                }
            }

            // Rewards Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Stars, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Rewards Points", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Text("You will earn $pointsToEarn points with this booking.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isBold: Boolean = false, color: Color = Color.Unspecified) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = color)
    }
}
