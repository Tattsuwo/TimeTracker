package com.example.timetracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.timetracker.MainActivity
import com.example.timetracker.R
import com.example.timetracker.TimeTrackerApplication
import com.example.timetracker.util.toHourString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Service en avant-plan dont le seul rôle est d'afficher une notification
 * persistante tant qu'un chrono est actif, pour deux raisons :
 * 1. informer clairement l'utilisateur qu'un chrono tourne, même écran
 *    verrouillé ou appli fermée ;
 * 2. réduire fortement le risque que le système tue le processus de l'appli
 *    en arrière-plan (un service "foreground" a une priorité bien plus
 *    élevée qu'un simple processus en tâche de fond).
 *
 * Le service ne fait AUCUN calcul de durée lui-même : à chaque
 * (re)démarrage, il relit l'heure de début directement depuis Room via le
 * repository. Ainsi, même si Android tue puis relance le service, ou si
 * l'heure affichée dans la notification n'est mise à jour qu'une fois par
 * minute, la source de vérité reste toujours la base de données, jamais une
 * variable en mémoire du service.
 */
class TimerForegroundService : Service() {

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? = null
    // Service "démarré" (startService), pas "lié" : personne n'a besoin de
    // dialoguer directement avec lui, il ne fait que pousser une notification.

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannelIfNeeded()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> startForegroundLoop()
        }
        // START_NOT_STICKY : si le système tue quand même ce service faute de
        // mémoire, on ne veut pas qu'il redémarre tout seul sans contexte
        // (l'utilisateur peut très bien avoir arrêté le chrono entre-temps) ;
        // c'est l'état persisté dans Room (ActiveTimer), et non ce service,
        // qui reste la source de vérité au prochain lancement de l'appli.
        return START_NOT_STICKY
    }

    private fun startForegroundLoop() {
        if (job?.isActive == true) return
        val repository = (application as TimeTrackerApplication).repository

        job = scope.launch {
            val active = repository.currentActiveTimer() ?: run {
                stopSelf()
                return@launch
            }
            while (true) {
                startForeground(NOTIFICATION_ID, buildNotification(active.startTime))
                // Une notification par minute suffit largement à informer
                // l'utilisateur qu'un chrono tourne ; la mettre à jour chaque
                // seconde consommerait de la batterie pour un gain
                // d'information nul (l'heure de démarrage, elle, ne change pas).
                delay(ONE_MINUTE_MS)
            }
        }
    }

    private fun buildNotification(startTime: Instant): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_timer_title))
            .setContentText(getString(R.string.notification_timer_text, startTime.toHourString()))
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .setContentIntent(openAppIntent)
            .build()
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        // Les canaux de notification n'existent qu'à partir d'Android 8 (API 26).
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
            // IMPORTANCE_LOW : pas de son ni de vibration, cette notification
            // est informative et persistante, pas une alerte.
        )
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        job?.cancel()
        isRunning = false
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "timer_channel"
        private const val NOTIFICATION_ID = 1
        private const val ONE_MINUTE_MS = 60_000L
        private const val ACTION_STOP = "com.example.timetracker.action.STOP"

        // Reflète simplement si CE processus a un service actif en ce moment :
        // remis à false naturellement si le processus est tué (redémarrage du
        // téléphone, appli tuée par le système), ce qui est exactement le
        // signal recherché par TimerViewModel pour détecter une session
        // interrompue. @Volatile car lu/écrit potentiellement depuis des
        // threads différents (UI côté ViewModel, thread du service ici).
        @Volatile
        var isRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java)
            // ContextCompat.startForegroundService gère la différence
            // pré/post Android 8 pour démarrer un service en avant-plan.
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
