package com.friendzone.android.presentation.zones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

private val DialogBackground = Color(0xFFF8F6FF)
private val DialogCardBackground = Color(0xFFFFE7D8)
private val DialogAccent = Color(0xFFE3874F)
private val DialogText = Color(0xFF1C1840)
private val DialogMuted = Color(0xFF817A99)
private val DialogDanger = Color(0xFFD85B4D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneEditorDialog(
    title: String,
    confirmText: String,
    initialName: String,
    initialRadius: String,
    initialIsActive: Boolean,
    maxRadiusMeters: Int,
    friends: List<FriendOptionUi>,
    initialDetectorFriendIds: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Boolean, List<String>) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember(title, initialName) { mutableStateOf(initialName) }
    var radiusText by remember(title, initialRadius) { mutableStateOf(initialRadius) }
    var isActive by remember(title, initialIsActive) { mutableStateOf(initialIsActive) }
    var friendsExpanded by remember { mutableStateOf(false) }
    val selectedFriendIds = remember(title, initialDetectorFriendIds) {
        mutableStateListOf<String>().apply { addAll(initialDetectorFriendIds) }
    }
    val selectedNames = friends
        .filter { it.id in selectedFriendIds }
        .joinToString { it.displayName }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogBackground,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = title,
                color = DialogText,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .widthIn(min = 280.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DialogCardBackground),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FieldLabel("Имя зоны")
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = dialogFieldColors()
                        )
                        FieldLabel("Радиус, м")
                        OutlinedTextField(
                            value = radiusText,
                            onValueChange = { value ->
                                val digits = value.filter { symbol -> symbol.isDigit() }
                                radiusText = digits.toIntOrNull()
                                    ?.coerceIn(50, maxRadiusMeters)
                                    ?.toString()
                                    ?: digits
                            },
                            supportingText = { Text("Максимум: $maxRadiusMeters м") },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = dialogFieldColors()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Активна",
                                color = DialogText,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(checked = isActive, onCheckedChange = { isActive = it })
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = DialogCardBackground),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FieldLabel("Обнаружение друзей")
                        if (friends.isEmpty()) {
                            Text(
                                text = "Список друзей пуст",
                                color = DialogMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            ExposedDropdownMenuBox(
                                expanded = friendsExpanded,
                                onExpandedChange = { friendsExpanded = !friendsExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedNames.ifBlank { "Выберите друзей" },
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = dialogFieldColors(),
                                    trailingIcon = {
                                        Icon(
                                            imageVector = if (friendsExpanded) {
                                                Icons.Default.KeyboardArrowUp
                                            } else {
                                                Icons.Default.KeyboardArrowDown
                                            },
                                            contentDescription = null,
                                            tint = DialogAccent
                                        )
                                    }
                                )
                                DropdownMenu(
                                    expanded = friendsExpanded,
                                    onDismissRequest = { friendsExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    friends.forEach { friend ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = friend.id in selectedFriendIds,
                                                        onCheckedChange = null
                                                    )
                                                    Column(modifier = Modifier.padding(start = 6.dp)) {
                                                        Text(text = friend.displayName, color = DialogText)
                                                        Text(
                                                            text = friend.tag,
                                                            color = DialogAccent,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                if (friend.id in selectedFriendIds) {
                                                    selectedFriendIds.remove(friend.id)
                                                } else {
                                                    selectedFriendIds.add(friend.id)
                                                }
                                            },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Удалить", color = DialogDanger, style = MaterialTheme.typography.labelLarge)
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Отмена", color = DialogMuted, style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = {
                        onConfirm(
                            name.ifBlank { "Новая зона" },
                            (radiusText.toDoubleOrNull() ?: 300.0).coerceIn(50.0, maxRadiusMeters.toDouble()),
                            isActive,
                            selectedFriendIds.toList()
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DialogAccent,
                        contentColor = Color.White
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(confirmText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {}
    )
}

@Composable
private fun FieldLabel(text: String) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = DialogAccent)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun dialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = DialogAccent,
    unfocusedBorderColor = Color.Transparent,
    focusedLabelColor = DialogAccent,
    unfocusedLabelColor = DialogText,
    focusedTextColor = DialogText,
    unfocusedTextColor = DialogText,
    focusedSupportingTextColor = DialogAccent,
    unfocusedSupportingTextColor = DialogMuted
)
