package com.friendzone.android.presentation.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val FriendsPageBackground = Color(0xFF120D33)
private val FriendsCardBackground = Color(0xFFFAFAFC)
private val FriendsAccent = Color(0xFFE3874F)
private val FriendsBlue = Color(0xFF57AFE6)
private val FriendsText = Color(0xFF1C1840)

private data class FriendStubUi(
    val name: String,
    val tag: String,
    val muted: Boolean,
    val distanceMeters: Int
)

private val demoFriends = listOf(
    FriendStubUi("Бурда", "@obezyana1487", true, 1000),
    FriendStubUi("Махмадрозик", "@sviina_halal", false, 500),
    FriendStubUi("Аошаоушоцщцшоа", "@uyauuyoaouyoaouuyuoqusha", false, 1)
)

@Composable
fun FriendsScreen() {
    var filterText by remember { mutableStateOf("") }

    val filteredFriends = remember(filterText) {
        val query = filterText.trim()
        if (query.isBlank()) {
            demoFriends
        } else {
            demoFriends.filter { friend ->
                friend.name.contains(query, ignoreCase = true) ||
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
                    FriendCard(friend = friend)
                }
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
            modifier = Modifier.padding(start = 10.dp, top = 0.dp),
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
private fun FriendCard(friend: FriendStubUi) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FriendsCardBackground)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = friend.name,
                        color = FriendsText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = friend.tag,
                        color = FriendsAccent,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать друга",
                        tint = FriendsText
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FriendsAccent)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (friend.muted) Icons.Default.Close else Icons.Default.Check,
                    contentDescription = null,
                    tint = FriendsBlue,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (friend.muted) "Не уведомлять" else "Уведомлять",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null,
                        tint = FriendsBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${friend.distanceMeters} м.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
