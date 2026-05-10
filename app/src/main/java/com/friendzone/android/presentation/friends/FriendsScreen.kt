package com.friendzone.android.presentation.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.friendzone.android.presentation.theme.AppBackground

private data class FriendStubUi(
    val name: String,
    val tag: String,
    val status: String
)

private val demoFriends = listOf(
    FriendStubUi("Бурда", "@obezyana1487", "Не уведомлять"),
    FriendStubUi("Махмадрозик", "@sviina_halal", "Уведомлять"),
    FriendStubUi("Аошаоушощцщшоа", "@uyauuyoaouyoaouuyuoqusha", "Уведомлять")
)

@Composable
fun FriendsScreen() {
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Друзья",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Переходы готовы, данные пока показаны заглушкой.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(demoFriends) { friend ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(friend.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = friend.tag,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = friend.status,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
