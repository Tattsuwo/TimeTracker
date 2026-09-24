package com.example.timetracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetracker.data.Category
import com.example.timetracker.data.Session
import com.example.timetracker.data.SessionWithCategory
import com.example.timetracker.repository.TimeTrackerRepository
import com.example.timetracker.util.durationBetween
import com.example.timetracker.util.toLocalDay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate

/** Un jour et ses sessions, avec le total déjà calculé pour l'en-tête. */
data class DayGroup(
    val day: LocalDate,
    val sessions: List<SessionWithCategory>,
    val totalDuration: Duration
)

class HistoryViewModel(private val repository: TimeTrackerRepository) : ViewModel() {

    val categories: StateFlow<List<Category>> = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dayGroups: StateFlow<List<DayGroup>> = repository.observeSessions()
        .map { sessions ->
            sessions
                .groupBy { it.session.startTime.toLocalDay() }
                // Le plus récent d'abord : c'est ce qu'on veut voir en premier
                // en ouvrant l'historique.
                .toSortedMap(compareByDescending { it })
                .map { (day, sessionsOfDay) ->
                    val sorted = sessionsOfDay.sortedByDescending { it.session.startTime }
                    val total = sorted.fold(Duration.ZERO) { acc, item ->
                        acc + durationBetween(item.session.startTime, item.session.endTime)
                    }
                    DayGroup(day, sorted, total)
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    // La logique de regroupement/somme est faite ici en Kotlin plutôt qu'en
    // SQL (GROUP BY) : le volume de données d'un tracker personnel reste
    // faible, et garder le calcul en Kotlin le rend beaucoup plus facile à
    // relire et à faire évoluer (ex. filtrer par catégorie) qu'une requête
    // agrégée.

    fun updateSession(session: Session) {
        viewModelScope.launch { repository.updateSession(session) }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch { repository.deleteSession(session) }
    }

    fun addCategory(name: String) {
        viewModelScope.launch { repository.addCategory(name) }
    }
}
