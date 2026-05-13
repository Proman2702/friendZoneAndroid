package com.friendzone.android.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

private val InvitationsBackground = Color(0xFF120D33)
private val InvitationsCard = Color(0xFFFAFAFC)
private val InvitationsAccent = Color(0xFFE3874F)
private val InvitationsText = Color(0xFF1C1840)

@Composable
fun InvitationsScreen(
    onBack: () -> Unit,
    viewModel: InvitationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var login by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InvitationsBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = InvitationsText
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = InvitationsCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Invitations",
                        color = InvitationsText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    OutlinedTextField(
                        value = login,
                        onValueChange = { login = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        singleLine = true,
                        label = { Text("User login") },
                        placeholder = { Text("friend_login") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = InvitationsAccent,
                            unfocusedBorderColor = InvitationsAccent.copy(alpha = 0.7f),
                            focusedTextColor = InvitationsText,
                            unfocusedTextColor = InvitationsText,
                            focusedLabelColor = InvitationsAccent,
                            unfocusedLabelColor = InvitationsText
                        )
                    )
                    Button(
                        onClick = {
                            viewModel.sendInvitation(login)
                            login = ""
                        },
                        modifier = Modifier.padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = InvitationsAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Send", fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { SectionTitle("Outgoing") }
                if (state.invited.isEmpty()) {
                    item { EmptyInvitationCard("No outgoing invitations") }
                } else {
                    items(state.invited, key = { it.id }) { invitation ->
                        InvitationCard(invitation = invitation, showActions = false)
                    }
                }

                item { SectionTitle("Incoming") }
                if (state.incoming.isEmpty()) {
                    item { EmptyInvitationCard("No incoming invitations") }
                } else {
                    items(state.incoming, key = { it.id }) { invitation ->
                        InvitationCard(
                            invitation = invitation,
                            showActions = true,
                            onAccept = { viewModel.acceptInvitation(invitation.id) },
                            onDecline = { viewModel.declineInvitation(invitation.id) }
                        )
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
                    color = InvitationsText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun InvitationCard(
    invitation: InvitationUi,
    showActions: Boolean,
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = InvitationsCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = invitation.displayName,
                color = InvitationsText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = invitation.login,
                color = InvitationsAccent,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = invitation.status,
                color = InvitationsText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (showActions) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = { onAccept?.invoke() }) {
                        Text("Accept", color = InvitationsAccent, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { onDecline?.invoke() }) {
                        Text("Decline", color = InvitationsText)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyInvitationCard(text: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = InvitationsCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = InvitationsText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
