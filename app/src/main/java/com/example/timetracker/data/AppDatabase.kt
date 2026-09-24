package com.example.timetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val DEFAULT_CATEGORY_NAMES = listOf("Administration", "Création", "Communication", "Autre")

@Database(
    entities = [Category::class, Session::class, ActiveTimer::class],
    version = 1,
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
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // onCreate n'est appelé qu'une seule fois, à la toute
                        // première création du fichier .db : c'est le bon
                        // endroit pour insérer les catégories par défaut,
                        // sans risquer de les dupliquer aux lancements suivants.
                        CoroutineScope(SupervisorJob()).launch {
                            val dao = getInstance(context).categoryDao()
                            DEFAULT_CATEGORY_NAMES.forEach { name ->
                                dao.insert(Category(name = name))
                            }
                        }
                    }
                })
                .build()
    }
}
