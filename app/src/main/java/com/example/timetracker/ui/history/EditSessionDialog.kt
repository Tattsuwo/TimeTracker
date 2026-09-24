package com.example.timetracker.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.timetracker.data.Category
import com.example.timetracker.data.Session
import com.example.timetracker.ui.common.CategoryDropdown
import com.example.timetracker.ui.common.DateTimePickerField

@Composable
fun EditSessionDialog(
    session: Session,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onAddCategory: (String) -> Unit,
    onConfirm: (Session) -> Unit,
    onDelete: (Session) -> Unit
) {
    var name by remember { mutableStateOf(session.name) }
    var description by remember { mutableStateOf(session.description) }
    var categoryId by remember { mutableStateOf(session.categoryId) }
    var startTime by remember { mutableStateOf(session.startTime) }
    var endTime by remember { mutableStateOf(session.endTime) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier la session") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de l'activité") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                CategoryDropdown(
                    categories = categories,
                    selectedCategoryId = categoryId,
                    onCategorySelected = { categoryId = it.id },
                    onAddCategory = onAddCategory,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                DateTimePickerField(
                    label = "Début",
                    value = startTime,
                    onValueChange = { startTime = it },
                    modifier = Modifier.padding(top = 8.dp)
                )
                DateTimePickerField(
                    label = "Fin",
                    value = endTime,
                    onValueChange = { endTime = it },
                    modifier = Modifier.padding(top = 8.dp)
                )
                TextButton(onClick = { showDeleteConfirmation = true }) {
                    Text("Supprimer cette session")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    session.copy(
                        name = name.trim(),
                        description = description.trim(),
                        categoryId = categoryId,
                        startTime = startTime,
                        endTime = endTime
                    )
                )
            }) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Supprimer la session ?") },
            text = { Text("Cette action est définitive.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(session)
                    showDeleteConfirmation = false
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Annuler") }
            }
        )
    }
}
