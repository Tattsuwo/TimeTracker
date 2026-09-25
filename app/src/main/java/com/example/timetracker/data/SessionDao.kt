package com.example.timetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query(
        """
        SELECT sessions.*, categories.name AS categoryName, categories.color AS categoryColor
        FROM sessions
        INNER JOIN categories ON categories.id = sessions.categoryId
        ORDER BY sessions.startTime DESC
        """
    )
    fun observeAllWithCategory(): Flow<List<SessionWithCategory>>
    // Source unique pour l'historique ET la synthèse : les deux écrans
    // regroupent simplement cette même liste différemment (voir
    // ui/history/HistoryViewModel.kt et ui/summary/SummaryViewModel.kt),
    // plutôt que de dupliquer la logique de jointure en plusieurs requêtes.

    @Insert
    suspend fun insert(session: Session): Long
    // Crée la session au moment où l'utilisateur arrête le chrono et
    // renseigne nom/description/catégorie.

    @Update
    suspend fun update(session: Session)
    // Utilisé par l'écran d'édition de l'historique (nom, description,
    // catégorie, heures de début/fin).

    @Delete
    suspend fun delete(session: Session)

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: Long): Session?

    @Query("DELETE FROM sessions WHERE categoryId = :categoryId")
    suspend fun deleteByCategoryId(categoryId: Long)
    // Suppression en cascade des sessions d'une catégorie : appelée par
    // TimeTrackerRepository.deleteCategory() juste avant de supprimer la
    // catégorie elle-même, dans une même transaction.

    @Query("SELECT COUNT(*) FROM sessions WHERE categoryId = :categoryId")
    suspend fun countByCategoryId(categoryId: Long): Int
    // Utilisé pour prévenir l'utilisateur du nombre de sessions qui seront
    // supprimées avant qu'il ne confirme la suppression d'une catégorie.
}
