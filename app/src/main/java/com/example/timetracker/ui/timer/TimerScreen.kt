package com.example.timetracker.ui.timer

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetracker.ui.rememberTimeTrackerViewModelFactory
import com.example.timetracker.util.toDisplayString
import com.example.timetracker.util.toHourString
import java.time.Duration
import java.time.Instant

@Composable
fun TimerScreen() {
    val viewModel: TimerViewModel = viewModel(factory = rememberTimeTrackerViewModelFactory())
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showStopDialog by remember { mutableStateOf(false) }

    // Demande la permission d'affichage des notifications (obligatoire à
    // partir d'Android 13, API 33) au moment où l'utilisateur démarre un
    // chrono : c'est le moment le plus pertinent pour lui expliquer
    // pourquoi elle est utile. Le chrono démarre dans tous les cas, y
    // compris si la permission est refusée : seule la notification restera
    // invisible, le suivi du temps continue de fonctionner normalement.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* résultat ignoré volontairement, voir commentaire ci-dessus */ }

    LaunchedEffect(viewModel) {
        viewModel.userMessages.collect { message -> snackbarHostState.showSnackbar(message) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            val active = uiState.activeTimer
            when {
                active != null && uiState.isInterruptedRecovery -> {
                    InterruptedRecoveryCard(
                        startedAt = active.startTime,
                        onCloseClicked = { showStopDialog = true }
                    )
                }
                active != null -> {
                    RunningTimerContent(
                        startedAt = active.startTime,
                        elapsed = uiState.elapsed,
                        onStopClicked = { showStopDialog = true }
                    )
                }
                else -> {
                    IdleContent(
                        onStartClicked = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            viewModel.onStartTimer()
                        }
                    )
                }
            }
        }
    }

    if (showStopDialog && uiState.activeTimer != null) {
        StopSessionDialog(
            categories = uiState.categories,
            initialEndTime = Instant.now(),
            allowEditingEndTime = uiState.isInterruptedRecovery,
            onDismiss = { showStopDialog = false },
            onAddCategory = viewModel::onAddCategory,
            onConfirm = { name, description, categoryId, endTime ->
                val currentActive = uiState.activeTimer ?: return@StopSessionDialog
                if (uiState.isInterruptedRecovery) {
                    viewModel.onConfirmCloseInterrupted(currentActive, name, description, categoryId, endTime)
                } else {
                    viewModel.onConfirmStop(name, description, categoryId, endTime)
                }
                showStopDialog = false
            }
        )
    }
}

@Composable
private fun IdleContent(onStartClicked: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Aucun chrono en cours", style = MaterialTheme.typography.titleLarge)
        Button(onClick = onStartClicked, modifier = Modifier.padding(top = 24.dp)) {
            Text("Démarrer")
        }
    }
}

@Composable
private fun RunningTimerContent(startedAt: Instant, elapsed: Duration, onStopClicked: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Démarré à ${startedAt.toHourString()}", style = MaterialTheme.typography.bodyMedium)
        Text(
            elapsed.toDisplayString(),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(onClick = onStopClicked) { Text("Arrêter") }
    }
}

@Composable
private fun InterruptedRecoveryCard(startedAt: Instant, onCloseClicked: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.padding(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Session laissée ouverte", style = MaterialTheme.typography.titleLarge)
            Text(
                "Un chrono démarré à ${startedAt.toHourString()} n'a pas été arrêté normalement " +
                    "(redémarrage du téléphone ou fermeture de l'application). Choisissez une heure " +
                    "de fin pour clôturer cette session."
            )
            OutlinedButton(onClick = onCloseClicked) { Text("Clôturer maintenant") }
        }
    }
}
