package com.example.timetracker.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.timetracker.data.Project

/**
 * Menu déroulant de sélection du projet, construit sur le même modèle que
 * [CategoryDropdown] : une entrée "+ Nouveau projet" en bas de liste ouvre une
 * mini boîte de dialogue de saisie, pour ajouter un projet exactement au moment
 * où on en a besoin (à l'arrêt du chrono ou à l'édition d'une session).
 *
 * Contrairement aux catégories, le projet est identifié par son NOM (c'est ce
 * que stocke chaque session) : [selectedName] et [onProjectSelected] manipulent
 * donc un texte, pas un identifiant.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDropdown(
    projects: List<Project>,
    selectedName: String,
    onProjectSelected: (String) -> Unit,
    onAddProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Projet") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            projects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = {
                        onProjectSelected(project.name)
                        expanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("+ Nouveau projet") },
                onClick = {
                    expanded = false
                    showAddDialog = true
                }
            )
        }
    }

    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nouveau projet") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nom du projet") },
                    singleLine = true,
                    modifier = Modifier.padding(top = 4.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = newName.trim()
                    if (trimmed.isNotEmpty()) {
                        // Si un projet du même nom existe déjà (majuscules mises à
                        // part), on sélectionne simplement celui-là plutôt que
                        // d'en créer un quasi-doublon. Sinon on le crée, puis on
                        // le sélectionne tout de suite : le menu se mettra à jour
                        // de lui-même, la sélection se fait par le nom.
                        val existing = projects.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
                        if (existing != null) {
                            onProjectSelected(existing.name)
                        } else {
                            onAddProject(trimmed)
                            onProjectSelected(trimmed)
                        }
                        showAddDialog = false
                    }
                }) { Text("Ajouter") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Annuler") }
            }
        )
    }
}
