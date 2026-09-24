package com.example.timetracker.data

import androidx.room.Embedded

/**
 * Résultat d'une jointure sessions <-> categories : on affiche presque
 * toujours le nom de la catégorie à côté d'une session (historique, résumé),
 * autant faire la jointure une fois en SQL plutôt que recharger chaque
 * catégorie séparément depuis l'UI.
 */
data class SessionWithCategory(
    @Embedded
    val session: Session,
    val categoryName: String
)
// Une session + le nom (déjà résolu) de sa catégorie.
