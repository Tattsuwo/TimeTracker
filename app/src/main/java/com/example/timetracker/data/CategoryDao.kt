package com.example.timetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun observeAll(): Flow<List<Category>>
    // Flux réactif : toute vue Compose qui collecte ce Flow se recompose
    // automatiquement dès qu'une catégorie est ajoutée/renommée, sans code
    // de rafraîchissement manuel.

    @Insert
    suspend fun insert(category: Category): Long
    // Ajoute une catégorie (créée par l'utilisateur ou catégorie par défaut).

    @Update
    suspend fun update(category: Category)
    // Renomme une catégorie existante ; comme les sessions référencent son id
    // et non son nom, aucune session existante n'a besoin d'être modifiée.

    @Delete
    suspend fun delete(category: Category)
    // Supprime la catégorie elle-même. Toujours appelée par le repository
    // APRÈS avoir supprimé ses sessions via SessionDao.deleteByCategoryId :
    // la suppression en cascade se fait donc côté application (dans une
    // transaction), pas via une contrainte de clé étrangère en base.

    @Query("SELECT COUNT(*) FROM categories WHERE name = :name")
    suspend fun countByName(name: String): Int
    // Utilisé pour éviter les doublons de nom lors de la création.

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countAll(): Int
    // Utilisé pour empêcher de supprimer la toute dernière catégorie
    // restante (il en faut au moins une pour pouvoir nommer une session).

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
    // Utilisé par une restauration JSON en mode "remplacement" : on vide la
    // table avant de réinsérer le contenu du fichier importé.
}
