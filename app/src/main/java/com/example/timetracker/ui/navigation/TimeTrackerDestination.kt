package com.example.timetracker.ui.navigation

enum class TimeTrackerDestination(val route: String, val label: String) {
    TIMER("timer", "Chrono"),
    HISTORY("history", "Historique"),
    SUMMARY("summary", "Synthèse"),
    SETTINGS("settings", "Réglages")
}
// Une seule source de vérité pour les 4 onglets de la barre de navigation du
// bas, réutilisée à la fois par le NavHost et par la NavigationBar.
// L'icône de chaque destination n'est pas stockée ici (contrairement à une
// version précédente utilisant ImageVector) : elle est choisie par un simple
// "when" dans TimeTrackerNavHost.kt, qui appelle l'une des icônes dessinées
// à la main dans ui/common/NavIcons.kt.
