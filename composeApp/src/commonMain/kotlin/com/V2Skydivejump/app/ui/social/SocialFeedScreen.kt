package com.V2Skydivejump.app.ui.social

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
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
import com.V2Skydivejump.app.database.entities.FeedCommentEntity
import com.V2Skydivejump.app.database.entities.FeedPostEntity
import kotlinx.coroutines.flow.Flow
import coil3.compose.AsyncImage
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalFeedScreen(
    posts: List<FeedPostEntity>,
    onLikePost: (FeedPostEntity) -> Unit,
    onAddComment: (Long, String) -> Unit,
    getComments: (Long) -> Flow<List<FeedCommentEntity>>
) {
    Scaffold { innerPadding ->
        if (posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No activity yet. Share your first jump!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(posts) { post ->
                    val comments by getComments(post.id).collectAsState(emptyList())
                    FeedPostCard(
                        post = post, 
                        comments = comments,
                        onLike = { onLikePost(post) },
                        onComment = { onAddComment(post.id, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun FullSizeMediaDialog(
    mediaUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = mediaUrl,
                contentDescription = "Full Size Media",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun MediaGrid(
    mediaUrls: List<String>,
    onMediaClick: (String) -> Unit
) {
    val maxDisplay = 4
    val displayList = mediaUrls.take(maxDisplay)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (displayList.size == 1) {
            AsyncImage(
                model = displayList[0],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray)
                    .clickable { onMediaClick(displayList[0]) },
                contentScale = ContentScale.Crop
            )
        } else if (displayList.size == 2) {
            Row(modifier = Modifier.height(150.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                displayList.forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier.weight(1f).fillMaxHeight().clickable { onMediaClick(url) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        } else if (displayList.size == 3) {
            Row(modifier = Modifier.height(150.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                AsyncImage(
                    model = displayList[0],
                    contentDescription = null,
                    modifier = Modifier.weight(1f).fillMaxHeight().clickable { onMediaClick(displayList[0]) },
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    displayList.drop(1).forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier.weight(1f).fillMaxWidth().clickable { onMediaClick(url) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        } else if (displayList.size >= 4) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(modifier = Modifier.height(120.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    displayList.take(2).forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier.weight(1f).fillMaxHeight().clickable { onMediaClick(url) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Row(modifier = Modifier.height(120.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    AsyncImage(
                        model = displayList[2],
                        contentDescription = null,
                        modifier = Modifier.weight(1f).fillMaxHeight().clickable { onMediaClick(displayList[2]) },
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        AsyncImage(
                            model = displayList[3],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clickable { onMediaClick(displayList[3]) },
                            contentScale = ContentScale.Crop
                        )
                        if (mediaUrls.size > 4) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable { onMediaClick(displayList[3]) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+${mediaUrls.size - 3}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedPostCard(
    post: FeedPostEntity, 
    comments: List<FeedCommentEntity>,
    onLike: () -> Unit,
    onComment: (String) -> Unit
) {
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Text(text = if(post.userName.isNotEmpty()) post.userName.take(1).uppercase() else "?", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = post.userName.ifBlank { "Unknown Jumper" }, fontWeight = FontWeight.Bold)
                    Text(text = if(post.userRole == "JUMPER") "Jumper" else "DZ Operator", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(text = TimeUtils.formatEpochMillis(post.timestamp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = post.content, style = MaterialTheme.typography.bodyLarge)
            
            var selectedFullMedia by remember { mutableStateOf<String?>(null) }
            
            val mediaList = remember(post.mediaUrl) {
                post.mediaUrl?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
            }

            if (mediaList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                MediaGrid(
                    mediaUrls = mediaList,
                    onMediaClick = { selectedFullMedia = it }
                )
            }
            
            if (selectedFullMedia != null) {
                FullSizeMediaDialog(
                    mediaUrl = selectedFullMedia!!,
                    onDismiss = { selectedFullMedia = null }
                )
            }

            if (post.type == "JUMP") {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Jump Log Shared", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLike) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (post.likes > 0) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.likes > 0) Color.Red else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        if (post.likes > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = post.likes.toString(), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = { showComments = !showComments }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send, // Use Send as the action icon
                            contentDescription = "Comment",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        if (comments.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = comments.size.toString(), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (showComments) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    comments.forEach { comment ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(text = "${comment.userName}: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Text(text = comment.content, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Write a comment or emoji...", fontSize = 12.sp) },
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(20.dp),
                            trailingIcon = {
                                TextButton(onClick = { 
                                    if (commentText.isNotBlank()) {
                                        onComment(commentText)
                                        commentText = ""
                                    }
                                }) {
                                    Text("Post", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                    
                    // Emoji quick-select
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("🪂", "🔥", "🤘", "🙌", "🤙", "👏").forEach { emoji ->
                            Text(
                                text = emoji,
                                modifier = Modifier.clickable { onComment(emoji) },
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
