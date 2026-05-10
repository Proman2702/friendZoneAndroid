package com.friendzone.android.presentation.zones

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

private val ZonesPageBackground = Color(0xFF120D33)
private val ZonesCardBackground = Color(0xFFFAFAFC)
private val ZonesAccent = Color(0xFFE3874F)
private val ZonesBlue = Color(0xFF57AFE6)
private val ZonesRed = Color(0xFFFF3B30)
private val ZonesText = Color(0xFF1C1840)

@Composable
fun ZoneListScreen(
    viewModel: ZoneListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var filterText by remember { mutableStateOf("") }
    var editingZoneId by remember { mutableStateOf<String?>(null) }
    val expandedIds = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        viewModel.loadZones()
    }

    val filteredZones = remember(state.zones, filterText) {
        val query = filterText.trim()
        if (query.isBlank()) {
            state.zones
        } else {
            state.zones.filter { zone ->
                zone.name.contains(query, ignoreCase = true)
            }
        }
    }
    val editingZone = state.zones.firstOrNull { it.id == editingZoneId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZonesPageBackground)
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

            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(message = state.error ?: "Не удалось загрузить зоны")
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredZones) { zone ->
                            val expanded = zone.id in expandedIds
                            ZoneCard(
                                zone = zone,
                                expanded = expanded,
                                icon = iconForZone(zone, state.zones.indexOfFirst { it.id == zone.id }),
                                onToggleExpand = {
                                    if (expanded) {
                                        expandedIds.remove(zone.id)
                                    } else {
                                        expandedIds.add(zone.id)
                                    }
                                },
                                onEdit = { editingZoneId = zone.id }
                            )
                        }
                    }
                }
            }
        }

        state.message?.let { message ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable { viewModel.clearMessage() },
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f))
            ) {
                Text(
                    text = message,
                    color = ZonesText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }

    if (editingZone != null) {
        ZoneEditorDialog(
            title = "Редактирование зоны",
            confirmText = "Сохранить",
            initialName = editingZone.name,
            initialRadius = editingZone.radiusMeters.toInt().toString(),
            initialIsActive = editingZone.isActive,
            onDismiss = { editingZoneId = null },
            onConfirm = { name, radius, isActive ->
                viewModel.updateZone(
                    editingZone.copy(
                        name = name,
                        radiusMeters = radius,
                        isActive = isActive
                    )
                )
                editingZoneId = null
            },
            onDelete = {
                viewModel.deleteZone(editingZone.id)
                editingZoneId = null
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
                focusedTextColor = ZonesText,
                unfocusedTextColor = ZonesText
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
                color = ZonesBlue,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = ZonesAccent)
        Text(
            text = "Загружаем зоны",
            color = Color.White,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = Color.White)
    }
}

@Composable
private fun ZoneCard(
    zone: ZoneUi,
    expanded: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit
) {
    Column {
        Text(
            text = if (zone.isActive) "Активно" else "Неактивно",
            color = if (zone.isActive) ZonesBlue else ZonesRed,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 14.dp, bottom = 4.dp)
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ZonesCardBackground)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ZonesText,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {
                        Text(
                            text = zone.name,
                            color = ZonesText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${zone.radiusMeters.toInt()} м.",
                            color = ZonesAccent,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать зону",
                            tint = ZonesText
                        )
                    }
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Развернуть",
                            tint = ZonesText
                        )
                    }
                }

                if (expanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = ZonesAccent,
                                shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Уведомлять об обнаружении:",
                                color = ZonesText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Вы (создатель)",
                                color = Color.White,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            Text(
                                text = "• Локальная зона",
                                color = Color.White,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun iconForZone(
    zone: ZoneUi,
    index: Int
): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        zone.name.contains("дом", ignoreCase = true) -> Icons.Default.PanTool
        zone.name.contains("мир", ignoreCase = true) -> Icons.Default.Public
        zone.isActive && index % 2 == 0 -> Icons.Default.Public
        zone.isActive -> Icons.Default.PanTool
        index % 2 == 0 -> Icons.Default.Timeline
        else -> Icons.Default.AccessTime
    }
}
