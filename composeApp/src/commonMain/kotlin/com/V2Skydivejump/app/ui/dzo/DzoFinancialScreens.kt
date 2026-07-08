package com.V2Skydivejump.app.ui.dzo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DzoFinancialDashboard(
    uiState: DzoUiState,
    onUpdateFinance: (FinanceReport) -> Unit = {},
    onUpdateCashFlow: (CashFlowReport) -> Unit = {},
    onUpdateBalanceSheet: (BalanceSheetReport) -> Unit = {}
) {
    var selectedReport by remember { mutableIntStateOf(0) } // 0: Dashboard, 1: P&L, 2: Cash Flow, 3: Balance Sheet, 4: Operational, 5: Analytics

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedReport,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = selectedReport == 0, onClick = { selectedReport = 0 }, text = { Text("Overview") })
            Tab(selected = selectedReport == 1, onClick = { selectedReport = 1 }, text = { Text("P&L") })
            Tab(selected = selectedReport == 2, onClick = { selectedReport = 2 }, text = { Text("Cash Flow") })
            Tab(selected = selectedReport == 3, onClick = { selectedReport = 3 }, text = { Text("Balance Sheet") })
            Tab(selected = selectedReport == 4, onClick = { selectedReport = 4 }, text = { Text("Operational") })
            Tab(selected = selectedReport == 5, onClick = { selectedReport = 5 }, text = { Text("Analytics") })
        }

        when (selectedReport) {
            0 -> FinancialOverview(uiState)
            1 -> ProfitAndLossScreen(uiState.financeReport, onUpdateFinance)
            2 -> CashFlowScreen(uiState.cashFlowReport, onUpdateCashFlow)
            3 -> BalanceSheetScreen(uiState.balanceSheet, onUpdateBalanceSheet)
            4 -> OperationalReportsScreen(uiState)
            5 -> CfoAnalyticsScreen(uiState)
        }
    }
}

@Composable
fun FinancialOverview(uiState: DzoUiState) {
    val finance = uiState.financeReport
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Today's Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard("Today's Revenue", "$4,850", MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
            SummaryCard("Today's Profit", "$2,930", Color(0xFFE8F5E9), Modifier.weight(1f))
        }

        Text("Monthly Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        KpiCard("Month Revenue", "$${finance.totalRevenue.toInt()}", Color(0xFFE3F2FD), Modifier.fillMaxWidth())
        KpiCard("Month Expenses", "$${finance.totalExpenses.toInt()}", Color(0xFFFBE9E7), Modifier.fillMaxWidth())
        KpiCard("Net Profit", "$${finance.netProfit.toInt()}", Color(0xFFE8F5E9), Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))
        Text("Key Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricRow("Most Profitable Activity", "Tandem Skydiving")
                MetricRow("Aircraft Utilization", "87%")
                MetricRow("Fuel Cost Per Load", "$105")
                MetricRow("Outstanding Receivables", "$3,250")
            }
        }
    }
}

