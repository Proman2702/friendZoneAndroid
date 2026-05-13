package com.friendzone.android.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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

@Composable
fun AccountScreen(
    onOpenInvitations: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
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
                Text(
                    text = state.userName,
                    color = AccountText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = state.userLogin,
                    color = AccountBlue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        ActionChip(
            title = "Invitations",
            onClick = onOpenInvitations
        )
        ActionChip(
            title = "History",
            onClick = { historyDialogVisible = true }
        )

        Text(
            text = "Logout",
            color = AccountAccent,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable(onClick = onLogout)
        )

        state.message?.let { message ->
            Card(
                colors = CardDefaults.cardColors(containerColor = AccountCardBackground)
            ) {
                Text(
                    text = message,
                    color = AccountText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .clickable { viewModel.clearMessage() }
                )
            }
        }
    }

    if (historyDialogVisible) {
        AlertDialog(
            onDismissRequest = { historyDialogVisible = false },
            title = { Text("History") },
            text = {
                if (state.history.isEmpty()) {
                    Text("No backend events yet")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.history, key = { it.id }) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AccountCardBackground)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = item.title,
                                        color = AccountText,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = item.subtitle,
                                        color = AccountBlue,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { historyDialogVisible = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun ActionChip(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
