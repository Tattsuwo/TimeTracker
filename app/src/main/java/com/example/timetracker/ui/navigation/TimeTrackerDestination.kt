package com.example.timetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

enum class TimeTrackerDestination(val route: String, val label: String, val icon: ImageVector) {
    TIMER("timer", "Chrono", Icons.Filled.Timer),
    HISTORY("history", "Historique", Icons.Filled.History),
    SUMMARY("summary", "Synthèse", Icons.Filled.PieChart),
    SETTINGS("settings", "Réglages", Icons.Filled.Settings)
}
// Une seule source de vérité pour les 4 onglets de la barre de navigation du
// bas, réutilisée à la fois par le NavHost et par la NavigationBar.
