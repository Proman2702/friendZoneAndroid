package com.friendzone.android.presentation.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.friendzone.android.presentation.theme.AppBackground

@Composable
fun AccountScreen(
    onLogout: () -> Unit
) {
    var allowNotifications by remember { mutableStateOf(true) }
    var autoAcceptInvites by remember { mutableStateOf(true) }
    var privateProfile by remember { mutableStateOf(false) }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Гость FriendZone", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "guest@friendzone.local",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            AccountSwitchRow(
                title = "Разрешить уведомлять друзей о вашем присутствии",
                checked = allowNotifications,
                onCheckedChange = { allowNotifications = it }
            )
            AccountSwitchRow(
                title = "Автоматически принимать приглашения",
                checked = autoAcceptInvites,
                onCheckedChange = { autoAcceptInvites = it }
            )
            AccountSwitchRow(
                title = "Закрыть профиль",
                checked = privateProfile,
                onCheckedChange = { privateProfile = it }
            )

            Button(
                onClick = onLogout,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Выйти")
            }
        }
    }
}

@Composable
private fun AccountSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
