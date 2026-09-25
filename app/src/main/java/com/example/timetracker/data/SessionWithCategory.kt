package com.example.timetracker.data

import androidx.room.Embedded

/**
 * Résultat d'une jointure sessions <-> categories : on affiche presque
 * toujours le nom (et maintenant la couleur) de la catégorie à côté d'une
 * session (historique, synthèse), autant faire la jointure une fois en SQL
 * plutôt que recharger chaque catégorie séparément depuis l'UI.
 */
data class SessionWithCategory(
    @Embedded
    val session: Session,
    val categoryName: String,
    val categoryColor: Int
)
// Une session + le nom et la couleur (déjà résolus) de sa catégorie.
