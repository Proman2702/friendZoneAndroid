package com.friendzone.android.presentation.zones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ZoneEditorDialog(
    title: String,
    confirmText: String,
    initialName: String,
    initialRadius: String,
    initialIsActive: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Boolean) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember(title, initialName) { mutableStateOf(initialName) }
    var radiusText by remember(title, initialRadius) { mutableStateOf(initialRadius) }
    var isActive by remember(title, initialIsActive) { mutableStateOf(initialIsActive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя зоны") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = radiusText,
                    onValueChange = { radiusText = it.filter { symbol -> symbol.isDigit() } },
                    label = { Text("Радиус, м") },
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Активна",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        name.ifBlank { "Новая зона" },
                        radiusText.toDoubleOrNull() ?: 300.0,
                        isActive
                    )
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Удалить", color = Color(0xFFB3261E))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        }
    )
}
