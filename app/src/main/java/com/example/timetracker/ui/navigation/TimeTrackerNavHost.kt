package com.example.timetracker.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.timetracker.ui.common.HistoryIcon
import com.example.timetracker.ui.common.SettingsIcon
import com.example.timetracker.ui.common.SummaryIcon
import com.example.timetracker.ui.common.TimerIcon
import com.example.timetracker.ui.history.HistoryScreen
import com.example.timetracker.ui.settings.SettingsScreen
import com.example.timetracker.ui.summary.SummaryScreen
import com.example.timetracker.ui.timer.TimerScreen

@Composable
fun TimeTrackerApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            NavigationBar {
                TimeTrackerDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute?.hierarchy?.any { it.route == destination.route } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                // Comportement standard d'une barre de navigation du
                                // bas : revenir sur un onglet ne l'empile pas
                                // indéfiniment et conserve son état de défilement.
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            when (destination) {
                                TimeTrackerDestination.TIMER -> TimerIcon()
                                TimeTrackerDestination.HISTORY -> HistoryIcon()
                                TimeTrackerDestination.SUMMARY -> SummaryIcon()
                                TimeTrackerDestination.SETTINGS -> SettingsIcon()
                            }
                        },
                        label = { androidx.compose.material3.Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TimeTrackerDestination.TIMER.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(TimeTrackerDestination.TIMER.route) { TimerScreen() }
            composable(TimeTrackerDestination.HISTORY.route) { HistoryScreen() }
            composable(TimeTrackerDestination.SUMMARY.route) { SummaryScreen() }
            composable(TimeTrackerDestination.SETTINGS.route) { SettingsScreen() }
        }
    }
}
