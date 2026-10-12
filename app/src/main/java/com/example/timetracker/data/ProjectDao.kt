package com.example.timetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Project>>
    // Tri alphabétique sans tenir compte des majuscules ("compta" et "Compta"
    // restent côte à côte), pour que le menu reste facile à parcourir.

    @Insert
    suspend fun insert(project: Project): Long
    // Ajoute un projet à la liste.

    @Query("SELECT COUNT(*) FROM projects WHERE name = :name COLLATE NOCASE")
    suspend fun countByNameIgnoreCase(name: String): Int
    // Détecte un doublon sans tenir compte des majuscules/minuscules.

    @Query("DELETE FROM projects")
    suspend fun deleteAll()
    // Utilisé par une restauration JSON (remplacement total des données).
}
