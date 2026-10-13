package com.example.timetracker.repository

import com.example.timetracker.backup.BackupManager
import com.example.timetracker.backup.BackupPayload
import com.example.timetracker.backup.CategoryBackup
import com.example.timetracker.backup.ExcelExporter
import com.example.timetracker.backup.SessionBackup
import com.example.timetracker.data.ActiveTimer
import com.example.timetracker.data.ActiveTimerDao
import com.example.timetracker.data.AppDatabase
import com.example.timetracker.data.Category
import com.example.timetracker.data.CategoryDao
import com.example.timetracker.data.Project
import com.example.timetracker.data.ProjectDao
import com.example.timetracker.data.Session
import com.example.timetracker.data.SessionDao
import com.example.timetracker.data.SessionWithCategory
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
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
    private val projectDao: ProjectDao,
    private val activeTimerDao: ActiveTimerDao,
    private val backupManager: BackupManager = BackupManager(),
    private val excelExporter: ExcelExporter = ExcelExporter()
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

    // --- Projets ------------------------------------------------------

    fun observeProjects(): Flow<List<Project>> = projectDao.observeAll()

    /**
     * Ajoute un projet à la liste déroulante. Refuse un nom vide ou déjà
     * présent (sans tenir compte des majuscules : "compta" et "Compta" sont le
     * même projet), pour éviter les quasi-doublons dans le menu.
     */
    suspend fun addProject(name: String): Result<Long> = runCatching {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) error("Le nom du projet ne peut pas être vide")
        if (projectDao.countByNameIgnoreCase(trimmed) > 0) error("Ce projet existe déjà")
        projectDao.insert(Project(name = trimmed))
    }

    /**
     * Renomme un projet : la liste ET toutes les sessions qui portent l'ancien
     * nom sont mises à jour dans une même transaction (tout ou rien), pour que
     * l'historique, la synthèse et les exports restent cohérents. Refuse un nom
     * vide ou déjà pris par UN AUTRE projet (majuscules ignorées) ; changer
     * seulement la casse de son propre nom est autorisé.
     */
    suspend fun renameProject(project: Project, newName: String): Result<Unit> = runCatching {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) error("Le nom du projet ne peut pas être vide")
        if (projectDao.countOtherByNameIgnoreCase(trimmed, project.id) > 0) error("Ce projet existe déjà")
        if (trimmed != project.name) {
            database.withTransaction {
                sessionDao.renameProject(oldName = project.name, newName = trimmed)
                projectDao.update(project.copy(name = trimmed))
            }
        }
    }

    /**
     * Supprime un projet ET toutes les sessions qui lui sont associées, dans une
     * même transaction (comme pour les catégories). Contrairement aux
     * catégories, il n'y a pas de minimum : la liste peut être vide, on peut
     * toujours en recréer un depuis le formulaire d'une session.
     */
    suspend fun deleteProject(project: Project): Result<Unit> = runCatching {
        database.withTransaction {
            sessionDao.deleteByProjectName(project.name)
            projectDao.delete(project)
        }
    }

    suspend fun countSessionsInProject(project: Project): Int = sessionDao.countByProjectName(project.name)
    // Pour prévenir l'utilisateur ("X sessions seront supprimées") avant la confirmation.

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
            projects = projectDao.observeAll().first().map { it.name },
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
     * Restaure un JSON exporté précédemment, EN REMPLAÇANT ENTIÈREMENT les
     * données actuelles : toutes les sessions et catégories existantes sont
     * supprimées avant de réinsérer le contenu du fichier. C'est un choix
     * assumé (plutôt qu'une fusion additive) : redonner un point de départ
     * propre et prévisible après restauration, sans risquer de doublons si
     * le même fichier est importé plusieurs fois.
     *
     * Le chrono actif éventuel (ActiveTimer) n'est volontairement pas touché :
     * il n'a aucun rapport avec les données sauvegardées/restaurées.
     *
     * Le vidage puis la réinsertion se font dans une seule transaction : si
     * l'import échoue en cours de route (fichier corrompu...), les anciennes
     * données ne sont jamais perdues sans que les nouvelles aient pu prendre
     * leur place.
     */
    suspend fun importFromJson(json: String): Result<ImportSummary> = runCatching {
        val payload = backupManager.deserialize(json)

        database.withTransaction {
            sessionDao.deleteAll()
            categoryDao.deleteAll()
            projectDao.deleteAll()

            val categoryIdByName = mutableMapOf<String, Long>()

            suspend fun resolveCategoryId(name: String, color: Int = Category.DEFAULT_COLOR): Long {
                categoryIdByName[name]?.let { return it }
                val id = categoryDao.insert(Category(name = name, color = color))
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

            // Liste des projets : ceux du fichier, plus tout nom utilisé par une
            // session mais absent de cette liste (fichier exporté avant
            // l'existence de la liste des projets) : ainsi chaque session
            // retrouve toujours son projet dans le menu.
            (payload.projects + payload.sessions.map { it.name })
                .filter { it.isNotBlank() }
                .distinct()
                .forEach { projectDao.insert(Project(name = it)) }

            ImportSummary(categoriesCreated = categoryIdByName.size, sessionsImported = payload.sessions.size)
        }
    }

    /** Écrit l'export JSON complet dans le fichier choisi par l'utilisateur via le SAF. */
    suspend fun exportToUri(context: android.content.Context, uri: android.net.Uri): Result<Unit> =
        runCatching { backupManager.writeToUri(context, uri, exportAllToJson()) }

    /**
     * Écrit un fichier Excel (.xlsx) listant toutes les sessions terminées, de
     * la plus ancienne à la plus récente, dans le fichier choisi via le SAF.
     * Le chrono éventuellement en cours n'en fait pas partie (il n'est pas
     * encore une session). La construction du classeur est faite hors du
     * thread principal (Dispatchers.Default) : avec beaucoup de sessions, la
     * génération et la compression ne doivent pas figer l'interface.
     */
    suspend fun exportToExcelUri(context: android.content.Context, uri: android.net.Uri): Result<Unit> =
        runCatching {
            val sessions = sessionDao.observeAllWithCategory().first()
            val bytes = withContext(Dispatchers.Default) { excelExporter.build(sessions) }
            backupManager.writeBytesToUri(context, uri, bytes)
        }

    /** Lit puis restaure le fichier JSON choisi par l'utilisateur via le SAF. */
    suspend fun importFromUri(context: android.content.Context, uri: android.net.Uri): Result<ImportSummary> =
        runCatching { backupManager.readFromUri(context, uri) }
            .fold(
                onSuccess = { importFromJson(it) },
                onFailure = { Result.failure(it) }
            )
}
