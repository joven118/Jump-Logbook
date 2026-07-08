package com.V2Skydivejump.app.ui.support

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class FaqItem(
    val question: String,
    val answer: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(onBack: () -> Unit) {
    val faqs = listOf(
        FaqItem("How do I sync my altimeter?", "Go to your Profile and tap the 'NFC' or 'Altimeter' icon. Ensure Bluetooth is enabled on your device.", "Hardware"),
        FaqItem("How do I book a jump?", "Tap the 'Book' tab in the bottom navigation, select a Dropzone and a load, then confirm your gear requirements.", "Booking"),
        FaqItem("What is the Digital Syllabus?", "It's a progress tracker for students (AFF/A-License). Instructors can digitally sign off your skills in the Staff Terminal.", "Training"),
        FaqItem("How do I add staff?", "DZO/Managers can go to Operations > Staff > Add Staff. Enter their email and assign a specific role.", "DZO Ops"),
        FaqItem("Is my data secure?", "Yes, all jump logs and personal info are synchronized using encrypted Supabase storage and local Room DB.", "Security"),
        FaqItem("How does the AI Interlock work?", "The system monitors live weather (METAR). If winds exceed your DZ's limit, it automatically places a hold on the manifest.", "Safety")
    )

    var searchQuery by remember { mutableStateOf("") }
    val filteredFaqs = faqs.filter { it.question.contains(searchQuery, ignoreCase = true) || it.answer.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Frequently Asked Questions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search help topics...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val grouped = filteredFaqs.groupBy { it.category }
                grouped.forEach { (category, items) ->
                    item {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(items) { faq ->
                        FaqExpandableCard(faq)
                    }
                }
            }
        }
    }
}

@Composable
fun FaqExpandableCard(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = faq.answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
