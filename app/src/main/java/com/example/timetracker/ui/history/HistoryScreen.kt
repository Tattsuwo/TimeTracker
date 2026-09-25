package com.example.timetracker.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetracker.data.SessionWithCategory
import com.example.timetracker.ui.common.CategoryColorDot
import com.example.timetracker.ui.common.attenuated
import com.example.timetracker.ui.rememberTimeTrackerViewModelFactory
import com.example.timetracker.util.durationBetween
import com.example.timetracker.util.toDisplayString
import com.example.timetracker.util.toHourString
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen() {
    val viewModel: HistoryViewModel = viewModel(factory = rememberTimeTrackerViewModelFactory())
    val dayGroups by viewModel.dayGroups.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var editingSession by remember { mutableStateOf<SessionWithCategory?>(null) }

    // Jours actuellement repliés (leurs sessions sont masquées, seul l'en-tête
    // avec le total reste visible). Un Set plutôt qu'un simple booléen "tout
    // replié" : chaque jour garde son état individuel, tout en permettant le
    // bouton global "tout fermer / tout ouvrir" ci-dessous.
    var collapsedDays by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }

    if (dayGroups.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucune session enregistrée pour l'instant.")
        }
        return
    }

    // Le bouton bascule selon l'état actuel : si tous les jours affichés sont
    // déjà repliés, il propose de tout rouvrir, sinon de tout refermer.
    val allCollapsed = dayGroups.all { it.day in collapsedDays }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                collapsedDays = if (allCollapsed) emptySet() else dayGroups.map { it.day }.toSet()
            }) {
                Text(if (allCollapsed) "Tout ouvrir" else "Tout fermer")
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            dayGroups.forEach { group ->
                val isCollapsed = group.day in collapsedDays
                stickyHeader {
                    DayHeader(
                        dayLabel = group.day.toDisplayString(),
                        total = group.totalDuration.toDisplayString(),
                        isCollapsed = isCollapsed,
                        onToggle = {
                            collapsedDays = if (isCollapsed) {
                                collapsedDays - group.day
                            } else {
                                collapsedDays + group.day
                            }
                        }
                    )
                }
                if (!isCollapsed) {
                    items(group.sessions, key = { it.session.id }) { item ->
                        SessionRow(item = item, onClick = { editingSession = item })
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    editingSession?.let { current ->
        EditSessionDialog(
            session = current.session,
            categories = categories,
            onDismiss = { editingSession = null },
            onAddCategory = viewModel::addCategory,
            onConfirm = { updated ->
                viewModel.updateSession(updated)
                editingSession = null
            },
            onDelete = { toDelete ->
                viewModel.deleteSession(toDelete)
                editingSession = null
            }
        )
    }
}

@Composable
private fun DayHeader(dayLabel: String, total: String, isCollapsed: Boolean, onToggle: () -> Unit) {
    // "▸"/"▾" : simples caractères Unicode (pas des icônes ni des emojis),
    // suffisants pour indiquer l'état replié/déplié sans dépendance supplémentaire.
    val chevron = if (isCollapsed) "▸" else "▾"
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$chevron $dayLabel", style = MaterialTheme.typography.titleMedium)
            Text("Total : $total", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SessionRow(item: SessionWithCategory, onClick: () -> Unit) {
    val session = item.session
    val tintedBackground = Color(item.categoryColor).attenuated(MaterialTheme.colorScheme.surface)
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = tintedBackground),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(session.name, style = MaterialTheme.typography.titleMedium)
                AssistChip(
                    onClick = {},
                    label = { Text(item.categoryName) },
                    leadingIcon = { CategoryColorDot(Color(item.categoryColor)) }
                )
            }
            if (session.description.isNotBlank()) {
                Text(session.description, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "${session.startTime.toHourString()} – ${session.endTime.toHourString()} " +
                    "(${durationBetween(session.startTime, session.endTime).toDisplayString()})",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
