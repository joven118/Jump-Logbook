package com.V2Skydivejump.app.ui.jumper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.V2Skydivejump.app.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Composable
fun UserFollowScreen(
    currentUserId: String?,
    users: List<UserEntity>,
    isFollowing: (String) -> Flow<Boolean>,
    onToggleFollow: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val visibleUsers = remember(users, currentUserId, query) {
        users
            .filter { it.userId != currentUserId }
            .filter { user ->
                val searchText = listOfNotNull(
                    user.name,
                    user.screenName,
                    user.email,
                    user.licenseNumber,
                    user.role,
                    user.country
                ).joinToString(" ")
                searchText.contains(query, ignoreCase = true)
            }
            .sortedWith(compareBy<UserEntity> { it.role != "JUMPER" }.thenBy { it.name.lowercase() })
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search registered users") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        if (visibleUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (users.size <= 1) "No other registered users yet." else "No users match your search.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visibleUsers, key = { it.userId }) { user ->
                    val following by isFollowing(user.userId).collectAsState(false)
                    RegisteredUserRow(
                        user = user,
                        isFollowing = following,
                        onToggleFollow = { onToggleFollow(user.userId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisteredUserRow(
    user: UserEntity,
    isFollowing: Boolean,
    onToggleFollow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (user.profilePictureUrl.isNullOrBlank()) {
                    Icon(Icons.Default.Person, contentDescription = null)
                } else {
                    AsyncImage(
                        model = user.profilePictureUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name.ifBlank { user.screenName ?: "Registered User" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = buildUserSubtitle(user),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isFollowing) {
                OutlinedButton(onClick = onToggleFollow) {
                    Text("Following")
                }
            } else {
                Button(onClick = onToggleFollow) {
                    Text("Follow")
                }
            }
        }
    }
}

private fun buildUserSubtitle(user: UserEntity): String {
    val role = user.role.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    val handle = user.screenName?.takeIf { it.isNotBlank() }?.let { "@$it" }
    val license = user.licenseNumber.takeIf { it.isNotBlank() }?.let { "License $it" }
    val location = listOfNotNull(user.city, user.country).filter { it.isNotBlank() }.joinToString(", ")
    return listOfNotNull(handle, role, license, location.takeIf { it.isNotBlank() }).joinToString(" | ")
}
