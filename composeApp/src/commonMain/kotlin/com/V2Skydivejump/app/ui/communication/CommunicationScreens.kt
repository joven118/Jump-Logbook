package com.V2Skydivejump.app.ui.communication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.V2Skydivejump.app.TimeUtils
import com.V2Skydivejump.app.database.entities.ChatMessageEntity
import com.V2Skydivejump.app.database.entities.NotificationEntity
import com.V2Skydivejump.app.database.entities.UserEntity

@Composable
fun ChatListScreen(
    users: List<UserEntity>,
    currentUserId: String?,
    onChatClick: (String) -> Unit
) {
    val chatUsers = users
        .filter { it.userId != currentUserId }
        .sortedBy { it.name.lowercase() }
    
    if (chatUsers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No registered users available for chat.", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(chatUsers, key = { it.userId }) { user ->
                ListItem(
                    headlineContent = { Text(user.name.ifBlank { user.screenName ?: "Registered User" }) },
                    supportingContent = {
                        Text(user.screenName?.let { "@$it" } ?: user.role.replace("_", " "))
                    },
                    leadingContent = {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                    },
                    modifier = Modifier.clickable { onChatClick(user.userId) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ChatDetailScreen(
    recipientName: String,
    currentUserId: String?,
    messages: List<ChatMessageEntity>,
    onSendMessage: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg, isMe = msg.senderId == currentUserId)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { 
                if (text.isNotBlank()) {
                    onSendMessage(text)
                    text = ""
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageEntity, isMe: Boolean) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = color,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 0.dp,
                bottomEnd = if (isMe) 0.dp else 16.dp
            )
        ) {
            Text(
                text = message.message,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = textColor
            )
        }
        Text(
            text = TimeUtils.formatEpochMillis(message.timestamp),
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun NotificationScreen(
    notifications: List<NotificationEntity>,
    onMarkAsRead: (Long) -> Unit
) {
    if (notifications.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No notifications", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(notifications) { note ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = note.title,
                            fontWeight = if (!note.isRead) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    supportingContent = { Text(note.content) },
                    overlineContent = { Text(TimeUtils.formatEpochMillis(note.timestamp)) },
                    leadingContent = {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (!note.isRead) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    },
                    modifier = Modifier.clickable { onMarkAsRead(note.id) }
                )
                HorizontalDivider()
            }
        }
    }
}
