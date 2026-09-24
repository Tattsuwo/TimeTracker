package com.example.timetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Une catégorie d'activité (administration, création, communication, autre,
 * ou une catégorie créée par l'utilisateur).
 *
 * Catégorie et Session sont deux tables séparées reliées par [Session.categoryId]
 * plutôt qu'un simple champ texte sur Session : cela permet de renommer une
 * catégorie sans avoir à réécrire toutes les sessions déjà enregistrées.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
// Table "categories" : id auto-incrémenté + nom modifiable.
