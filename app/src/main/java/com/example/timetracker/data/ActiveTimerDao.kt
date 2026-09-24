package com.example.timetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveTimerDao {

    @Query("SELECT * FROM active_timer WHERE id = ${ActiveTimer.SINGLETON_ID} LIMIT 1")
    fun observe(): Flow<ActiveTimer?>
    // L'écran Chrono collecte ce Flow pour savoir en permanence s'il doit
    // afficher "Démarrer" ou "Arrêter", y compris juste après un
    // redémarrage de l'app.

    @Query("SELECT * FROM active_timer WHERE id = ${ActiveTimer.SINGLETON_ID} LIMIT 1")
    suspend fun getOnce(): ActiveTimer?

    // OnConflictStrategy.ABORT (le défaut d'@Insert) fait échouer l'insertion
    // si une ligne id=SINGLETON_ID existe déjà : c'est ce qui garantit "un
    // seul chrono actif à la fois" au niveau de la base plutôt qu'avec une
    // simple vérification côté Kotlin qui pourrait rater un cas limite.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun start(activeTimer: ActiveTimer)

    @Query("DELETE FROM active_timer WHERE id = ${ActiveTimer.SINGLETON_ID}")
    suspend fun clear()
    // Appelé une fois la session nommée et enregistrée dans "sessions".
}
