package com.example.timetracker.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// Toutes les conversions Instant -> heure locale passent par ce fuseau, pour
// être cohérentes partout dans l'appli (regroupement par jour, affichage).
private val zone: ZoneId = ZoneId.systemDefault()

private val hourFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dayFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.FRENCH)

/** Heure au format 24h, ex. "09:05". */
fun Instant.toHourString(): String = hourFormatter.format(this.atZone(zone))
// Formatte l'heure de début/fin d'une session pour l'affichage.

/**
 * Jour local (ZoneId.systemDefault()) auquel rattacher une session. On se
 * base uniquement sur startTime : une session commencée à 23h50 et terminée
 * à 00h10 reste donc entièrement sur son jour de départ, sans découpage,
 * comme demandé dans le cahier des charges.
 */
fun Instant.toLocalDay(): LocalDate = this.atZone(zone).toLocalDate()
// Clé de regroupement "par jour" utilisée par l'historique et la synthèse.

/** Ex. "jeudi 24 septembre 2026", avec une majuscule initiale. */
fun LocalDate.toDisplayString(): String {
    val formatted = dayFormatter.format(this)
    return formatted.replaceFirstChar { it.titlecase(Locale.FRENCH) }
}

/** Ex. Duration(1h32) -> "1 h 32". Une durée de 0 à 59 min -> "0 h 05". */
fun Duration.toDisplayString(): String {
    val totalMinutes = this.toMinutes()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "%d h %02d".format(hours, minutes)
}
// Format volontairement simple (pas de secondes) : cohérent avec la demande
// "durées en heures et minutes".

/** Durée entre deux instants, jamais négative même si l'utilisateur modifie
 *  manuellement des heures incohérentes dans l'écran d'édition. */
fun durationBetween(start: Instant, end: Instant): Duration {
    val duration = Duration.between(start, end)
    return if (duration.isNegative) Duration.ZERO else duration
}
