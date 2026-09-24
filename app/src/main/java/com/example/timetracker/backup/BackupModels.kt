package com.example.timetracker.backup

import kotlinx.serialization.Serializable

/**
 * Format du fichier JSON d'export/import. On sérialise les sessions avec le
 * NOM de leur catégorie (categoryName) plutôt que leur categoryId brut : les
 * id sont des détails internes à la base Room de l'appareil source, ils
 * n'ont aucune raison de correspondre aux id de l'appareil de destination
 * lors d'une restauration. Le nom, lui, est stable et permet de retrouver ou
 * recréer la bonne catégorie à l'import (voir BackupManager.buildRestorePlan).
 */
@Serializable
data class CategoryBackup(
    val name: String
)

@Serializable
data class SessionBackup(
    val name: String,
    val description: String,
    val categoryName: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long
)

@Serializable
data class BackupPayload(
    val formatVersion: Int = 1,
    val exportedAtEpochMillis: Long,
    val categories: List<CategoryBackup>,
    val sessions: List<SessionBackup>
)
