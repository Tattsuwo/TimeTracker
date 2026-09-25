package com.example.timetracker.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetracker.data.Category
import com.example.timetracker.ui.common.CATEGORY_COLOR_PALETTE
import com.example.timetracker.ui.common.CategoryColorDot
import com.example.timetracker.ui.common.ColorPickerRow
import com.example.timetracker.ui.rememberTimeTrackerViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = viewModel(factory = rememberTimeTrackerViewModelFactory())
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var categoryBeingEdited by remember { mutableStateOf<Category?>(null) }
    var categoryPendingDelete by remember { mutableStateOf<Category?>(null) }
    var pendingDeleteSessionCount by remember { mutableStateOf<Int?>(null) }
    var showImportConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    // Récupère le nombre de sessions concernées dès qu'une suppression est
    // demandée, pour l'afficher dans la boîte de dialogue de confirmation.
    LaunchedEffect(categoryPendingDelete) {
        val category = categoryPendingDelete
        pendingDeleteSessionCount = if (category != null) viewModel.sessionCountFor(category) else null
    }

    // ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT (Storage Access Framework) :
    // l'utilisateur choisit lui-même où écrire/lire le fichier via le
    // sélectionneur système. On n'a donc besoin d'aucune permission de
    // stockage (READ/WRITE_EXTERNAL_STORAGE), contrairement à un accès
    // direct au système de fichiers.
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportTo(context, it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importFrom(context, it) } }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Sauvegarde", style = MaterialTheme.typography.titleLarge)
            Row(modifier = Modifier.padding(top = 12.dp)) {
                OutlinedButton(onClick = { exportLauncher.launch("sauvegarde-suivi-du-temps.json") }) {
                    Text("Exporter")
                }
                OutlinedButton(
                    onClick = { showImportConfirmation = true },
                    modifier = Modifier.padding(start = 8.dp)
                ) { Text("Importer") }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text("Catégories", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { showAddDialog = true }) { Text("+ Ajouter") }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(categories, key = { it.id }) { category ->
                    ListItem(
                        leadingContent = { CategoryColorDot(Color(category.color)) },
                        headlineContent = { Text(category.name) },
                        trailingContent = {
                            Row {
                                TextButton(onClick = { categoryBeingEdited = category }) { Text("Modifier") }
                                TextButton(onClick = { categoryPendingDelete = category }) { Text("Supprimer") }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showImportConfirmation) {
        AlertDialog(
            onDismissRequest = { showImportConfirmation = false },
            title = { Text("Remplacer toutes les données ?") },
            text = {
                Text(
                    "L'import va supprimer TOUTES les sessions et catégories actuelles " +
                        "pour les remplacer par le contenu du fichier choisi. Cette action " +
                        "est irréversible."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showImportConfirmation = false
                    importLauncher.launch(arrayOf("application/json"))
                }) { Text("Continuer") }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmation = false }) { Text("Annuler") }
            }
        )
    }

    if (showAddDialog) {
        CategoryFormDialog(
            title = "Nouvelle catégorie",
            initialName = "",
            initialColor = CATEGORY_COLOR_PALETTE.first(),
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color ->
                viewModel.addCategory(name, color.toArgb())
                showAddDialog = false
            }
        )
    }

    categoryBeingEdited?.let { category ->
        CategoryFormDialog(
            title = "Modifier la catégorie",
            initialName = category.name,
            initialColor = Color(category.color),
            onDismiss = { categoryBeingEdited = null },
            onConfirm = { name, color ->
                viewModel.updateCategory(category, name, color.toArgb())
                categoryBeingEdited = null
            }
        )
    }

    categoryPendingDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryPendingDelete = null },
            title = { Text("Supprimer « ${category.name} » ?") },
            text = {
                val count = pendingDeleteSessionCount
                Text(
                    when {
                        count == null -> "Vérification des sessions concernées…"
                        count == 0 -> "Aucune session n'est rattachée à cette catégorie."
                        else -> "$count session(s) rattachée(s) à cette catégorie seront " +
                            "également supprimées. Cette action est irréversible."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(category)
                    categoryPendingDelete = null
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { categoryPendingDelete = null }) { Text("Annuler") }
            }
        )
    }
}

/** Formulaire partagé par l'ajout et la modification d'une catégorie (nom + couleur). */
@Composable
private fun CategoryFormDialog(
    title: String,
    initialName: String,
    initialColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: Color) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var color by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Couleur",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                ColorPickerRow(selectedColor = color, onColorSelected = { color = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, color) }) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
