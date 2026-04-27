package com.friendzone.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.OutlinedTextField
import com.friendzone.android.ui.theme.AppBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneListScreen(
    onCreateZone: () -> Unit,
    onOpenEvents: () -> Unit,
    onEditZone: (String) -> Unit,
    viewModel: ZoneListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var baseUrlDraft by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadZones()
        viewModel.loadBaseUrl()
    }

    AppBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Zones", style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        IconButton(onClick = { viewModel.loadZones() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                        IconButton(onClick = {
                            baseUrlDraft = state.apiBaseUrl
                            showSettings = true
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = "API settings")
                        }
                        IconButton(onClick = onOpenEvents) {
                            Icon(Icons.Default.History, contentDescription = "Events")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onCreateZone,
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create zone")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                when {
                    state.isLoading -> LoadingState()
                    state.error != null -> ErrorState(
                        state.error ?: "Unknown error",
                        onRetry = { viewModel.loadZones() }
                    )
                    else -> ZoneList(state.zones, onEditZone)
                }
            }
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateBaseUrl(baseUrlDraft.trim())
                    showSettings = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showSettings = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("API Base URL") },
            text = {
                Column {
                    Text("Set base URL for backend (example: http://10.0.2.2:8080/)")
                    OutlinedTextField(
                        value = baseUrlDraft,
                        onValueChange = { baseUrlDraft = it },
                        label = { Text("Base URL") },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        )
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Text("Loading zones...", modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun ZoneList(zones: List<ZoneUi>, onEditZone: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(zones) { zone ->
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onEditZone(zone.id) }
                    .shadow(8.dp, shape = MaterialTheme.shapes.medium, clip = false),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(zone.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${"%.0f".format(zone.radiusMeters)} m • ${if (zone.isActive) "Active" else "Inactive"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
