package com.example.timetracker.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * S'occupe uniquement du format JSON et des entrées/sorties fichier via le
 * Storage Access Framework (SAF). Ne connaît pas Room : c'est
 * TimeTrackerRepository qui convertit les entités <-> BackupPayload et
 * applique la restauration en base (voir Repository.exportAllToJson /
 * importFromJson).
 *
 * SAF (ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT, lancés depuis
 * SettingsScreen) est utilisé plutôt qu'un chemin de fichier "en dur" : cela
 * évite d'avoir à demander une permission de stockage (READ/WRITE_EXTERNAL_STORAGE),
 * l'utilisateur choisissant lui-même l'emplacement via le sélecteur système,
 * qui accorde l'accès au fichier précis choisi.
 */
class BackupManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        // ignoreUnknownKeys : un fichier exporté par une future version de
        // l'appli (avec des champs en plus) reste importable par une version
        // plus ancienne sans planter.
    }

    fun serialize(payload: BackupPayload): String = json.encodeToString(payload)

    fun deserialize(content: String): BackupPayload = json.decodeFromString(content)

    suspend fun writeToUri(context: Context, uri: Uri, content: String) =
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(content.toByteArray(Charsets.UTF_8))
            } ?: error("Impossible d'ouvrir le fichier de destination")
        }
    // Dispatchers.IO : écriture disque, ne doit jamais bloquer le thread principal.

    suspend fun readFromUri(context: Context, uri: Uri): String =
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            } ?: error("Impossible d'ouvrir le fichier sélectionné")
        }
}
