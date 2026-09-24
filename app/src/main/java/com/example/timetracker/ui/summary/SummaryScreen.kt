package com.example.timetracker.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetracker.ui.rememberTimeTrackerViewModelFactory
import com.example.timetracker.util.toDisplayString

private val TAB_TITLES = listOf("Par jour", "Par activité", "Par catégorie")

@Composable
fun SummaryScreen() {
    val viewModel: SummaryViewModel = viewModel(factory = rememberTimeTrackerViewModelFactory())
    var selectedTab by remember { mutableIntStateOf(0) }

    val byDay by viewModel.byDay.collectAsStateWithLifecycle()
    val byActivity by viewModel.byActivity.collectAsStateWithLifecycle()
    val byCategory by viewModel.byCategory.collectAsStateWithLifecycle()

    val rows = when (selectedTab) {
        0 -> byDay
        1 -> byActivity
        else -> byCategory
    }

    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            TAB_TITLES.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        if (rows.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucune donnée à résumer pour l'instant.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(rows, key = { it.label }) { row ->
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(row.label, style = MaterialTheme.typography.bodyLarge)
                        Text(row.total.toDisplayString(), style = MaterialTheme.typography.bodyLarge)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
