package com.example.timetracker.ui.timer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.timetracker.data.Category
import com.example.timetracker.data.Project
import com.example.timetracker.ui.common.CategoryDropdown
import com.example.timetracker.ui.common.DateTimePickerField
import com.example.timetracker.ui.common.ProjectDropdown
import java.time.Instant

/**
 * Formulaire de fin de session, utilisé dans deux contextes :
 * - arrêt normal du chrono (allowEditingEndTime = false, fin = maintenant) ;
 * - clôture d'une session restée ouverte après une interruption
 *   (allowEditingEndTime = true, l'utilisateur choisit lui-même la fin
 *   puisque "maintenant" n'a probablement plus de sens).
 */
@Composable
fun StopSessionDialog(
    categories: List<Category>,
    projects: List<Project>,
    initialEndTime: Instant,
    allowEditingEndTime: Boolean,
    onDismiss: () -> Unit,
    onAddCategory: (String) -> Unit,
    onAddProject: (String) -> Unit,
    onConfirm: (name: String, description: String, categoryId: Long, endTime: Instant) -> Unit
) {
    // Nom du projet choisi dans la liste (vide tant que rien n'est sélectionné).
    var projectName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var endTime by remember { mutableStateOf(initialEndTime) }

    // Sélectionne une catégorie par défaut dès qu'elles sont chargées, pour
    // éviter d'obliger l'utilisateur à ouvrir le menu à chaque fois.
    LaunchedEffect(categories) {
        if (selectedCategoryId == null) {
            selectedCategoryId = categories.firstOrNull()?.id
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (allowEditingEndTime) "Clôturer la session" else "Enregistrer la session") },
        text = {
            Column {
                ProjectDropdown(
                    projects = projects,
                    selectedName = projectName,
                    onProjectSelected = { projectName = it },
                    onAddProject = onAddProject,
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
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = { selectedCategoryId = it.id },
                    onAddCategory = onAddCategory,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                if (allowEditingEndTime) {
                    DateTimePickerField(
                        label = "Heure de fin",
                        value = endTime,
                        onValueChange = { endTime = it },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            // Désactivé tant qu'un projet n'est pas choisi : le projet est obligatoire.
            TextButton(
                enabled = projectName.isNotBlank(),
                onClick = {
                    selectedCategoryId?.let { categoryId ->
                        onConfirm(projectName, description, categoryId, endTime)
                    }
                }
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
