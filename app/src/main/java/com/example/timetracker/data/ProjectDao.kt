package com.example.timetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT COUNT(*) FROM projects WHERE name = :name COLLATE NOCASE AND id != :excludedId")
    suspend fun countOtherByNameIgnoreCase(name: String, excludedId: Long): Int
    // Même détection de doublon, mais en ignorant le projet qu'on est en train
    // de renommer : permet de ne changer que la casse du sien ("compta" -> "Compta")
    // sans que celui-ci soit pris pour son propre doublon.

    @Update
    suspend fun update(project: Project)
    // Renomme un projet dans la liste (les sessions sont mises à jour à part,
    // voir SessionDao.renameProject et TimeTrackerRepository.renameProject).

    @Delete
    suspend fun delete(project: Project)
    // Retire un projet de la liste (ses sessions sont supprimées à part, voir
    // TimeTrackerRepository.deleteProject).

    @Query("DELETE FROM projects")
    suspend fun deleteAll()
    // Utilisé par une restauration JSON (remplacement total des données).
}
