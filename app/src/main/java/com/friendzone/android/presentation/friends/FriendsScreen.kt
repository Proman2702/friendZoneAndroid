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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    val filterState = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    val query = filterState.value.trim()
    val filteredFriends = remember(state.friends, query) {
        if (query.isBlank()) {
            state.friends
        } else {
            state.friends.filter { friend ->
                friend.displayName.contains(query, ignoreCase = true) ||
                    friend.login.contains(query, ignoreCase = true)
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
                value = filterState.value,
                onValueChange = { filterState.value = it }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filteredFriends.isEmpty()) {
                    item { EmptyFriendsCard() }
                } else {
                    items(filteredFriends, key = { it.id }) { friend ->
                        FriendCard(friend = friend, onRemove = { viewModel.removeFriend(friend.id) })
                    }
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
                text = "Filter",
                color = FriendsBlue,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun EmptyFriendsCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FriendsCardBackground)
    ) {
        Text(
            text = "No friends yet",
            color = FriendsText,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun FriendCard(
    friend: FriendUi,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FriendsCardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = friend.displayName,
                    color = FriendsText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = friend.login,
                    color = FriendsAccent,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Friend profile is loaded from backend",
                    color = FriendsBlue,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            TextButton(onClick = onRemove) {
                Text("Remove", color = FriendsAccent, fontWeight = FontWeight.Bold)
            }
        }
    }
}
