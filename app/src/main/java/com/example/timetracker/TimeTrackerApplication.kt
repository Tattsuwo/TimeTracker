package com.example.timetracker

import android.app.Application
import com.example.timetracker.data.AppDatabase
import com.example.timetracker.repository.TimeTrackerRepository

/**
 * Pas de framework d'injection de dépendances (Hilt, Koin...) dans cette
 * base : le projet est volontairement petit et local, donc une simple
 * instance de repository exposée par l'Application suffit et reste facile à
 * suivre. Le service et les ViewModels y accèdent via
 * (context.applicationContext as TimeTrackerApplication).repository.
 */
class TimeTrackerApplication : Application() {

    val repository: TimeTrackerRepository by lazy {
        val database = AppDatabase.getInstance(this)
        TimeTrackerRepository(
            database = database,
            categoryDao = database.categoryDao(),
            sessionDao = database.sessionDao(),
            activeTimerDao = database.activeTimerDao()
        )
    }
    // "by lazy" : la base de données Room n'est ouverte qu'à la première
    // utilisation réelle du repository, pas au lancement de l'app.
}
