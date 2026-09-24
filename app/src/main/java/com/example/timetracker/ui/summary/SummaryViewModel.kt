package com.example.timetracker.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetracker.data.SessionWithCategory
import com.example.timetracker.repository.TimeTrackerRepository
import com.example.timetracker.util.durationBetween
import com.example.timetracker.util.toDisplayString
import com.example.timetracker.util.toLocalDay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Duration

/** Une ligne de synthèse : un libellé (jour, activité ou catégorie) et son total. */
data class TotalRow(val label: String, val total: Duration)

private fun sumDuration(sessions: List<SessionWithCategory>): Duration =
    sessions.fold(Duration.ZERO) { acc, item ->
        acc + durationBetween(item.session.startTime, item.session.endTime)
    }

class SummaryViewModel(repository: TimeTrackerRepository) : ViewModel() {

    // Les trois vues partagent la même source (observeSessions) mais
    // regroupent différemment : c'est un simple .groupBy en Kotlin, pas trois
    // requêtes SQL séparées à maintenir.

    val byDay: StateFlow<List<TotalRow>> = repository.observeSessions()
        .map { sessions ->
            sessions.groupBy { it.session.startTime.toLocalDay() }
                .toSortedMap(compareByDescending { it })
                .map { (day, group) -> TotalRow(day.toDisplayString(), sumDuration(group)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val byActivity: StateFlow<List<TotalRow>> = repository.observeSessions()
        .map { sessions ->
            sessions.groupBy { it.session.name }
                .map { (name, group) -> TotalRow(name, sumDuration(group)) }
                .sortedByDescending { it.total }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val byCategory: StateFlow<List<TotalRow>> = repository.observeSessions()
        .map { sessions ->
            sessions.groupBy { it.categoryName }
                .map { (name, group) -> TotalRow(name, sumDuration(group)) }
                .sortedByDescending { it.total }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
