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
    // Supprime une catégorie (interdit en pratique si des sessions la
    // référencent encore, à cause de la contrainte RESTRICT sur la clé
    // étrangère de Session).

    @Query("SELECT COUNT(*) FROM categories WHERE name = :name")
    suspend fun countByName(name: String): Int
    // Utilisé pour éviter les doublons de nom lors de la création.

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Category?
    // Utilisé lors d'une restauration JSON pour relier chaque session
    // importée à la catégorie locale du même nom (voir Repository.importFromJson).
}
