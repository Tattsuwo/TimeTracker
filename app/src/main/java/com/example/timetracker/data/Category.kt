package com.example.timetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Une catégorie d'activité (administration, création, communication, autre,
 * ou une catégorie créée par l'utilisateur).
 *
 * Catégorie et Session sont deux tables séparées reliées par [Session.categoryId]
 * plutôt qu'un simple champ texte sur Session : cela permet de renommer une
 * catégorie (ou de changer sa couleur) sans avoir à réécrire toutes les
 * sessions déjà enregistrées.
 *
 * [color] est stocké en Int (couleur ARGB packée, ex. 0xFF1E88E5.toInt())
 * plutôt qu'en androidx.compose.ui.graphics.Color : la couche data ne dépend
 * ainsi d'aucune bibliothèque d'UI. La conversion vers/depuis Color se fait
 * uniquement côté UI (voir ui/common/CategoryColors.kt).
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int = DEFAULT_COLOR
) {
    companion object {
        // Gris neutre utilisé quand aucune couleur n'est choisie explicitement
        // (catégories créées via le "+ Nouvelle catégorie" rapide pendant
        // l'arrêt d'un chrono, sans passer par l'écran Réglages).
        val DEFAULT_COLOR: Int = 0xFF6D6D6D.toInt() // gris neutre
    }
}
// Table "categories" : id auto-incrémenté + nom et couleur modifiables.
