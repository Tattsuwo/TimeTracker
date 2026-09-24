package com.example.timetracker.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetracker.TimeTrackerApplication
import com.example.timetracker.data.ActiveTimer
import com.example.timetracker.data.Category
import com.example.timetracker.service.TimerForegroundService
import com.example.timetracker.util.durationBetween
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

data class TimerUiState(
    val activeTimer: ActiveTimer? = null,
    val elapsed: Duration = Duration.ZERO,
    val categories: List<Category> = emptyList(),
    // true seulement si un chrono était déjà actif AU LANCEMENT de l'appli
    // alors que le service n'était pas en cours d'exécution : signe que la
    // session a été interrompue (redémarrage du téléphone, appli tuée...)
    // plutôt qu'un simple retour normal sur l'écran. Voir le commentaire sur
    // TimerForegroundService.isRunning.
    val isInterruptedRecovery: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModel(private val application: TimeTrackerApplication) : ViewModel() {

    private val repository = application.repository
    private val interruptedAtLaunch = MutableStateFlow(false)

    private val _userMessages = MutableSharedFlow<String>()
    val userMessages: SharedFlow<String> = _userMessages
    // Canal "à usage unique" pour les messages d'erreur ponctuels (ex. Snackbar),
    // distinct de l'état persistant uiState : contrairement à un StateFlow, un
    // message ne doit être consommé qu'une seule fois, pas rejoué à chaque
    // recomposition ou changement de configuration.

    init {
        viewModelScope.launch {
            val active = repository.currentActiveTimer()
            interruptedAtLaunch.value = active != null && !TimerForegroundService.isRunning
        }
    }

    private fun secondTicker() = flow {
        while (true) {
            emit(Unit)
            delay(1_000)
        }
    }
    // Le tick ne tourne que pendant la collecte de uiState ET seulement si un
    // chrono est actif (voir flatMapLatest ci-dessous) : pas de travail inutile
    // en arrière-plan quand aucun chrono ne tourne.

    val uiState: StateFlow<TimerUiState> =
        combine(
            repository.observeActiveTimer(),
            repository.observeCategories(),
            interruptedAtLaunch
        ) { active, categories, interrupted -> Triple(active, categories, interrupted) }
            .flatMapLatest { (active, categories, interrupted) ->
                if (active == null) {
                    flowOf(TimerUiState(categories = categories))
                } else {
                    secondTicker().map {
                        TimerUiState(
                            activeTimer = active,
                            elapsed = durationBetween(active.startTime, Instant.now()),
                            categories = categories,
                            isInterruptedRecovery = interrupted
                        )
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerUiState())

    fun onStartTimer() {
        viewModelScope.launch {
            repository.startTimer()
                .onSuccess { TimerForegroundService.start(application) }
                .onFailure { _userMessages.emit("Un chrono est déjà en cours.") }
        }
    }

    fun onConfirmStop(name: String, description: String, categoryId: Long, endTime: Instant) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _userMessages.emit("Le nom de l'activité est obligatoire.")
                return@launch
            }
            repository.stopTimerAndSaveSession(name, description, categoryId, endTime)
                .onSuccess { TimerForegroundService.stop(application) }
                .onFailure { _userMessages.emit("Impossible d'enregistrer la session.") }
        }
    }

    fun onConfirmCloseInterrupted(
        active: ActiveTimer,
        name: String,
        description: String,
        categoryId: Long,
        endTime: Instant
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _userMessages.emit("Le nom de l'activité est obligatoire.")
                return@launch
            }
            repository.closeInterruptedSession(active, name, description, categoryId, endTime)
                .onSuccess { interruptedAtLaunch.value = false }
                .onFailure { _userMessages.emit("Impossible d'enregistrer la session.") }
        }
    }

    fun onAddCategory(name: String) {
        viewModelScope.launch {
            repository.addCategory(name).onFailure {
                _userMessages.emit(it.message ?: "Impossible de créer la catégorie.")
            }
        }
    }
}
