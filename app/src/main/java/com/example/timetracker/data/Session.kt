package com.example.timetracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Une session de travail terminée : nom, description, catégorie, heure de
 * début et heure de fin. La durée n'est jamais stockée, elle se recalcule à
 * l'affichage avec Duration.between(startTime, endTime) (voir util/TimeFormat.kt).
 *
 * Choix de conception important : cette table ne contient QUE des sessions
 * terminées (name/description/categoryId toujours renseignés). Le chrono "en
 * cours" (démarré mais pas encore nommé) n'est donc pas une ligne de cette
 * table mais une ligne de la table séparée [ActiveTimer] : cela colle au
 * modèle de données demandé (Session = nom + description + catégorie + début
 * + fin, tous obligatoires) sans avoir besoin de champs nullables ici.
 *
 * startTime/endTime sont stockés en Instant (UTC, TypeConverters dans
 * Converters.kt). Le regroupement "par jour" se fait par contre en heure
 * locale (ZoneId.systemDefault()) au moment de l'affichage : c'est ce qui
 * garantit qu'une session commencée à 23h50 et terminée à 00h10 reste
 * comptée entièrement sur son jour de départ, comme demandé.
 */
@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("categoryId")]
)
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val categoryId: Long,
    val startTime: Instant,
    val endTime: Instant
)
// Table "sessions" : une ligne = une activité terminée et nommée.
