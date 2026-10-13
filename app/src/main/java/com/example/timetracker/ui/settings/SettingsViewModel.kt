package com.example.timetracker.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetracker.data.Category
import com.example.timetracker.data.Project
import com.example.timetracker.repository.TimeTrackerRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: TimeTrackerRepository) : ViewModel() {

    val categories: StateFlow<List<Category>> = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val projects: StateFlow<List<Project>> = repository.observeProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages

    fun addCategory(name: String, color: Int) {
        viewModelScope.launch {
            repository.addCategory(name, color).onFailure {
                _messages.emit(it.message ?: "Impossible de créer la catégorie.")
            }
        }
    }

    fun updateCategory(category: Category, newName: String, newColor: Int) {
        viewModelScope.launch {
            repository.updateCategory(category, newName, newColor).onFailure {
                _messages.emit(it.message ?: "Impossible de modifier la catégorie.")
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
                .onSuccess { _messages.emit("Catégorie « ${category.name} » supprimée.") }
                .onFailure { _messages.emit(it.message ?: "Impossible de supprimer la catégorie.") }
        }
    }

    fun addProject(name: String) {
        viewModelScope.launch {
            repository.addProject(name).onFailure {
                _messages.emit(it.message ?: "Impossible de créer le projet.")
            }
        }
    }

    fun renameProject(project: Project, newName: String) {
        viewModelScope.launch {
            repository.renameProject(project, newName).onFailure {
                _messages.emit(it.message ?: "Impossible de renommer le projet.")
            }
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
                .onSuccess { _messages.emit("Projet « ${project.name} » supprimé.") }
                .onFailure { _messages.emit(it.message ?: "Impossible de supprimer le projet.") }
        }
    }

    /** Nombre de sessions actuellement rattachées à ce projet (pour la confirmation de suppression). */
    suspend fun sessionCountForProject(project: Project): Int = repository.countSessionsInProject(project)

    /**
     * Nombre de sessions actuellement rattachées à cette catégorie, pour
     * prévenir l'utilisateur avant qu'il ne confirme une suppression
     * (destructrice : ces sessions seront supprimées avec la catégorie).
     */
    suspend fun sessionCountFor(category: Category): Int = repository.countSessionsInCategory(category.id)

    /** Appelé avec l'Uri renvoyée par ACTION_CREATE_DOCUMENT (voir SettingsScreen). */
    fun exportTo(context: Context, uri: Uri) {
        viewModelScope.launch {
            repository.exportToUri(context, uri)
                .onSuccess { _messages.emit("Sauvegarde exportée avec succès.") }
                .onFailure { _messages.emit("Échec de l'export : ${it.message}") }
        }
    }

    /** Appelé avec l'Uri renvoyée par ACTION_CREATE_DOCUMENT pour l'export Excel (.xlsx). */
    fun exportExcelTo(context: Context, uri: Uri) {
        viewModelScope.launch {
            repository.exportToExcelUri(context, uri)
                .onSuccess { _messages.emit("Export Excel terminé.") }
                .onFailure { _messages.emit("Échec de l'export Excel : ${it.message}") }
        }
    }

    /** Appelé avec l'Uri renvoyée par ACTION_OPEN_DOCUMENT (voir SettingsScreen). */
    fun importFrom(context: Context, uri: Uri) {
        viewModelScope.launch {
            repository.importFromUri(context, uri)
                .onSuccess {
                    _messages.emit(
                        "Import terminé : ${it.sessionsImported} session(s), " +
                            "${it.categoriesCreated} nouvelle(s) catégorie(s)."
                    )
                }
                .onFailure { _messages.emit("Échec de la restauration : ${it.message}") }
        }
    }
}
