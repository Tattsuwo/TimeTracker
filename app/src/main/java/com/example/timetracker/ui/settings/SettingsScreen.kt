package com.example.timetracker.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetracker.data.Category
import com.example.timetracker.ui.rememberTimeTrackerViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = viewModel(factory = rememberTimeTrackerViewModelFactory())
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
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
                    Text("Exporter (JSON)")
                }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier.padding(start = 8.dp)
                ) { Text("Restaurer") }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text("Catégories", style = MaterialTheme.typography.titleLarge)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(categories, key = { it.id }) { category ->
                    ListItem(
                        headlineContent = { Text(category.name) },
                        trailingContent = {
                            TextButton(onClick = { editingCategory = category }) { Text("Renommer") }
                        }
                    )
                }
            }
        }
    }

    editingCategory?.let { category ->
        RenameCategoryDialog(
            category = category,
            onDismiss = { editingCategory = null },
            onConfirm = { newName ->
                viewModel.renameCategory(category, newName)
                editingCategory = null
            }
        )
    }
}

@Composable
private fun RenameCategoryDialog(category: Category, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(category.name) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renommer la catégorie") },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true)
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
