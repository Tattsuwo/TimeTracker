package com.example.timetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Un projet proposé dans la liste déroulante "Projet" (formulaires d'arrêt du
 * chrono et de modification d'une session).
 *
 * Choix de conception : une session garde le NOM du projet dans son champ
 * [Session.name], plutôt qu'une référence (id) vers cette table, comme le fait
 * [Session.categoryId] pour les catégories. Cette table ne sert donc qu'à
 * mémoriser la liste des projets proposés. Avantage : la table "sessions" et
 * toutes les données déjà enregistrées restent strictement inchangées (la
 * migration se limite à créer cette table et à la remplir avec les noms déjà
 * utilisés). Contrepartie : renommer un projet plus tard demandera de mettre à
 * jour la liste ET les sessions concernées (non prévu pour l'instant).
 */
@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
// Table "projects" : id auto-incrémenté + nom du projet.
