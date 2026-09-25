package com.example.timetracker.repository

import com.example.timetracker.backup.BackupManager
import com.example.timetracker.backup.BackupPayload
import com.example.timetracker.backup.CategoryBackup
import com.example.timetracker.backup.SessionBackup
import com.example.timetracker.data.ActiveTimer
import com.example.timetracker.data.ActiveTimerDao
import com.example.timetracker.data.AppDatabase
import com.example.timetracker.data.Category
import com.example.timetracker.data.CategoryDao
import com.example.timetracker.data.Session
import com.example.timetracker.data.SessionDao
import com.example.timetracker.data.SessionWithCategory
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant

/** Résumé renvoyé après une restauration, affiché à l'utilisateur. */
data class ImportSummary(val categoriesCreated: Int, val sessionsImported: Int)

/**
 * Point d'entrée unique pour toute la logique de données. Les ViewModels ne
 * connaissent que cette classe, jamais les DAO Room directement : si demain
 * la persistance change (migration Room, ajout d'un cache...), seul ce
 * fichier a besoin d'être touché.
 */
class TimeTrackerRepository(
    private val database: AppDatabase,
    private val categoryDao: CategoryDao,
    private val sessionDao: SessionDao,
    private val activeTimerDao: ActiveTimerDao,
    private val backupManager: BackupManager = BackupManager()
) {

    // --- Catégories ---------------------------------------------------

    fun observeCategories(): Flow<List<Category>> = categoryDao.observeAll()

    suspend fun addCategory(name: String, color: Int = Category.DEFAULT_COLOR): Result<Long> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("Nom vide"))
        if (categoryDao.countByName(trimmed) > 0) {
            return Result.failure(IllegalStateException("Cette catégorie existe déjà"))
        }
        return Result.success(categoryDao.insert(Category(name = trimmed, color = color)))
    }
    // Result<> plutôt qu'une exception non gérée : l'UI peut afficher un
    // message clair ("nom déjà utilisé") sans bloc try/catch générique.
    // color a une valeur par défaut : les appels existants (catégorie créée
    // à la volée depuis le sélecteur pendant l'arrêt d'un chrono) n'ont pas
    // besoin de choisir explicitement une couleur.

    suspend fun updateCategory(category: Category, newName: String, newColor: Int): Result<Unit> = runCatching {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) error("Le nom ne peut pas être vide")
        categoryDao.update(category.copy(name = trimmed, color = newColor))
    }

    /**
     * Supprime une catégorie ET toutes les sessions qui lui sont associées.
     * Les deux suppressions sont faites dans une même transaction Room
     * (withTransaction) : soit les deux réussissent, soit aucune n'est
     * appliquée, pour ne jamais se retrouver avec des sessions orphelines
     * si l'opération est interrompue en plein milieu.
     */
    suspend fun deleteCategory(category: Category): Result<Unit> = runCatching {
        if (categoryDao.countAll() <= 1) {
            error("Impossible de supprimer la dernière catégorie restante")
        }
        database.withTransaction {
            sessionDao.deleteByCategoryId(category.id)
            categoryDao.delete(category)
        }
    }

    suspend fun countSessionsInCategory(categoryId: Long): Int = sessionDao.countByCategoryId(categoryId)
    // Utilisé pour prévenir l'utilisateur ("X sessions seront supprimées")
    // avant qu'il ne confirme la suppression d'une catégorie.

    // --- Chronomètre ----------------------------------------------------

    fun observeActiveTimer(): Flow<ActiveTimer?> = activeTimerDao.observe()

    suspend fun currentActiveTimer(): ActiveTimer? = activeTimerDao.getOnce()

    suspend fun startTimer(now: Instant = Instant.now()): Result<Unit> = runCatching {
        activeTimerDao.start(ActiveTimer(startTime = now))
        // Si une ligne existe déjà, ActiveTimerDao.start() lève une
        // SQLiteConstraintException (OnConflictStrategy.ABORT) : runCatching
        // la transforme en Result.failure exploitable par l'UI plutôt que
        // de faire planter l'appli si l'utilisateur double-tape "Démarrer".
    }

    suspend fun stopTimerAndSaveSession(
        name: String,
        description: String,
        categoryId: Long,
        endTime: Instant = Instant.now()
    ): Result<Long> = runCatching {
        val active = activeTimerDao.getOnce() ?: error("Aucun chrono en cours")
        val session = Session(
            name = name.trim(),
            description = description.trim(),
            categoryId = categoryId,
            startTime = active.startTime,
            endTime = endTime
        )
        val id = sessionDao.insert(session)
        // L'ordre insert() puis clear() est important : si l'appli est tuée
        // pile entre les deux, on retrouve au pire encore un chrono "actif"
        // au prochain lancement (signalé à l'utilisateur), jamais une
        // session perdue silencieusement.
        activeTimerDao.clear()
        id
    }
    // Toute la fonction est enveloppée dans runCatching (et pas seulement le
    // "cas attendu" d'absence de chrono actif) : une erreur SQLite inattendue
    // sur l'insert() doit, elle aussi, remonter comme un Result.failure
    // exploitable par le ViewModel plutôt que de faire planter l'appli.

    /**
     * Ferme manuellement une session restée ouverte après un redémarrage du
     * téléphone (chrono actif détecté au lancement). Fonctionnellement
     * identique à [stopTimerAndSaveSession] : l'endTime n'est simplement pas
     * "maintenant" par défaut, l'utilisateur le choisit dans le formulaire.
     */
    suspend fun closeInterruptedSession(
        active: ActiveTimer,
        name: String,
        description: String,
        categoryId: Long,
        endTime: Instant
    ): Result<Long> = runCatching {
        val session = Session(
            name = name.trim(),
            description = description.trim(),
            categoryId = categoryId,
            startTime = active.startTime,
            endTime = endTime
        )
        val id = sessionDao.insert(session)
        activeTimerDao.clear()
        id
    }

    // --- Sessions terminées --------------------------------------------

    fun observeSessions(): Flow<List<SessionWithCategory>> = sessionDao.observeAllWithCategory()

    suspend fun updateSession(session: Session) = sessionDao.update(session)

    suspend fun deleteSession(session: Session) = sessionDao.delete(session)

    // --- Sauvegarde / restauration JSON ---------------------------------

    /** Construit le JSON complet (toutes catégories + toutes sessions). */
    suspend fun exportAllToJson(): String {
        val categories = categoryDao.observeAll().first()
        val sessionsWithCategory = sessionDao.observeAllWithCategory().first()
        val payload = BackupPayload(
            exportedAtEpochMillis = Instant.now().toEpochMilli(),
            categories = categories.map { CategoryBackup(name = it.name, color = it.color) },
            sessions = sessionsWithCategory.map {
                SessionBackup(
                    name = it.session.name,
                    description = it.session.description,
                    categoryName = it.categoryName,
                    startTimeEpochMillis = it.session.startTime.toEpochMilli(),
                    endTimeEpochMillis = it.session.endTime.toEpochMilli()
                )
            }
        )
        return backupManager.serialize(payload)
    }

    /**
     * Restaure un JSON exporté précédemment. Les sessions sont rattachées
     * par NOM de catégorie : si une catégorie du même nom existe déjà sur cet
     * appareil, elle est réutilisée ; sinon elle est créée à la volée.
     *
     * Import volontairement additif (pas de suppression, pas de détection de
     * doublons) : restaurer deux fois le même fichier créera deux fois les
     * mêmes sessions. C'est une base simple à faire évoluer si besoin
     * (par ex. ignorer une session dont le triplet nom/début/fin existe déjà).
     */
    suspend fun importFromJson(json: String): Result<ImportSummary> = runCatching {
        val payload = backupManager.deserialize(json)
        val categoryIdByName = mutableMapOf<String, Long>()
        var categoriesCreated = 0

        suspend fun resolveCategoryId(name: String, color: Int = Category.DEFAULT_COLOR): Long {
            categoryIdByName[name]?.let { return it }
            val existing = categoryDao.getByName(name)
            // La couleur du JSON n'est appliquée qu'à la création d'une
            // nouvelle catégorie ; si elle existe déjà localement, sa couleur
            // actuelle (potentiellement déjà personnalisée) n'est pas écrasée.
            val id = existing?.id
                ?: categoryDao.insert(Category(name = name, color = color)).also { categoriesCreated++ }
            categoryIdByName[name] = id
            return id
        }

        payload.categories.forEach { resolveCategoryId(it.name, it.color) }
        payload.sessions.forEach { backup ->
            val categoryId = resolveCategoryId(backup.categoryName)
            sessionDao.insert(
                Session(
                    name = backup.name,
                    description = backup.description,
                    categoryId = categoryId,
                    startTime = Instant.ofEpochMilli(backup.startTimeEpochMillis),
                    endTime = Instant.ofEpochMilli(backup.endTimeEpochMillis)
                )
            )
        }

        ImportSummary(categoriesCreated = categoriesCreated, sessionsImported = payload.sessions.size)
    }

    /** Écrit l'export JSON complet dans le fichier choisi par l'utilisateur via le SAF. */
    suspend fun exportToUri(context: android.content.Context, uri: android.net.Uri): Result<Unit> =
        runCatching { backupManager.writeToUri(context, uri, exportAllToJson()) }

    /** Lit puis restaure le fichier JSON choisi par l'utilisateur via le SAF. */
    suspend fun importFromUri(context: android.content.Context, uri: android.net.Uri): Result<ImportSummary> =
        runCatching { backupManager.readFromUri(context, uri) }
            .fold(
                onSuccess = { importFromJson(it) },
                onFailure = { Result.failure(it) }
            )
}
