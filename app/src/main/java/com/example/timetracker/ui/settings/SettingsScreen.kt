package com.example.timetracker.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.timetracker.data.Project
import com.example.timetracker.ui.common.CATEGORY_COLOR_PALETTE
import com.example.timetracker.ui.common.CategoryColorDot
import com.example.timetracker.ui.common.ColorPickerRow
import com.example.timetracker.ui.rememberTimeTrackerViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = viewModel(factory = rememberTimeTrackerViewModelFactory())
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var categoryBeingEdited by remember { mutableStateOf<Category?>(null) }
    var categoryPendingDelete by remember { mutableStateOf<Category?>(null) }
    var pendingDeleteSessionCount by remember { mutableStateOf<Int?>(null) }
    var showImportConfirmation by remember { mutableStateOf(false) }

    var showAddProjectDialog by remember { mutableStateOf(false) }
    var projectBeingEdited by remember { mutableStateOf<Project?>(null) }
    var projectPendingDelete by remember { mutableStateOf<Project?>(null) }
    var pendingProjectDeleteSessionCount by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    // Récupère le nombre de sessions concernées dès qu'une suppression est
    // demandée, pour l'afficher dans la boîte de dialogue de confirmation.
    LaunchedEffect(categoryPendingDelete) {
        val category = categoryPendingDelete
        pendingDeleteSessionCount = if (category != null) viewModel.sessionCountFor(category) else null
    }

    // Même principe pour les projets : nombre de sessions qui seraient supprimées.
    LaunchedEffect(projectPendingDelete) {
        val project = projectPendingDelete
        pendingProjectDeleteSessionCount = if (project != null) viewModel.sessionCountForProject(project) else null
    }

    // ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT (Storage Access Framework) :
    // l'utilisateur choisit lui-même où écrire/lire le fichier via le
    // sélectionneur système. On n'a donc besoin d'aucune permission de
    // stockage (READ/WRITE_EXTERNAL_STORAGE), contrairement à un accès
    // direct au système de fichiers.
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportTo(context, it) } }

    // Même mécanisme (SAF) pour l'export Excel : seul le type de fichier change
    // (MIME d'un classeur .xlsx), ce qui permet au sélecteur système de proposer
    // le bon nom et la bonne extension.
    val excelExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri -> uri?.let { viewModel.exportExcelTo(context, it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importFrom(context, it) } }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        // Tout l'écran est UNE SEULE liste défilante (et non plus un en-tête fixe
        // suivi d'une liste de catégories) : avec deux listes (catégories puis
        // projets), la première aurait pris toute la hauteur restante et la
        // seconde n'aurait eu aucune place. Les clés (key) sont préfixées
        // ("category-", "project-") car elles doivent être uniques dans toute la
        // liste, or une catégorie et un projet peuvent avoir le même id.
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item(key = "backup-title") {
                Text("Sauvegarde", style = MaterialTheme.typography.titleLarge)
            }
            item(key = "backup-buttons") {
                // weight(1f) : les trois boutons se partagent la largeur à parts égales
                // plutôt que d'avoir chacun la largeur de son texte, pour qu'ils
                // restent sur une seule ligne même sur un écran étroit.
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { exportLauncher.launch("sauvegarde-suivi-du-temps.json") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) { Text("Exporter") }
                    OutlinedButton(
                        onClick = { showImportConfirmation = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) { Text("Importer") }
                    OutlinedButton(
                        onClick = { excelExportLauncher.launch("suivi-du-temps.xlsx") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) { Text("Export Excel") }
                }
            }

            item(key = "divider-categories") {
                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
            }

            item(key = "categories-header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Catégories", style = MaterialTheme.typography.titleLarge)
                    Button(onClick = { showAddDialog = true }) { Text("+ Ajouter") }
                }
            }
            items(categories, key = { "category-${it.id}" }) { category ->
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

            item(key = "divider-projects") {
                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
            }

            item(key = "projects-header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Projets", style = MaterialTheme.typography.titleLarge)
                    Button(onClick = { showAddProjectDialog = true }) { Text("+ Ajouter") }
                }
            }
            if (projects.isEmpty()) {
                item(key = "projects-empty") {
                    Text(
                        "Aucun projet pour l'instant.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
            items(projects, key = { "project-${it.id}" }) { project ->
                ListItem(
                    headlineContent = { Text(project.name) },
                    trailingContent = {
                        Row {
                            TextButton(onClick = { projectBeingEdited = project }) { Text("Modifier") }
                            TextButton(onClick = { projectPendingDelete = project }) { Text("Supprimer") }
                        }
                    }
                )
            }
        }
    }

    if (showImportConfirmation) {
        AlertDialog(
            onDismissRequest = { showImportConfirmation = false },
            title = { Text("Remplacer toutes les données ?") },
            text = {
                Text(
                    "L'import va supprimer TOUTES les sessions, catégories et projets actuels " +
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

    if (showAddProjectDialog) {
        ProjectNameDialog(
            title = "Nouveau projet",
            initialName = "",
            onDismiss = { showAddProjectDialog = false },
            onConfirm = { name ->
                viewModel.addProject(name)
                showAddProjectDialog = false
            }
        )
    }

    projectBeingEdited?.let { project ->
        ProjectNameDialog(
            title = "Renommer le projet",
            initialName = project.name,
            onDismiss = { projectBeingEdited = null },
            onConfirm = { name ->
                viewModel.renameProject(project, name)
                projectBeingEdited = null
            }
        )
    }

    projectPendingDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectPendingDelete = null },
            title = { Text("Supprimer « ${project.name} » ?") },
            text = {
                val count = pendingProjectDeleteSessionCount
                Text(
                    when {
                        count == null -> "Vérification des sessions concernées…"
                        count == 0 -> "Aucune session n'est rattachée à ce projet."
                        else -> "$count session(s) rattachée(s) à ce projet seront " +
                            "également supprimées. Cette action est irréversible."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProject(project)
                    projectPendingDelete = null
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { projectPendingDelete = null }) { Text("Annuler") }
            }
        )
    }
}

/** Formulaire partagé par l'ajout et le renommage d'un projet (nom seul). */
@Composable
private fun ProjectNameDialog(
    title: String,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom du projet") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
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
