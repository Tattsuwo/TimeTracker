package com.example.timetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Couleurs par défaut (ARGB) des 4 catégories fournies avec l'application,
// choisies pour rester visuellement distinctes. Voir aussi
// ui/common/CategoryColors.kt pour la palette proposée à l'utilisateur.
private const val COLOR_ADMINISTRATION = 0xFF1E88E5.toInt() // bleu
private const val COLOR_CREATION = 0xFF8E24AA.toInt()       // violet
private const val COLOR_COMMUNICATION = 0xFF00897B.toInt()  // teal
private const val COLOR_AUTRE = 0xFF6D6D6D.toInt()          // gris

private val DEFAULT_CATEGORIES = listOf(
    Category(name = "Administration", color = COLOR_ADMINISTRATION),
    Category(name = "Création", color = COLOR_CREATION),
    Category(name = "Communication", color = COLOR_COMMUNICATION),
    Category(name = "Autre", color = COLOR_AUTRE)
)

/**
 * Migration 1 -> 2 : ajout de la colonne "color" sur la table categories
 * (fonctionnalité de couleur par catégorie). Une vraie migration SQL est
 * utilisée ici plutôt qu'une réinitialisation destructrice de la base : les
 * catégories et sessions déjà enregistrées par l'utilisateur sont conservées.
 * Les UPDATE qui suivent l'ALTER TABLE ne font que donner une jolie couleur
 * par défaut aux 4 catégories fournies avec l'appli si elles existent déjà
 * (par leur nom) ; toute catégorie personnalisée récupère simplement la
 * couleur grise neutre posée par le DEFAULT de l'ALTER TABLE.
 */
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE categories ADD COLUMN color INTEGER NOT NULL DEFAULT ${Category.DEFAULT_COLOR}")
        db.execSQL("UPDATE categories SET color = $COLOR_ADMINISTRATION WHERE name = 'Administration'")
        db.execSQL("UPDATE categories SET color = $COLOR_CREATION WHERE name = 'Création'")
        db.execSQL("UPDATE categories SET color = $COLOR_COMMUNICATION WHERE name = 'Communication'")
        db.execSQL("UPDATE categories SET color = $COLOR_AUTRE WHERE name = 'Autre'")
    }
}

@Database(
    entities = [Category::class, Session::class, ActiveTimer::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun sessionDao(): SessionDao
    abstract fun activeTimerDao(): ActiveTimerDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }
        // Pattern singleton classique pour Room : une seule connexion SQLite
        // pour toute l'application, quel que soit l'écran qui la demande.

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "timetracker.db"
            )
                .addMigrations(MIGRATION_1_2)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // onCreate n'est appelé qu'une seule fois, à la toute
                        // première création du fichier .db (installation neuve,
                        // donc jamais en passant par MIGRATION_1_2 ci-dessus) :
                        // c'est le bon endroit pour insérer les catégories par
                        // défaut, avec leurs couleurs, sans risquer de les
                        // dupliquer aux lancements suivants.
                        CoroutineScope(SupervisorJob()).launch {
                            val dao = getInstance(context).categoryDao()
                            DEFAULT_CATEGORIES.forEach { category -> dao.insert(category) }
                        }
                    }
                })
                .build()
    }
}
