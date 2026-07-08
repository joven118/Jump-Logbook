package com.V2Skydivejump.app.ui.jumper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.V2Skydivejump.app.database.entities.StudentSkillEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSyllabusScreen(
    userName: String,
    skills: List<StudentSkillEntity>,
    onBack: () -> Unit
) {
    val categories = listOf("AFF", "A_LICENSE", "B_LICENSE")
    var selectedCategory by remember { mutableStateOf("AFF") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digital Syllabus: $userName") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = categories.indexOf(selectedCategory)) {
                categories.forEach { cat ->
                    Tab(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        text = { Text(cat.replace("_", " ")) }
                    )
                }
            }

            val filteredSkills = skills.filter { it.category == selectedCategory }
            val completedCount = filteredSkills.count { it.isCompleted }
            val totalCount = filteredSkills.size

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Progress", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape)
                )
                Text(
                    "$completedCount / $totalCount Requirements Completed",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredSkills) { skill ->
                    SkillRequirementCard(skill)
                }
            }
        }
    }
}

@Composable
fun SkillRequirementCard(skill: StudentSkillEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (skill.isCompleted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (skill.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (skill.isCompleted) Color(0xFF2E7D32) else Color.Gray
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(skill.skillName, fontWeight = FontWeight.Bold)
                if (skill.isCompleted) {
                    Text(
                        "Verified by ${skill.instructorName} on ${com.V2Skydivejump.app.TimeUtils.formatEpochMillis(skill.completionDate ?: 0)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                } else {
                    Text("Requirement Pending", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorSkillMatrix(
    studentName: String,
    skills: List<StudentSkillEntity>,
    onToggleSkill: (StudentSkillEntity) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Skills: $studentName") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val grouped = skills.groupBy { it.category }
            grouped.forEach { (category, categorySkills) ->
                item {
                    Text(
                        category.replace("_", " "),
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
                items(categorySkills) { skill ->
                    ListItem(
                        headlineContent = { Text(skill.skillName) },
                        trailingContent = {
                            Checkbox(
                                checked = skill.isCompleted,
                                onCheckedChange = { onToggleSkill(skill.copy(isCompleted = it)) }
                            )
                        },
                        modifier = Modifier.clickable { onToggleSkill(skill.copy(isCompleted = !skill.isCompleted)) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
