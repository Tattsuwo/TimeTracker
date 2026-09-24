package com.example.timetracker.data

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Room ne sait stocker que des types primitifs (SQLite n'a pas de type
 * date/heure natif) : on convertit donc Instant <-> millisecondes epoch.
 * Instant (et non LocalDateTime) est utilisé pour la colonne stockée car il
 * représente un instant absolu, indépendant du fuseau horaire de
 * l'utilisateur ; le fuseau n'intervient qu'à l'affichage/au regroupement
 * (voir util/TimeFormat.kt), ce qui évite les incohérences si l'appareil
 * change de fuseau entre deux sessions.
 */
class Converters {
    @TypeConverter
    fun fromEpochMillis(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)
    // Long stocké en base -> Instant utilisé côté Kotlin.

    @TypeConverter
    fun instantToEpochMillis(instant: Instant?): Long? = instant?.toEpochMilli()
    // Instant côté Kotlin -> Long stocké en base.
}
