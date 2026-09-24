package com.example.timetracker.ui.navigation

enum class TimeTrackerDestination(val route: String, val label: String) {
    TIMER("timer", "Chrono"),
    HISTORY("history", "Historique"),
    SUMMARY("summary", "Synthèse"),
    SETTINGS("settings", "Réglages")
}
