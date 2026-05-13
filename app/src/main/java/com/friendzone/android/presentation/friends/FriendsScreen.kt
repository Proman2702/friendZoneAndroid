package com.friendzone.android.presentation.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

private val FriendsPageBackground = Color(0xFF120D33)
private val FriendsCardBackground = Color(0xFFFAFAFC)
private val FriendsAccent = Color(0xFFE3874F)
private val FriendsBlue = Color(0xFF57AFE6)
private val FriendsText = Color(0xFF1C1840)

@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var filterText by remember { mutableStateOf("") }
    var editingFriend by remember { mutableStateOf<FriendUi?>(null) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    val filteredFriends = remember(state.friends, filterText) {
        val query = filterText.trim()
        if (query.isBlank()) {
            state.friends
        } else {
            state.friends.filter { friend ->
                friend.displayName.contains(query, ignoreCase = true) ||
                    friend.tag.contains(query, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FriendsPageBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            FilterField(
                value = filterText,
                onValueChange = { filterText = it }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredFriends) { friend ->
                    FriendCard(friend = friend, onEdit = { editingFriend = friend })
                }
            }
        }

        state.message?.let { message ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 92.dp)
                    .clickable { viewModel.clearMessage() },
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f))
            ) {
                Text(
                    text = message,
                    color = FriendsText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }

    editingFriend?.let { friend ->
        FriendDialog(
            friend = friend,
            onDismiss = { editingFriend = null },
            onSave = { friendId, displayName, latitude, longitude ->
                viewModel.renameFriend(friendId, displayName)
                viewModel.updateFriendLocation(friendId, latitude, longitude)
                editingFriend = null
            }
        )
    }
}

@Composable
private fun FilterField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = FriendsText,
                unfocusedTextColor = FriendsText
            )
        )
        Card(
            modifier = Modifier
                .padding(start = 10.dp)
                .offset(y = (-6).dp),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Text(
                text = "Фильтр",
                color = FriendsBlue,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun FriendCard(
    friend: FriendUi,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FriendsCardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.displayName,
                    color = FriendsText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = friend.tag,
                    color = FriendsAccent,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = friend.locationSummary(),
                    color = FriendsBlue,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Редактировать друга",
                    tint = FriendsText
                )
            }
        }
    }
}

@Composable
private fun FriendDialog(
    friend: FriendUi,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember(friend.id) { mutableStateOf(friend.displayName) }
    var latitude by remember(friend.id) { mutableStateOf(friend.latitude?.toString().orEmpty()) }
    var longitude by remember(friend.id) { mutableStateOf(friend.longitude?.toString().orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Привязка друга") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("Отображаемое имя") }
                )
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    singleLine = true,
                    label = { Text("Широта") }
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    singleLine = true,
                    label = { Text("Долгота") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(friend.id, name, latitude, longitude) }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun FriendUi.locationSummary(): String {
    return if (latitude != null && longitude != null) {
        "Широта %.5f, долгота %.5f".format(latitude, longitude)
    } else {
        "Геолокация не привязана"
    }
}
