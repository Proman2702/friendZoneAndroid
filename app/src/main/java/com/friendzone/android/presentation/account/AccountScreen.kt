package com.friendzone.android.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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

private val AccountPageBackground = Color(0xFF120D33)
private val AccountCardBackground = Color(0xFFFAFAFC)
private val AccountAccent = Color(0xFFE3874F)
private val AccountBlue = Color(0xFF57AFE6)
private val AccountText = Color(0xFF1C1840)
private val AccountDanger = Color(0xFFFF4B4B)

@Composable
fun AccountScreen(
    onOpenInvitations: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var allowNotifications by remember { mutableStateOf(true) }
    var autoAcceptInvites by remember { mutableStateOf(true) }
    var privateProfile by remember { mutableStateOf(true) }
    var historyDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccountPageBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AccountCardBackground)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.userName,
                            color = AccountText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = state.userEmail,
                            color = AccountText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать аккаунт",
                            tint = AccountText
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Сменить пароль",
                        color = AccountBlue,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Удалить аккаунт",
                        color = AccountDanger,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionChip(
                title = "Приглашения",
                modifier = Modifier.weight(1f),
                onClick = onOpenInvitations
            )
            ActionChip(
                title = "История",
                modifier = Modifier.weight(1f),
                onClick = { historyDialogVisible = true }
            )
        }

        AccountSwitchRow(
            title = "Разрешить уведомлять друзей о вашем присутствии в их зонах",
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

        Text(
            text = "Выйти",
            color = AccountAccent,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable(onClick = onLogout)
        )
    }

    if (historyDialogVisible) {
        AlertDialog(
            onDismissRequest = { historyDialogVisible = false },
            title = { Text("История") },
            text = { Text("Пока пусто. Здесь будет история из базы данных.") },
            confirmButton = {
                TextButton(onClick = { historyDialogVisible = false }) {
                    Text("Закрыть")
                }
            }
        )
    }
}

@Composable
private fun ActionChip(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AccountAccent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
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
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AccountCardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = AccountText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 20.dp)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