@Composable
fun ProfitAndLossScreen(report: FinanceReport, onUpdate: (FinanceReport) -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { SectionHeader("REVENUE") }
            items(report.revenueItems) { item ->
                FinanceRow(item.category, item.amount)
            }
            item { 
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FinanceRow("Total Revenue", report.totalRevenue, isBold = true)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item { SectionHeader("EXPENSES") }
            items(report.expenseItems) { item ->
                FinanceRow(item.category, item.amount)
            }
            item { 
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FinanceRow("Total Expenses", report.totalExpenses, isBold = true)
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    FinanceRow("Net Profit", report.netProfit, isBold = true, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = { showEditDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit P&L")
        }
    }

    if (showEditDialog) {
        EditProfitAndLossDialog(
            report = report,
            onDismiss = { showEditDialog = false },
            onConfirm = { updated ->
                onUpdate(updated)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun CashFlowScreen(report: CashFlowReport, onUpdate: (CashFlowReport) -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionHeader("CASH FLOW")
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FinanceRow("Beginning Cash", report.beginningCash)
                    FinanceRow("Cash Received (+)", report.cashIn, color = Color(0xFF2E7D32))
                    FinanceRow("Cash Spent (-)", report.cashOut, color = Color(0xFFD32F2F))
                    HorizontalDivider()
                    FinanceRow("Ending Cash", report.endingCash, isBold = true)
                }
            }
        }

        FloatingActionButton(
            onClick = { showEditDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Cash Flow")
        }
    }

    if (showEditDialog) {
        EditCashFlowDialog(
            report = report,
            onDismiss = { showEditDialog = false },
            onConfirm = { updated ->
                onUpdate(updated)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun BalanceSheetScreen(report: BalanceSheetReport, onUpdate: (BalanceSheetReport) -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { SectionHeader("ASSETS") }
            item { Text("Current Assets", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
            items(report.currentAssets) { item -> FinanceRow(item.category, item.amount) }
            
            item { Spacer(modifier = Modifier.height(8.dp)); Text("Fixed Assets", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
            items(report.fixedAssets) { item -> FinanceRow(item.category, item.amount) }
            
            item { 
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FinanceRow("Total Assets", report.totalAssets, isBold = true)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item { SectionHeader("LIABILITIES & EQUITY") }
            items(report.liabilities) { item -> FinanceRow(item.category, item.amount) }
            items(report.equity) { item -> FinanceRow(item.category, item.amount) }
            
            item { 
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FinanceRow("Total Liabilities & Equity", report.totalLiabilities + report.totalEquity, isBold = true)
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = { showEditDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Balance Sheet")
        }
    }

    if (showEditDialog) {
        EditBalanceSheetDialog(
            report = report,
            onDismiss = { showEditDialog = false },
            onConfirm = { updated ->
                onUpdate(updated)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun OperationalReportsScreen(uiState: DzoUiState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader("OPERATIONAL REPORTS")
        
        Text("Aircraft Profitability", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        uiState.aircraftProfitability.forEach { ac ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(ac.aircraftName, fontWeight = FontWeight.Bold)
                    FinanceRow("Revenue Generated", ac.revenue)
                    FinanceRow("Fuel Cost", ac.fuel)
                    FinanceRow("Maintenance", ac.maintenance)
                    HorizontalDivider()
                    FinanceRow("Profit", ac.profit, isBold = true, color = Color(0xFF2E7D32))
                }
            }
        }

        Text("Load Profitability (Last Load)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        uiState.loadProfitability.firstOrNull()?.let { load ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Load #${load.loadId} (${load.jumperCount} Jumpers)", fontWeight = FontWeight.Bold)
                    FinanceRow("Revenue", load.revenue)
                    FinanceRow("Fuel Cost", load.fuel)
                    FinanceRow("Pilot Cost", load.pilotCost)
                    HorizontalDivider()
                    FinanceRow("Profit", load.profit, isBold = true, color = Color(0xFF2E7D32))
                }
            }
        }
        
        Text("Revenue by Activity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.financeReport.revenueItems.forEach { item ->
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.category, style = MaterialTheme.typography.bodySmall)
                            Text("${item.percentage.toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { item.percentage.toFloat() / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.LightGray, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }

        Text("Payroll Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricRow("Pilots", "$8,500")
                MetricRow("Instructors", "$12,000")
                MetricRow("Manifest & Staff", "$4,500")
                MetricRow("Packers", "$2,700")
            }
        }

        Text("Tandem Operations Report", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricRow("Total Tandems", "145")
                MetricRow("Revenue", "$36,250")
                MetricRow("Instructor Fees", "$8,700")
                MetricRow("Estimated Profit", "$21,800")
            }
        }

        Text("Fuel Consumption", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricRow("Fuel Cost Per Load", "$105")
                MetricRow("Fuel Cost Per Jumper", "$6.12")
            }
        }
    }
}

@Composable
fun CfoAnalyticsScreen(uiState: DzoUiState) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SectionHeader("BUSINESS INTELLIGENCE")

        // 1. Dynamic Pricing Suggestions
        Text("AI Dynamic Pricing Suggestions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        uiState.pricingSuggestions.forEach { suggestion ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(suggestion.activityType, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(16.dp)) {
                            Text("${suggestion.confidence}% AI Confidence", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(suggestion.reason, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$${suggestion.currentPrice.toInt()}", style = MaterialTheme.typography.titleMedium, color = Color.Gray, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                        Icon(Icons.Default.ArrowForward, null, modifier = Modifier.padding(horizontal = 8.dp).size(16.dp))
                        Text("$${suggestion.suggestedPrice.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                        Spacer(Modifier.width(8.dp))
                        val diff = suggestion.suggestedPrice - suggestion.currentPrice
                        Text(if (diff > 0) "+$${diff.toInt()}" else "-$${kotlin.math.abs(diff).toInt()}", style = MaterialTheme.typography.labelSmall, color = if (diff > 0) Color(0xFF2E7D32) else Color.Red)
                    }
                }
            }
        }

        // 2. Customer Lifetime Value (CLV)
        Text("High-Value Jumper Insights (CLV)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                uiState.customerClvMetrics.forEachIndexed { index, clv ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                            Text(clv.userName.take(1), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(clv.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("${clv.jumpCount} jumps • Score: ${clv.clvScore}/100", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        Text("$${formatAmount(clv.totalSpend)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    if (index < uiState.customerClvMetrics.size - 1) HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }

        // 3. Inventory ROI Tracking
        Text("Rental Inventory ROI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        uiState.gearRoiMetrics.forEach { roi ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(roi.gearName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Revenue Generated", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("$${formatAmount(roi.totalRevenue)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Break-even Status", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("${(roi.breakEvenProgress * 100).toInt()}%", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodySmall, color = if (roi.breakEvenProgress >= 1f) Color(0xFF2E7D32) else Color.Unspecified)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { roi.breakEvenProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = if (roi.breakEvenProgress >= 1f) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun SummaryCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FinanceRow(label: String, amount: Double, isBold: Boolean = false, color: Color = Color.Unspecified, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(
            "$${formatAmount(amount)}", 
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Black,
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

fun formatAmount(amount: Double): String {
    return amount.toInt().toString().reversed().chunked(3).joinToString(",").reversed()
}

@Composable
fun EditProfitAndLossDialog(report: FinanceReport, onDismiss: () -> Unit, onConfirm: (FinanceReport) -> Unit) {
    val revenue = remember { report.revenueItems.toMutableStateList() }
    val expenses = remember { report.expenseItems.toMutableStateList() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profit & Loss") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Revenue", fontWeight = FontWeight.Bold)
                revenue.forEachIndexed { index, item ->
                    OutlinedTextField(
                        value = item.amount.toString(),
                        onValueChange = { val value = it.toDoubleOrNull() ?: 0.0; revenue[index] = item.copy(amount = value) },
                        label = { Text(item.category) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                HorizontalDivider()
                Text("Expenses", fontWeight = FontWeight.Bold)
                expenses.forEachIndexed { index, item ->
                    OutlinedTextField(
                        value = item.amount.toString(),
                        onValueChange = { val value = it.toDoubleOrNull() ?: 0.0; expenses[index] = item.copy(amount = value) },
                        label = { Text(item.category) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val totalRev = revenue.sumOf { it.amount }
                val totalExp = expenses.sumOf { it.amount }
                onConfirm(FinanceReport(revenue.toList(), expenses.toList(), totalRev, totalExp, totalRev - totalExp))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditCashFlowDialog(report: CashFlowReport, onDismiss: () -> Unit, onConfirm: (CashFlowReport) -> Unit) {
    var beg by remember { mutableStateOf(report.beginningCash.toString()) }
    var inc by remember { mutableStateOf(report.cashIn.toString()) }
    var out by remember { mutableStateOf(report.cashOut.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Cash Flow") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = beg, onValueChange = { beg = it }, label = { Text("Beginning Cash") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = inc, onValueChange = { inc = it }, label = { Text("Cash In") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = out, onValueChange = { out = it }, label = { Text("Cash Out") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val b = beg.toDoubleOrNull() ?: 0.0
                val i = inc.toDoubleOrNull() ?: 0.0
                val o = out.toDoubleOrNull() ?: 0.0
                onConfirm(CashFlowReport(b, i, o, b + i - o))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditBalanceSheetDialog(report: BalanceSheetReport, onDismiss: () -> Unit, onConfirm: (BalanceSheetReport) -> Unit) {
    val current = remember { report.currentAssets.toMutableStateList() }
    val fixed = remember { report.fixedAssets.toMutableStateList() }
    val liab = remember { report.liabilities.toMutableStateList() }
    val eq = remember { report.equity.toMutableStateList() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Balance Sheet") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current Assets", fontWeight = FontWeight.Bold)
                current.forEachIndexed { index, item ->
                    OutlinedTextField(value = item.amount.toString(), onValueChange = { current[index] = item.copy(amount = it.toDoubleOrNull() ?: 0.0) }, label = { Text(item.category) }, modifier = Modifier.fillMaxWidth())
                }
                Text("Fixed Assets", fontWeight = FontWeight.Bold)
                fixed.forEachIndexed { index, item ->
                    OutlinedTextField(value = item.amount.toString(), onValueChange = { fixed[index] = item.copy(amount = it.toDoubleOrNull() ?: 0.0) }, label = { Text(item.category) }, modifier = Modifier.fillMaxWidth())
                }
                Text("Liabilities", fontWeight = FontWeight.Bold)
                liab.forEachIndexed { index, item ->
                    OutlinedTextField(value = item.amount.toString(), onValueChange = { liab[index] = item.copy(amount = it.toDoubleOrNull() ?: 0.0) }, label = { Text(item.category) }, modifier = Modifier.fillMaxWidth())
                }
                Text("Equity", fontWeight = FontWeight.Bold)
                eq.forEachIndexed { index, item ->
                    OutlinedTextField(value = item.amount.toString(), onValueChange = { eq[index] = item.copy(amount = it.toDoubleOrNull() ?: 0.0) }, label = { Text(item.category) }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val ta = current.sumOf { it.amount } + fixed.sumOf { it.amount }
                val tl = liab.sumOf { it.amount }
                val te = eq.sumOf { it.amount }
                onConfirm(BalanceSheetReport(current.toList(), fixed.toList(), liab.toList(), eq.toList(), ta, tl, te))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

