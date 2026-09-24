package com.example.timetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Représente le chronomètre actuellement en cours, s'il y en a un.
 *
 * Il n'y a jamais qu'une seule ligne dans cette table (id fixé à SINGLETON_ID) :
 * - une ligne présente = un chrono tourne depuis [startTime] et n'a pas
 *   encore été arrêté/nommé ;
 * - aucune ligne = aucun chrono actif.
 *
 * Persister cet état dans Room (plutôt qu'en mémoire dans le service ou
 * l'Activity) est ce qui permet de répondre à deux exigences du cahier des
 * charges avec un minimum de code :
 * 1. "un seul chrono actif à la fois" : il suffit de vérifier qu'aucune ligne
 *    n'existe déjà avant d'en insérer une ;
 * 2. "si le téléphone redémarre, la session ne reprend pas automatiquement
 *    mais est signalée au prochain lancement" : au démarrage de l'app, on
 *    regarde simplement si une ligne existe encore ici (le redémarrage du
 *    téléphone tue le service, mais ne touche pas à la base de données).
 */
@Entity(tableName = "active_timer")
data class ActiveTimer(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val startTime: Instant
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
// Table "active_timer" : 0 ou 1 ligne, l'unique chrono en cours.
